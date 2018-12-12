package expensify.bot.services;

import expensify.bot.domain.ExpenseDto;
import expensify.bot.utils.ExpensifyAuth;
import expensify.bot.utils.TextAnalyser;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.multipart.MultipartBody;
import io.reactivex.Flowable;
import model.InboundMessage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ExpensifyService {

  private static final Logger LOG = LoggerFactory.getLogger(ExpensifyService.class);

  private static final Map<String, ExpensifyAuth> EXPENSIFY_AUTH_MAP = new HashMap<>();
  private static final Map<Long, String> ACTIVE_USERS_MAP = new HashMap<>();

  private static final String SORRY = "Sorry, I didn't get what you said";
  private static final String NOT_AUTHENTICATED = "Sorry, You are not authenticated yet";
  private static final String NO_AMOUNT = "Sorry, I could not extract any amount of what you just said";
  private static final String TOO_MANY_AMOUNTS = "Sorry, You last sentense seems to contains multiple amounts";
  public static final String REQUEST_JOB_DESCRIPTION_PARAM = "requestJobDescription";
  public static final String EXPENSE_URI = "https://integrations.expensify.com/Integration-Server/ExpensifyIntegrations";

  private TemplateService templateService;

  private TextAnalyser analyser = new TextAnalyser();

  @Client("https://integrations.expensify.com/Integration-Server/ExpensifyIntegrations")
  @Inject
  RxHttpClient httpClient;


  public ExpensifyService(TemplateService templateService) {
    this.templateService = templateService;
  }

  public String processMessage(InboundMessage message) {
    final String originalMessageText = message.getMessageText();
    if (analyser.isAuthAction(originalMessageText)) {
      List<String> tokens = analyser.extractAuthTokens(originalMessageText);
      if (tokens.size() == 2) {
        LOG.info("User {} just sent his authentication tokens", message.getUser().getEmail());
        EXPENSIFY_AUTH_MAP.put(message.getUser().getEmail(), new ExpensifyAuth(tokens.get(0), tokens.get(1)));
        ACTIVE_USERS_MAP.put(message.getUser().getUserId(), message.getUser().getEmail());
        return "You are now authenticated on Expensify";
      }
    } else if (analyser.isHelpAction(originalMessageText)) {
      String helpMessage = templateService.getHelpMessage(message.getUser().getEmail());
      return helpMessage;
    } else if (analyser.isExpenseAction(originalMessageText)) {
      String email = message.getUser().getEmail();
      String messageText = originalMessageText;
      ExpensifyAuth auth = EXPENSIFY_AUTH_MAP.get(email);
      if (auth == null) {
        return NOT_AUTHENTICATED;
      }
      List<BigDecimal> amounts = analyser.extractAmount(messageText);
      if (amounts.isEmpty()) {
        return NO_AMOUNT;
      } else if (amounts.size() > 1) {
        return TOO_MANY_AMOUNTS;
      } else {
        int amount = amounts.get(0).multiply(BigDecimal.valueOf(100)).intValue();
        String merchant = analyser.extractMerchant(messageText);

        postExpense(email, auth, amount, merchant);
        return "expensed !";
      }
    } else if (analyser.isExpenseListAction(originalMessageText)) {

      String email = message.getUser().getEmail();
      String messageText = originalMessageText;
      ExpensifyAuth auth = EXPENSIFY_AUTH_MAP.get(email);
      try {
        List<ExpenseDto> expenseList = listExpenses(auth);
        LOG.info("Expense list size : {}", expenseList.size());
      } catch (IOException e) {
        // throws an exception
      }

    }
    return SORRY;
  }


  private void postExpense(String email, ExpensifyAuth auth, int amount, String merchant) {
    String expensePayload = templateService.expensePayload(email, auth, amount, merchant, new Date());

    MultipartBody requestBody = MultipartBody.builder()
            .addPart(
                REQUEST_JOB_DESCRIPTION_PARAM,
                    expensePayload
            ).build();

    MutableHttpRequest<MultipartBody> multipartBodyMutableHttpRequest = HttpRequest.POST("", requestBody)
            .contentType(MediaType.MULTIPART_FORM_DATA_TYPE);

    Flowable<HttpResponse<String>> call = httpClient.exchange(
            multipartBodyMutableHttpRequest, String.class
    );
    HttpResponse<String> response = call.blockingFirst();
    LOG.info("Expensify returned {} : \n{}", response.code(), response.body());
  }


  /**
   * Return all authenticated users emails
   */
  public Map<Long, String> getActiveUsers(){
    return ACTIVE_USERS_MAP;
  }

  /**
   * This method return a list of Expense.
   */
  private List<ExpenseDto> listExpenses(ExpensifyAuth auth) throws IOException {

    Instant instant = LocalDate.now().minusDays(10).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    Date fromDate = Date.from(instant);

    final String expenseListPayload =
        templateService.getExpenseListPayload(auth, 10, fromDate);

    final String template = templateService.getExpenseListTemplate();
    String reportName = getReportName(expenseListPayload, template);

    final String expenseDownloadPayload = templateService.downloadExpenseListPayload(auth, reportName);
    String reportString = getReport(expenseDownloadPayload);

    return parseExpenseReport(reportString);
  }

  /**
   * This method is reponsible for parse the expense Report to return a List of {@link ExpenseDto}.
   */
  private List<ExpenseDto> parseExpenseReport(String reportString) {

    List<ExpenseDto> result = new ArrayList<>();

    if (StringUtils.isNotEmpty(reportString)) {
      Scanner scanner = new Scanner(reportString);
      String expenseLine = scanner.nextLine(); // skip first line with columns names

      while (scanner.hasNextLine()) {
        expenseLine = scanner.nextLine();
        String[] expenseColumns = expenseLine.split(",");

        Arrays.stream(expenseColumns).forEach(curretnExpense -> {
          String merchant = expenseColumns[0];
          Double amount = Double.valueOf(expenseColumns[1]);
          String currency = expenseColumns[2];
          String created = expenseColumns[3];
          String reportNumber = expenseColumns[4];
          String expenseNumber = expenseColumns[5];

          ExpenseDto expenseDto = new ExpenseDto(merchant, amount, currency, created, reportNumber, expenseNumber);
          result.add(expenseDto);
        });
      }
    }
     return result;
  }

  /**
   * This method invoke expensy rest api to get the nam eof the report which contains all expenses from 10 days ago.
   * @param expenseListPayload this parameter represents the body of the request.
   * @param template this template represents the format of the report.
   * @return a {@link List} od {@link ExpenseDto}.
   * @throws IOException
   */
  private String getReportName(final String expenseListPayload, final String template) throws IOException {

    HttpPost httpPost = new HttpPost(EXPENSE_URI);

    HttpEntity entity = MultipartEntityBuilder.create()
        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
        .addTextBody("template", template)
        .addTextBody(REQUEST_JOB_DESCRIPTION_PARAM, expenseListPayload)
        .build();
    httpPost.setEntity(entity);

    CloseableHttpClient httpclient = HttpClients.createDefault();
    CloseableHttpResponse response = httpclient.execute(httpPost);

    if (response.getStatusLine().getStatusCode() != HttpStatus.OK.getCode()) {

      throw new RuntimeException("The expense list action failed, please contact the Expense Bot Admin");
    }

    InputStream content = response.getEntity().getContent();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    IOUtils.copy(content, bos);
    return new String(bos.toByteArray());

  }

  /**
   * This method is responsible to get the expense report.
   * @param expenseDownloadPayload represent the body of the request to get the expense report.
   * @return a String represents the expense report.
   */
  private String getReport(final String expenseDownloadPayload) throws IOException {

    HttpPost httpPost = new HttpPost(EXPENSE_URI);

    HttpEntity entityReport = MultipartEntityBuilder.create()
        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
        .addTextBody(REQUEST_JOB_DESCRIPTION_PARAM, expenseDownloadPayload)
        .build();
    httpPost.setEntity(entityReport);

    CloseableHttpClient httpclient = HttpClients.createDefault();
    CloseableHttpResponse downloadResponse = httpclient.execute(httpPost);

    if (downloadResponse.getStatusLine().getStatusCode() != HttpStatus.OK.getCode()) {
      throw new RuntimeException("The expense download report action failed, please contact the Expense Bot Admin");
    }

    ByteArrayOutputStream csvBos = new ByteArrayOutputStream();
    downloadResponse.getEntity().writeTo(csvBos);

    //IOUtils.copy(csvStream, csvBos);
    String result = new String(csvBos.toByteArray());
    LOG.info("CSV = {}", result);
    return result ;
  }

}

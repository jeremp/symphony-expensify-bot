package expensify.bot.services;

import expensify.bot.utils.ExpensifyAuth;
import expensify.bot.utils.TextAnalyser;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.multipart.MultipartBody;
import io.reactivex.Flowable;
import model.InboundMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ExpensifyService {

  private static final Logger LOG = LoggerFactory.getLogger(ExpensifyService.class);
  private static final Map<String, ExpensifyAuth> EXPENSIFY_AUTH_MAP = new HashMap<>();
  private static final String SORRY = "Sorry, I didn't get what you said";
  private static final String NOT_AUTHENTICATED = "Sorry, You are not authenticated yet";
  private static final String NO_AMOUNT = "Sorry, I could not extract any amount of what you just said";
  private static final String TOO_MANY_AMOUNTS = "Sorry, You last sentense seems to contains multiple amounts";

  private TemplateService templateService;

  private TextAnalyser analyser = new TextAnalyser();

  @Client("https://integrations.expensify.com/Integration-Server/ExpensifyIntegrations")
  @Inject
  RxHttpClient httpClient;


  public ExpensifyService(TemplateService templateService) {
    this.templateService = templateService;
  }

  public String processMessage(InboundMessage message) {
    if (analyser.isAuthAction(message.getMessageText())) {
      List<String> tokens = analyser.extractAuthTokens(message.getMessageText());
      if (tokens.size() == 2) {
        LOG.info("User {} just sent his authentication tokens", message.getUser().getEmail());
        EXPENSIFY_AUTH_MAP.put(message.getUser().getEmail(), new ExpensifyAuth(tokens.get(0), tokens.get(1)));
        return "You are now authenticated on Expensify";
      }
    } else if (analyser.isExpenseAction(message.getMessageText())) {
      String email = message.getUser().getEmail();
      String messageText = message.getMessageText();
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
    }
    return SORRY;
  }


  private void postExpense(String email, ExpensifyAuth auth, int amount, String merchant) {
    String expensePayload = templateService.expensePayload(email, auth, amount, merchant);

    MultipartBody requestBody = MultipartBody.builder()
            .addPart(
                    "requestJobDescription",
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

}

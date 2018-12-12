package expensify.bot.utils;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.DefaultHttpClient;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.multipart.MultipartBody;
import io.reactivex.Flowable;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ExpensifyApiDebugTest {

  //@Test
  public void testGenerateReport() throws IOException {
    HttpPost httpPost = new HttpPost("https://integrations.expensify.com/Integration-Server/ExpensifyIntegrations");

    String payloadString = "{\n"
        + "        \"type\":\"file\",\n"
        + "         \"credentials\":{\n"
        + "            \"partnerUserID\":\"REPLACE-ME\",\n"
        + "            \"partnerUserSecret\":\"REPLACE-ME\"\n"
        + "        },\n"
        + "        \"onReceive\":{\n"
        + "            \"immediateResponse\":[\"returnRandomFileName\"]\n"
        + "        },\n"
        + "        \"inputSettings\":{\n"
        + "            \"type\":\"combinedReportData\",\n"
        + "            \"filters\":{\n"
        + "                \"startDate\":\"2018-12-01\"\n"
        + "            },\n"
        + "            \"reportState\":\"OPEN,SUBMITTED,APPROVED\",\n"
        + "            \"limit\":\"10\"\n"
        + "        },\n"
        + "        \"outputSettings\":{\n"
        + "            \"fileExtension\":\"csv\"\n"
        + "        }\n"
        + "    }";



    String template = "<#if addHeader == true>\n"
        + "    Merchant,Original Amount,Currency,Date,Category,Report number,Expense number<#lt>\n"
        + "</#if>\n"
        + "<#assign reportNumber = 1>\n"
        + "<#assign expenseNumber = 1>\n"
        + "<#list reports as report>\n"
        + "    <#list report.transactionList as expense>\n"
        + "        ${expense.merchant},<#t>\n"
        + "        <#-- note: expense.amount prints the original amount only -->\n"
        + "        ${expense.amount},<#t>\n"
        + "        ${expense.currency},<#t>\n"
        + "        ${expense.created},<#t>\n"
        + "        ${expense.category},<#t>\n"
        + "        ${reportNumber},<#t>\n"
        + "        ${expenseNumber}<#lt>\n"
        + "        <#assign expenseNumber = expenseNumber + 1>\n"
        + "    </#list>\n"
        + "    <#assign reportNumber = reportNumber + 1>\n"
        + "</#list>";


    HttpEntity entity = MultipartEntityBuilder.create()
        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
        .addTextBody("template", template)
        .addTextBody("requestJobDescription", payloadString)
        .build();
    httpPost.setEntity(entity);

    CloseableHttpClient httpclient = HttpClients.createDefault();
    CloseableHttpResponse response = httpclient.execute(httpPost);
    System.out.println("status "+response.getStatusLine().getStatusCode());
    InputStream content = response.getEntity().getContent();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    IOUtils.copy(content, bos);
    String reportName = new String(bos.toByteArray());
    System.out.println("Body : "+reportName);


    String downloadPayload = "{\n"
        + "        \"type\":\"download\",\n"
        + "        \"credentials\":{\n"
        + "            \"partnerUserID\":\"aa_jeremy_passeron_symphony_com\",\n"
        + "            \"partnerUserSecret\":\"c84fc74ec6815a34c070d328968570ea596e8db6\"\n"
        + "        },\n"
        + "        \"fileName\":\"%s\",\n"
        + "        \"fileSystem\":\"integrationServer\"\n"
        + "    }";

    String readyPayload = String.format(downloadPayload, reportName);

    HttpEntity entityReport = MultipartEntityBuilder.create()
        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
        .addTextBody("requestJobDescription", readyPayload)
        .build();
    httpPost.setEntity(entityReport);

    CloseableHttpResponse downloadResponse = httpclient.execute(httpPost);
    System.out.println("code "+downloadResponse.getStatusLine().getStatusCode());
    InputStream csvStream = downloadResponse.getEntity().getContent();
    ByteArrayOutputStream csvBos = new ByteArrayOutputStream();
    IOUtils.copy(csvStream, csvBos);
    System.out.println(new String(csvBos.toByteArray()));


  }

}

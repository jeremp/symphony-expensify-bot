package expensify.bot.utils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextAnalyser {

  public static final String ACTION_AUTH = "auth";
  public static final String ACTION_HELP = "help";
  public static final String ACTION_EXPENSE = "expense";

  private static final Pattern AMOUNT_PATTERN = Pattern.compile("[ ]{1}([0-9]){1,2}[\\.\\,]{0,1}([0-9]){0,2}[ ]{0,1}[\\$\\€]");

  public boolean isAuthAction(String message){
    return ACTION_AUTH.equals(extractAction(message));
  }

  public boolean isExpenseAction(String message){
    return ACTION_EXPENSE.equals(extractAction(message));
  }

  public boolean isHelpAction(String message){
    return ACTION_HELP.equals(extractAction(message));
  }

  public List<String> extractAuthTokens(String message){
    // we already know that it's a auth action
    String right = StringUtils.substring(message.trim(), ACTION_AUTH.length());
    String[] split = StringUtils.split(right, ' ');
    List<String> result = new ArrayList<>();
    for(String s : split){
      if(StringUtils.isNotBlank(s)){
        result.add(s.trim());
      }
    }
    return result;
  }

  public List<BigDecimal> extractAmount(String message){
    List<BigDecimal> res = new ArrayList<>();
    Matcher matcher = AMOUNT_PATTERN.matcher(message);
    while (matcher.find()) {
      String value = matcher.group();
      value = StringUtils.replaceAll(value, "\\$", "");
      value = StringUtils.replaceAll(value, "\\€", "");
      value = StringUtils.replaceAll(value, "\\,", ".");
      value = value.trim();
      res.add(new BigDecimal(value));
    }
    return res;
  }

  public String extractMerchant(String message){
    String result = message.trim();
    result = StringUtils.removeFirst(result, ACTION_EXPENSE);
    // let's remove the amounts we find
    Matcher matcher = AMOUNT_PATTERN.matcher(message);
    while (matcher.find()) {
      result = StringUtils.removeFirst(result, matcher.group());
    }
    return result.trim();
  }

  private String extractAction(String message){
    if(StringUtils.isNotBlank(message)){
      String[] split = wordSplit(message);
      if(split.length>0){
       return split[0];
      }
    }
    return null ;
  }

  private String[] wordSplit(String message) {
    String res = message.trim();
    return StringUtils.split(res, ' ');
  }


}

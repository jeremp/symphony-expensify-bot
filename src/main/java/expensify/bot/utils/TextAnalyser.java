package expensify.bot.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TextAnalyser {

  public static final String ACTION_AUTH = "auth";
  public static final String ACTION_EXPENSE = "expense";


  public boolean isAuthAction(String message){
    return ACTION_AUTH.equals(extractAction(message));
  }

  public boolean isExpenseAction(String message){
    return ACTION_EXPENSE.equals(extractAction(message));
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

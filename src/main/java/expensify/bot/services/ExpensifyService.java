package expensify.bot.services;

import expensify.bot.utils.ExpensifyAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ExpensifyService {

  private static final Logger LOG = LoggerFactory.getLogger(BotService.class);

  private static final Map<String, ExpensifyAuth> EXPENSIFY_AUTH_MAP = new HashMap<>();

  public void hey(){
    System.out.println("---> HEY");
  }

  public void auth(String userEmail, String partnerUserID, String partnerUserSecret){
    EXPENSIFY_AUTH_MAP.put(userEmail, new ExpensifyAuth(partnerUserID, partnerUserSecret));
  }

}

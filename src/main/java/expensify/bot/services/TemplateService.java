package expensify.bot.services;

import expensify.bot.utils.ExpensifyAuth;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class TemplateService {

  private static final JtwigTemplate EXP_TEMPLATE = JtwigTemplate.classpathTemplate("template/requestJobDescription.twig");
  private static final JtwigTemplate HELP_TEMPLATE = JtwigTemplate.classpathTemplate("template/msg_help.twig");

  private static final Logger LOG = LoggerFactory.getLogger(ExpensifyService.class);

  public String expensePayload(String email, ExpensifyAuth auth, int amount, String merchant){
    JtwigModel model = JtwigModel.newModel()
            .with("email", email)
            .with("auth", auth)
            .with("amount", amount)
            .with("merchant", merchant);

    return EXP_TEMPLATE.render(model);
  }

  public String getHelpMessage(String email){
    JtwigModel model = JtwigModel.newModel().with("email", email);
    return HELP_TEMPLATE.render(model);
  }



}

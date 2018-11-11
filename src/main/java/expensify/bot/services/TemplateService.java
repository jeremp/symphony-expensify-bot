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

  private static final Logger LOG = LoggerFactory.getLogger(ExpensifyService.class);

  public String expensePayload(String email, ExpensifyAuth auth, int amount){
    JtwigModel model = JtwigModel.newModel()
            .with("email", email)
            .with("auth", auth)
            .with("amount", amount);

    return EXP_TEMPLATE.render(model);
  }



}

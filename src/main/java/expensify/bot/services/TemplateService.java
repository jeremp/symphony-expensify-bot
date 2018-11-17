package expensify.bot.services;

import expensify.bot.utils.ExpensifyAuth;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Singleton
public class TemplateService {

  private static final JtwigTemplate EXP_TEMPLATE = JtwigTemplate.classpathTemplate("template/requestJobDescription.twig");
  private static final JtwigTemplate HELP_TEMPLATE = JtwigTemplate.classpathTemplate("template/msg_help.twig");
  private static final JtwigTemplate DAILY_REMINDER_TEMPLATE = JtwigTemplate.classpathTemplate("template/daily_reminder.twig");

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private static final Logger LOG = LoggerFactory.getLogger(TemplateService.class);

  public String expensePayload(String email, ExpensifyAuth auth, int amount, String merchant, Date date){
    JtwigModel model = JtwigModel.newModel()
            .with("email", email)
            .with("auth", auth)
            .with("amount", amount)
            .with("merchant", merchant)
            .with("expenseDate", formatDate(date));


    return EXP_TEMPLATE.render(model);
  }

  public String getHelpMessage(String email){
    JtwigModel model = JtwigModel.newModel().with("email", email);
    return HELP_TEMPLATE.render(model);
  }

  public String getReminderMessage(String email){
    JtwigModel model = JtwigModel.newModel().with("email", email);
    return DAILY_REMINDER_TEMPLATE.render(model);
  }

  public String formatDate(Date date){
    LocalDate localDate = Instant.ofEpochMilli(date.getTime())
            .atZone(ZoneId.systemDefault())
            .toLocalDate();

    return DATE_TIME_FORMATTER.format(localDate);

  }

}

package expensify.bot.jobs;

import expensify.bot.services.ReminderService;
import io.micronaut.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class ExpenseReminderJob {

  private static final Logger LOG = LoggerFactory.getLogger(ExpenseReminderJob.class);

  private final ReminderService reminderService ;

  public ExpenseReminderJob(ReminderService reminderService) {
    this.reminderService = reminderService;
  }

  @Scheduled(fixedDelay = "850s", initialDelay = "30s")
  public void remindUsersForExpenses(){
    LOG.info("remindUsersForExpenses ...");
    reminderService.remindActiveUsers();
    LOG.info("remindUsersForExpenses [DONE]");
  }

}

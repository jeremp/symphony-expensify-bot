package expensify.bot.services;

import clients.symphony.api.StreamsClient;
import model.OutboundMessage;
import model.StreamListItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
public class ReminderService {

  private static final Logger LOG = LoggerFactory.getLogger(ReminderService.class);

  private final BotService botService;
  private final ExpensifyService expensifyService;
  private final TemplateService templateService;

  public ReminderService(BotService botService, ExpensifyService expensifyService, TemplateService templateService) {
    this.botService = botService;
    this.expensifyService = expensifyService;
    this.templateService = templateService;
  }

  public long remindActiveUsers(){
    int result = -1 ;
    Map<Long, String> activeUsers = expensifyService.getActiveUsers();
    StreamsClient streamsClient = botService.getBotClient().getStreamsClient();
    List<StreamListItem> activeIM = streamsClient.getUserStreams(Arrays.asList("IM"), false);
    LOG.info("We have {} active users in {} actives IM", activeUsers.size(), activeIM.size());

    long botId = botService.getBotClient().getBotUserInfo().getId();

    for(StreamListItem stream : activeIM){
      Optional<Long> userId = stream.getStreamAttributes().getMembers().stream().filter(uId -> uId != botId).findFirst();
      if(userId.isPresent()){
        if(activeUsers.containsKey(userId.get())) {
          remindUser(stream.getId(), activeUsers.get(userId.get()));
        }else{
          LOG.debug("{} is not an active user", userId.get());
        }
      }else{
        LOG.warn("Cannot find another user in IM {}", stream.getId());
      }
    }

    return result;
  }

  private void remindUser(String streamId, String email){
    String reminderMessage = templateService.getReminderMessage(email);
    OutboundMessage message = new OutboundMessage();
    message.setMessage(reminderMessage);
    this.botService.getBotClient().getMessagesClient().sendMessage(streamId, message);
  }


}

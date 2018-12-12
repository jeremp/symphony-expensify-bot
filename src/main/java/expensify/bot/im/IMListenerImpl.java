package expensify.bot.im;

import clients.SymBotClient;
import expensify.bot.services.ExpensifyService;
import listeners.IMListener;
import model.InboundMessage;
import model.OutboundMessage;
import model.Stream;
import org.apache.commons.lang3.StringUtils;

public class IMListenerImpl implements IMListener {

  private SymBotClient botClient;
  private ExpensifyService expensifyService;

  public IMListenerImpl(SymBotClient botClient, ExpensifyService expensifyService) {
    this.botClient = botClient;
    this.expensifyService = expensifyService;
  }

  public void onIMMessage(InboundMessage inboundMessage) {
    OutboundMessage messageOut = new OutboundMessage();
    String response = expensifyService.processMessage(inboundMessage);
    if(response != null && StringUtils.isNotBlank(response)) {
      messageOut.setMessage(response);

    try {
      this.botClient.getMessagesClient().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
    } catch (Exception e) {
      e.printStackTrace();
    }
    }
  }

  public void onIMCreated(Stream stream) {

  }
}
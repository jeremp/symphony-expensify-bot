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
      if(response.lastIndexOf("_DATA_")==-1) {
        messageOut.setMessage(response);
      }else{
        int dataIndex = response.lastIndexOf("_DATA_");
        String data = "{ \"data\":" + response.substring(dataIndex+6) +"}";
        String messageMl = response.substring(0, dataIndex);
        messageOut.setMessage(messageMl);
        messageOut.setData(data);
      }
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
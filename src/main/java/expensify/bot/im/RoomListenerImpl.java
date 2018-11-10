package expensify.bot.im;

import clients.SymBotClient;
import expensify.bot.utils.SymUtils;
import listeners.RoomListener;
import model.InboundMessage;
import model.OutboundMessage;
import model.Stream;
import model.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomListenerImpl implements RoomListener {

  private SymBotClient botClient;
  private String botUsername ;

  public RoomListenerImpl(SymBotClient botClient) {
    this.botClient = botClient;
    this.botUsername = SymUtils.getConfig().getBotUsername();
  }

  private final Logger logger = LoggerFactory.getLogger(RoomListenerImpl.class);

  public void onRoomMessage(InboundMessage inboundMessage) {
  }

  public void onRoomCreated(RoomCreated roomCreated) {

  }

  public void onRoomDeactivated(RoomDeactivated roomDeactivated) {

  }

  public void onRoomMemberDemotedFromOwner(RoomMemberDemotedFromOwner roomMemberDemotedFromOwner) {

  }

  public void onRoomMemberPromotedToOwner(RoomMemberPromotedToOwner roomMemberPromotedToOwner) {

  }

  public void onRoomReactivated(Stream stream) {

  }

  public void onRoomUpdated(RoomUpdated roomUpdated) {

  }

  public void onUserJoinedRoom(UserJoinedRoom userJoinedRoom) {
    OutboundMessage messageOut = new OutboundMessage();
    if(userJoinedRoom.getAffectedUser().getUsername().equals(this.botUsername)){
      messageOut.setMessage("Hi All, I should only be added to IM, not ROOMs.");
      try {
        this.botClient.getMessagesClient().sendMessage(userJoinedRoom.getStream().getStreamId(), messageOut);
      } catch (Exception e) {
        logger.error("failed to post message in a room", e);
      }
    }
  }

  public void onUserLeftRoom(UserLeftRoom userLeftRoom) {

  }
}

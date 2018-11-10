package expensify.bot.services;

import authentication.ISymAuth;
import authentication.SymBotRSAAuth;
import clients.SymBotClient;
import configuration.SymConfig;
import configuration.SymConfigLoader;
import expensify.bot.im.IMListenerImpl;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.discovery.event.ServiceStartedEvent;
import io.micronaut.scheduling.annotation.Async;
import listeners.IMListener;
import listeners.RoomListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.DatafeedEventsService;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.net.URL;

@Singleton
public class BotService implements ApplicationEventListener<ServiceStartedEvent> {

  private static final Logger LOG = LoggerFactory.getLogger(BotService.class);

  private ExpensifyService expensifyService ;

  private boolean authenticated = false ;

  public BotService(ExpensifyService expensifyService) {
    this.expensifyService = expensifyService;
  }

  @Async
  @Override
  public void onApplicationEvent(final ServiceStartedEvent event) {
    LOG.info("Authenticate pod against the pod");
    if(!authenticated) {
      authenticate();
    }
    LOG.info("DONE");
  }

  private synchronized void authenticate(){
    authenticated = true ;
    URL url = getClass().getResource("/config.json");
    SymConfigLoader configLoader = new SymConfigLoader();
    SymConfig config = configLoader.loadFromFile(url.getPath());
    ISymAuth botAuth = new SymBotRSAAuth(config);
    botAuth.authenticate();
    SymBotClient botClient = SymBotClient.initBot(config, botAuth);
    DatafeedEventsService datafeedEventsService = botClient.getDatafeedEventsService();
    //RoomListener roomListenerTest = new RoomListenerTestImpl(botClient);
    //datafeedEventsService.addRoomListener(roomListenerTest);
    IMListener imListener = new IMListenerImpl(botClient, expensifyService);
    datafeedEventsService.addIMListener(imListener);

  }

}

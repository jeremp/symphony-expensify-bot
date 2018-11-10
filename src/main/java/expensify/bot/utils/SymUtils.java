package expensify.bot.utils;

import configuration.SymConfig;
import configuration.SymConfigLoader;

import java.net.URL;

public class SymUtils {

  private static SymConfig config ;

  public static SymConfig getConfig(){
    if(config!=null){
      return config;
    }else {
      URL url = SymUtils.class.getResource("/config.json");
      SymConfigLoader configLoader = new SymConfigLoader();
      config = configLoader.loadFromFile(url.getPath());
      return config;
    }
  }

}

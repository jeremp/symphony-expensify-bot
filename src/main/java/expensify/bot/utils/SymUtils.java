package expensify.bot.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import configuration.SymConfig;
import configuration.SymConfigLoader;
import expensify.bot.services.ExpensifyService;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SymUtils {

  private static ObjectMapper MAPPER = new ObjectMapper();
  private static SymConfig CONFIG;
  private static final Logger LOG = LoggerFactory.getLogger(ExpensifyService.class);

  public static SymConfig getConfig() {
    if (CONFIG != null) {
      return CONFIG;
    } else {
      InputStream is = null;
      try {
        is = SymUtils.class.getResourceAsStream("/config.json");
        return MAPPER.readValue(is, SymConfig.class);

      } catch (IOException e) {
        LOG.error("Error reading config", e);
      } finally {
        if (is != null) {
          IOUtils.closeQuietly(is);
        }
      }
      return null ;
    }
  }

}

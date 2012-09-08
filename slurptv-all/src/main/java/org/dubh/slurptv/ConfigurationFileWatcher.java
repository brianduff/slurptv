package org.dubh.slurptv;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Logger;

import org.dubh.slurptv.ConfigurationModule.ConfigurationFile;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;

/**
 * Watch the config file for changes.
 * @author brianduff
 */
class ConfigurationFileWatcher extends AbstractExecutionThreadService {
  private static final Logger log = Logger.getLogger(ConfigurationFileWatcher.class.getName());
  private final @ConfigurationFile File configFile;
  private final ConfigurationManager configManager;
  private WatchService watchService;
  
  @Inject
  ConfigurationFileWatcher(@ConfigurationFile File configFile, ConfigurationManager configManager) {
    this.configFile = configFile;
    this.configManager = configManager;
  }
  
  @Override
  protected void startUp() throws Exception {
    this.watchService = FileSystems.getDefault().newWatchService();
    Path dir = configFile.getParentFile().toPath();
    dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
    log.info("Started watching " + dir);
  }

  @Override
  protected void run() throws Exception {
    while (isRunning()) {
      WatchKey key = watchService.take();

      for (WatchEvent<?> event : key.pollEvents()) {
        WatchEvent.Kind<?> kind = event.kind();
        if (kind == StandardWatchEventKinds.OVERFLOW) {
          log.warning("Overflow event: " + event);
          // TODO(bduff) handle this.
          continue;
        }
        
        @SuppressWarnings("unchecked")
        Path filename = ((WatchEvent<Path>) event).context();
        if (filename.toString().equals(configFile.getName())) {
          log.info("Detected filesystem change for configuration file");
          configManager.forceLoadConfiguration();
        }
      }
      
      boolean valid = key.reset();
      if (!valid) {
        stop();
      }
    }
  }
}

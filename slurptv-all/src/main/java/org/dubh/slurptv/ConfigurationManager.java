package org.dubh.slurptv;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.dubh.easynews.slurptv.SlurpTv.Configuration;
import org.dubh.slurptv.ConfigurationModule.ConfigurationFile;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.TextFormat;

/**
 * Manages access to configuration state.
 * 
 * @author brianduff
 */
@Singleton
@ThreadSafe
public class ConfigurationManager {
  private static final Logger log = Logger.getLogger(ConfigurationManager.class.getName());
  private final File configFile;
  private final Object lock = new Object();
  private @Nullable Configuration configuration;
  private final EventBus eventBus;
  
  @Inject
  ConfigurationManager(@ConfigurationFile File configFile, EventBus eventBus) {
    this.configFile = configFile;
    this.eventBus = eventBus;
  }

  /**
   * Returns the current configuration.
   * @return
   */
  public Configuration getConfiguration() {
    synchronized (lock) {
      if (configuration == null) {
        configuration = loadConfiguration();
      }
      return configuration;
    }
  }
  
  /**
   * Force configuration to be loaded.
   * @return
   */
  private Configuration loadConfiguration() {
    synchronized (lock) {
      Configuration.Builder configBuilder = Configuration.newBuilder();
      if (configFile.exists()) {
        try (Reader r = Files.newReader(configFile, Charsets.UTF_8)) {
          TextFormat.merge(r, configBuilder);
        } catch (IOException e) {
          throw Throwables.propagate(e);
        }
        log.info("Loaded configuration from file " + configFile);
      }
      return configBuilder.build();
    }
  }
  
  Configuration forceLoadConfiguration() {
    synchronized (lock) {
      Configuration old = getConfiguration();
      configuration = loadConfiguration();
      fireConfigurationChanged(old);
      return configuration;
    }
  }

  private void fireConfigurationChanged(Configuration old) {
    if (old.equals(configuration)) {
      return;
    }
    eventBus.post(new ConfigurationChangedEvent(old, configuration));
  }
  
  public Configuration setConfiguration(Configuration configuration) {
    Configuration current = null;
    synchronized (lock) {
      current = getConfiguration();
      if (current.getVersion() != configuration.getVersion()) {
        throw new IllegalStateException("Conflict updating configuration.");
      }
      Configuration versioned = Configuration.newBuilder(configuration)
          .setVersion(configuration.getVersion() + 1)
          .build();
      writeConfiguration(versioned);
      this.configuration = versioned;
      fireConfigurationChanged(current);
      return this.configuration;
    }
  }
  
  private void writeConfiguration(Configuration config) {
    try (Writer w = Files.newWriter(configFile, Charsets.UTF_8)) {
      TextFormat.print(config, w);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    log.info("Wrote configuration file version " + config.getVersion());
  }
}

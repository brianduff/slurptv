package org.dubh.slurptv;

import org.dubh.easynews.slurptv.SlurpTv.Configuration;

/**
 * An event fired on the event bus to indicate that configuration changed.
 * @author brianduff
 */
public class ConfigurationChangedEvent {
  private final Configuration oldConfig;
  private final Configuration newConfig;
  
  ConfigurationChangedEvent(Configuration oldConfig, Configuration newConfig) {
    this.oldConfig = oldConfig;
    this.newConfig = newConfig;
  }
  
  public Configuration getOldConfig() {
    return oldConfig;
  }
  
  public Configuration getNewConfig() {
    return newConfig;
  }
}

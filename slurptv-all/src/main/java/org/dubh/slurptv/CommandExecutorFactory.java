package org.dubh.slurptv;

import java.io.File;

import org.dubh.easynews.slurptv.SlurpTv.Configuration;

import com.google.inject.Inject;
import com.google.inject.Provider;

class CommandExecutorFactory {
  private final Provider<Configuration> configuration;

  @Inject
  CommandExecutorFactory(Provider<Configuration> configuration) {
    this.configuration = configuration;
  }

  public CommandExecutor newExecutor(String taskName) {
    return new CommandExecutor(new File(configuration.get().getTempDir()), taskName);
  }
}

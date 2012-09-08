package org.dubh.slurptv;

import java.io.File;

import org.dubh.slurptv.ConfigurationModule.ConfiguredDirectory;
import org.dubh.slurptv.ConfigurationModule.Directory;

import com.google.inject.Inject;

class CommandExecutorFactory {
  private final File tempDir;

  @Inject
  CommandExecutorFactory(@ConfiguredDirectory(Directory.TEMP) File tempDir) {
    this.tempDir = tempDir;
  }

  public CommandExecutor newExecutor(String taskName) {
    return new CommandExecutor(tempDir, taskName);
  }
}

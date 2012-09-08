package org.dubh.slurptv;

import java.io.File;
import java.io.IOException;

import org.dubh.easynews.slurptv.SlurpTv.Configuration;
import org.dubh.easynews.slurptv.SlurpTv.Show;
import org.dubh.easynews.slurptv.State.EpisodeState;
import org.dubh.easynews.slurptv.State.EpisodeState.Step;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Provider;

class CopyToITunesTask extends AbstractTask {
  private final Provider<Configuration> configuration;

  @Inject
  CopyToITunesTask(Provider<Configuration> configuration) {
    this.configuration = configuration;
  }

  @Override
  public EpisodeState perform(Show show, EpisodeState previousState) throws TaskFailedException,
      InterruptedException {
    File originalFile = new File(previousState.getConvertedFile());
    File itunesFile = new File(configuration.get().getItunesAutoDir(), originalFile.getName());
    // Should we use move?
    try {
      Files.copy(originalFile, itunesFile);
      return EpisodeState.newBuilder(previousState).setLastCompletedStep(Step.COPYING_TO_ITUNES)
          .setItunesFile(itunesFile.getPath()).build();
    } catch (IOException e) {
      throw new TaskFailedException("Failed to copy from " + originalFile + " to " + itunesFile);
    }
  }

}

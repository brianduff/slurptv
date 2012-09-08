package org.dubh.slurptv;

import java.io.File;
import java.io.IOException;

import org.dubh.easynews.slurptv.SlurpTv.Configuration;
import org.dubh.easynews.slurptv.SlurpTv.Show;
import org.dubh.easynews.slurptv.State.EpisodeState;
import org.dubh.easynews.slurptv.State.EpisodeState.Step;

import com.google.common.io.Files;
import com.google.inject.Inject;

class CopyToITunesTask extends AbstractTask {
  private final Configuration configuration;

  @Inject
  CopyToITunesTask(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public EpisodeState perform(Show show, EpisodeState previousState) throws TaskFailedException,
      InterruptedException {
    File originalFile = new File(previousState.getConvertedFile());
    File itunesFile = new File(configuration.getItunesAutoDir(), originalFile.getName());
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

package org.dubh.slurptv;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.dubh.easynews.slurptv.SlurpTv.Configuration;
import org.dubh.easynews.slurptv.SlurpTv.Show;
import org.dubh.easynews.slurptv.State.EpisodeState;
import org.dubh.easynews.slurptv.State.EpisodeState.Step;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Converts a movie to MP4 format using HandBrake.
 * 
 * @author brianduff
 */
class ConvertToMp4Task extends AbstractTask {
  private static final Logger log = Logger.getLogger(ConvertToMp4Task.class.getName());
  private static final String HANDBRAKE_CLI = "/Applications/HandBrakeCLI";
  private final CommandExecutorFactory executorFactory;
  private final Provider<Configuration> configuration;

  @Inject
  ConvertToMp4Task(Provider<Configuration> configuration, CommandExecutorFactory executorFactory) {
    this.configuration = configuration;
    this.executorFactory = executorFactory;
  }

  private void convert(File inputFile, File outputFile) throws IOException, InterruptedException {
    log.info("Converting to mp4: " + inputFile);
    executorFactory.newExecutor("convert").execute(
        new String[] { HANDBRAKE_CLI, "-i", inputFile.getPath(), "-o", outputFile.getPath(),
            "--preset=\"AppleTV 2\"", }, inputFile.getName());
  }

  @Override
  public EpisodeState perform(Show show, EpisodeState previousState) throws TaskFailedException,
      InterruptedException {
    File inputFile = new File(previousState.getDownloadFile());
    int lastDot = inputFile.getName().lastIndexOf('.');
    String outputFileName = inputFile.getName().substring(0, lastDot) + ".m4v";
    File mp4Dir = new File(configuration.get().getMp4Dir());
    File convertedFile = new File(mp4Dir, outputFileName);

    mp4Dir.mkdirs();
    try {
      convert(inputFile, convertedFile);
      return EpisodeState.newBuilder(previousState).setLastCompletedStep(Step.CONVERTING)
          .setConvertedFile(convertedFile.getPath()).build();
    } catch (IOException e) {
      throw new TaskFailedException("Failed to convert", e);
    }
  }
}

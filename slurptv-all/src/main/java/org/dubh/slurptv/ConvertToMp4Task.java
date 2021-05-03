package org.dubh.slurptv;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.dubh.easynews.slurptv.SlurpTv.Configuration;
import org.dubh.easynews.slurptv.SlurpTv.Show;
import org.dubh.easynews.slurptv.State.EpisodeState;
import org.dubh.easynews.slurptv.State.Step;
import org.dubh.slurptv.CommandExecutor.OutputType;

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
    CompletionWatcher watcher = new CompletionWatcher();
    
    // HandBrakeCLI will return a successful exit code even if it fails to finish the conversion
    // (e.g. if it's abruptly terminated via Ctrl+C). We watch the process and look for specific
    // output to know for sure that it's really done.
    executorFactory.newExecutor("convert").setOutputProcessor(watcher).execute(
        new String[] { HANDBRAKE_CLI, "-i", inputFile.getPath(), "-o", outputFile.getPath(),
            "-Z", "AppleTV 2" }, inputFile.getName());
    
    if (!watcher.isSuccessful()) {
      throw new IOException("Failed to get successful encode status output from HandbrakeCLI");
    }
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
  
  private static class CompletionWatcher implements CommandExecutor.OutputProcessor {
    private static final Object lock = new Object();
    private boolean isDone;
    private boolean isSuccessful;

    @Override
    public void processLine(OutputType type, String line) {
      if (line.startsWith("Encode ")) {
        synchronized (lock) {
          if (line.contains("done!")) {
            isDone = true;
            isSuccessful = true;
          } else if (line.contains("failed") || line.contains("canceled")) {
            isDone = true;
            isSuccessful = false;
          }
        }
      }
    }
    
    boolean isSuccessful() {
      synchronized (lock) {
        return isDone && isSuccessful;
      }
    }
  }
}

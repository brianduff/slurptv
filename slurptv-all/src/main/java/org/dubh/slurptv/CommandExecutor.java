package org.dubh.slurptv;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Collection;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

public final class CommandExecutor {
  private final File logDir;
  private final String name;
  private OutputProcessor outputProcessor = new OutputProcessor() {
    @Override
    public void processLine(OutputType type, String line) {
      // No-op
    }    
  };

  CommandExecutor(File logDir, String name) {
    this.logDir = logDir;
    this.name = name;
  }
  
  public CommandExecutor setOutputProcessor(OutputProcessor processor) {
    this.outputProcessor = Preconditions.checkNotNull(processor);
    return this;
  }

  public void execute(Collection<String> command, String label) throws IOException,
      InterruptedException {
    execute(command.toArray(new String[command.size()]), label);
  }

  public void execute(String[] command, String label) throws IOException, InterruptedException {
    logDir.mkdirs();
    Process process = Runtime.getRuntime().exec(command);
    File outLog = new File(logDir, name + "." + label + "_out.log");
    File errLog = new File(logDir, name + "." + label + "_err.log");
    try (Streamer out = new Streamer(OutputType.STDOUT, process.getInputStream(), outLog);
        Streamer err = new Streamer(OutputType.STDERR, process.getErrorStream(), errLog)) {
      Thread tOut = new Thread(out);
      tOut.setPriority(Thread.MIN_PRIORITY);
      tOut.start();
  
      Thread tErr = new Thread(err);
      tErr.setPriority(Thread.MIN_PRIORITY);
      tErr.start();
 
      if (process.waitFor() != 0) {
        throw new IOException("Error from " + name + "." + label + ". Command="
            + Joiner.on(' ').join(command));
      }
      // Wait until we've written everything.
      tOut.join();
      tErr.join();
    }
    
    if (outLog.length() == 0) {
      outLog.delete();
    }
    if (errLog.length() == 0) {
      errLog.delete();
    }
  }

  private class Streamer implements Runnable, AutoCloseable {  
    // Intentionally rather short so that we can report progress more accurately.
    private static final int STREAM_BUFFER_SIZE = 50;
    private final OutputType outputType;
    private final BufferedReader reader;
    private final Writer writer;

    Streamer(OutputType outputType, InputStream in, File out) throws IOException {
      this.outputType = outputType;
      reader = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8),
          STREAM_BUFFER_SIZE);      
      writer = Files.newWriter(out, Charsets.UTF_8);
    }

    @Override
    public void run() {
      try {
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          writer.write(line + "\n");
          outputProcessor.processLine(outputType, line);
        }
      } catch (IOException e) {
        Throwables.propagate(e);
      }
    }

    @Override
    public void close() throws IOException {
      Closeables.closeQuietly(reader);
      Closeables.closeQuietly(writer);
    }
  }
  
  public enum OutputType {
    STDOUT,
    STDERR
  }
  
  public interface OutputProcessor {
    void processLine(OutputType type, String line);
  }
}

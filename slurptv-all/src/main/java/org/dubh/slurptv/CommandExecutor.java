package org.dubh.slurptv;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import com.google.common.base.Joiner;

public final class CommandExecutor {
  private final File logDir;
  private final String name;

  public CommandExecutor(File logDir, String name) {
    this.logDir = logDir;
    this.name = name;
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
    Streamer out = new Streamer(process.getInputStream(), outLog);
    Streamer err = new Streamer(process.getErrorStream(), errLog);

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

    if (outLog.length() == 0) {
      outLog.delete();
    }
    if (errLog.length() == 0) {
      errLog.delete();
    }
  }

  private class Streamer implements Runnable, Closeable {
    private final BufferedOutputStream os;
    private final BufferedInputStream is;
    private final byte[] buffer = new byte[512];

    Streamer(InputStream in, File out) throws IOException {
      os = new BufferedOutputStream(new FileOutputStream(out));
      is = new BufferedInputStream(in);
    }

    @Override
    public void run() {
      try {
        int read = is.read(buffer);
        while (read != -1) {
          os.write(buffer, 0, read);
          read = is.read(buffer);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void close() throws IOException {
      try {
        is.close();
      } finally {
        os.close();
      }
    }
  }
}

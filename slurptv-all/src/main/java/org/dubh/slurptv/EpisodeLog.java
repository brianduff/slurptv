package org.dubh.slurptv;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.dubh.easynews.slurptv.Data.Episode;
import org.dubh.easynews.slurptv.SlurpTv.Show;

import com.google.inject.Inject;

/**
 * Log per-episode information.
 * 
 * @author brianduff
 */
class EpisodeLog {
  private final EpisodeFormatter formatter;

  @Inject
  EpisodeLog(EpisodeFormatter formatter) {
    this.formatter = formatter;
  }

  public LogRecord warning(Show show, Episode episode, String message) {
    return new EpisodeLogRecord(Level.WARNING, message, show, episode);
  }

  public LogRecord info(Show show, Episode episode, String message) {
    return new EpisodeLogRecord(Level.INFO, message, show, episode);
  }

  public LogRecord severe(Show show, Episode episode, String message) {
    return new EpisodeLogRecord(Level.SEVERE, message, show, episode);
  }

  public LogRecord severe(Show show, Episode episode, String message,
      Throwable t) {
    LogRecord record = severe(show, episode, message);
    record.setThrown(t);
    return record;
  }

  private class EpisodeLogRecord extends LogRecord {
    private static final long serialVersionUID = -501583018483854300L;

    public EpisodeLogRecord(Level level, String message, Show show,
        Episode episode) {
      super(level, "[" + show.getId() + ":" + formatter.format(episode) + "] "
          + message);
    }
  }
}

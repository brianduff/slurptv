package org.dubh.slurptv;

import org.dubh.easynews.slurptv.Data.EpisodeDate;
import org.joda.time.DateTime;

public class DateConverter {
  public DateTime toDateTime(EpisodeDate date) {
    return new DateTime(date.getYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0);
  }
}

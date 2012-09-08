package org.dubh.slurptv.easynews;

import org.dubh.easynews.slurptv.Data.Episode;
import org.dubh.slurptv.DateConverter;
import org.dubh.slurptv.EpisodeFormatter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Query {
  // This pattern matches the following:
  // 2012-08-30
  // 2012.08.30
  // 30.08.2012
  // 30-08-2012
  private static final DateTimeFormatter format = DateTimeFormat
      .forPattern("(yyyy[\\.\\-]MM[\\.\\-]dd|dd[\\.\\-]MM[\\.\\-]yyyy)");
  private String showTitle;
  private Episode episode;

  public Query setShowTitle(String showTitle) {
    this.showTitle = showTitle;
    return this;
  }

  public String getShowTitle() {
    return showTitle;
  }

  public Query setEpisode(Episode episode) {
    this.episode = episode;
    return this;
  }

  public Episode getEpisode() {
    return episode;
  }

  String getSearch() {
    String baseQuery = "autounrar " + getShowTitle() + " ";
    if (episode.hasDate()) {
      DateTime date = new DateConverter().toDateTime(episode.getDate());
      return baseQuery + format.print(date);
    } else {
      return baseQuery + new EpisodeFormatter().format(episode);
    }
  }
}

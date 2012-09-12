package org.dubh.slurptv;

import java.io.File;
import java.io.IOException;

import org.dubh.easynews.slurptv.Data.Episode;
import org.dubh.easynews.slurptv.Data.EpisodeDetails;
import org.dubh.easynews.slurptv.SlurpTv.Show;
import org.dubh.easynews.slurptv.State.EpisodeState;
import org.dubh.easynews.slurptv.State.Step;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

class TagTask extends AbstractTask {
  private final CommandExecutor executor;
  private final TVDatabase tvDatabase;

  @Inject
  TagTask(CommandExecutorFactory executorFactory, TVDatabase tvDatabase) {
    this.executor = executorFactory.newExecutor("settags");
    this.tvDatabase = tvDatabase;
  }

  @Override
  public EpisodeState perform(Show show, EpisodeState previousState) throws TaskFailedException,
      InterruptedException {
    File imageFile = null;
    if (previousState.hasArtFile()) {
      imageFile = new File(previousState.getArtFile());
    }

    Episode episode = previousState.getEpisode();
    try {
      EpisodeDetails details = null;
      if (show.hasTvdbId()) {
        details = tvDatabase.findEpisodeDetails(show.getTvdbId(), episode);
      }
      if (details == null) {
        details = createFakeDetails(show, episode);
      }

      ImmutableList.Builder<String> p = ImmutableList.builder();
      p.add("/Applications/AtomicParsley-MacOSX-0.9.0/AtomicParsley").add(
          previousState.getConvertedFile());
      if (imageFile != null && imageFile.exists()) {
        p.add("--artwork").add(imageFile.getPath());
      }
      p.add("--stik").add("TV Show");
      if (details.hasNetwork()) {
        p.add("--TVNetwork").add(details.getNetwork());
      }
      if (episode.hasDate()) {
        p.add("--TVSeasonNum").add(String.valueOf(episode.getDate().getYear()));
        p.add("--TVEpisodeNum").add(
            episode.getDate().getMonth()
                + Strings.padStart(String.valueOf(episode.getDate().getDate()), 2, '0'));
      } else {
        p.add("--TVSeasonNum").add(String.valueOf(details.getEpisode().getSeason()));
        p.add("--TVEpisodeNum").add(String.valueOf(details.getEpisode().getEpisode()));
      }
      if (details.hasDescription()) {
        p.add("--description").add(details.getDescription());
      }
      if (details.hasEpisodeName()) {
        p.add("--title").add(details.getEpisodeName());
      }
      p.add("--TVShowName");
      if (details.hasShowName()) {
        p.add(details.getShowName());
      } else {
        p.add(show.getName());
      }
      if (details.hasGenre()) {
        p.add("--genre").add(details.getGenre());
      }
      if (details.hasAirDate()) {
        p.add("--year").add(details.getAirDate());
      }
      p.add("--overWrite");

      System.out.println("*****************");
      System.out.println(Joiner.on(' ').join(p.build()));
      System.out.println("*****************");
      executor.execute(p.build(), new File(previousState.getConvertedFile()).getName());

      return EpisodeState.newBuilder(previousState).setLastCompletedStep(Step.TAGGING).build();

    } catch (IOException e) {
      throw new TaskFailedException("Failed to tag", e);
    }
  }

  private EpisodeDetails createFakeDetails(Show show, Episode episode) {
    // Make up some synthetic details. It's the best we can do.
    EpisodeDetails.Builder detailsBuilder = EpisodeDetails.newBuilder().setEpisode(episode)
        .setShowName(show.getName());
    if (episode.hasDate()) {
      DateTime date = new DateTime(episode.getDate().getYear(), episode.getDate().getMonth(),
          episode.getDate().getDate(), 0, 0, 0, 0);
      String dateString = DateTimeFormat.forPattern("EEE MMM dd").print(date);
      detailsBuilder.setEpisodeName(dateString).setAirDate(
          ISODateTimeFormat.basicDateTime().print(date));
    } else {
      detailsBuilder.setEpisodeName("Episode " + episode.getEpisode());
    }
    return detailsBuilder.build();
  }
}

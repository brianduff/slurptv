package org.dubh.slurptv.easynews;

import java.io.IOException;
import java.util.List;

import org.dubh.easynews.slurptv.SlurpTv.Configuration;
import org.dubh.easynews.slurptv.SlurpTv.Show;
import org.dubh.easynews.slurptv.State.EpisodeState;
import org.dubh.easynews.slurptv.State.EpisodeState.Step;
import org.dubh.slurptv.AbstractTask;
import org.dubh.slurptv.TaskFailedException;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

public class FindDownloadFileTask extends AbstractTask {
  private final Configuration configuration;
  private final EasynewsService easynews;

  @Inject
  public FindDownloadFileTask(Configuration configuration,
      EasynewsService easynews) {
    this.configuration = configuration;
    this.easynews = easynews;
  }

  @Override
  public EpisodeState perform(Show show, EpisodeState previousState)
      throws TaskFailedException {
    long minSize = configuration.getFileConfiguration().getMinSizeBytes();
    if (show.hasFileConfiguration()) {
      minSize = show.getFileConfiguration().getMinSizeBytes();
    }
    long maxSize = configuration.getFileConfiguration().getMaxSizeBytes();
    if (show.hasFileConfiguration()) {
      maxSize = show.getFileConfiguration().getMaxSizeBytes();
    }
    List<String> extensions = configuration.getFileConfiguration()
        .getFileTypeList();
    if (show.hasFileConfiguration()
        && show.getFileConfiguration().getFileTypeCount() > 0) {
      extensions = show.getFileConfiguration().getFileTypeList();
    }
    String preferredExtension = configuration.getFileConfiguration()
        .getPreferredType();
    if (show.hasFileConfiguration()) {
      preferredExtension = show.getFileConfiguration().getPreferredType();
    }

    ImmutableSet<ResultFilter> filters = ImmutableSet.of(new AtLeastSizeFilter(
        minSize), new NoBiggerThanFilter(maxSize), new FilenameExtensionFilter(
        extensions));
    ImmutableSet<ResultRanker> rankers = ImmutableSet.of(
        new PreferExtensionRanker(preferredExtension),
        new PreferEnglishRanker());

    Query query = new Query().setShowTitle(show.getName()).setEpisode(
        previousState.getEpisode());

    try {
      List<Result> results = easynews.findFiles(query, filters, rankers);
      if (results.size() == 0) {
        throw new TaskFailedException("No results found for query "
            + query.getSearch());
      }
      return EpisodeState.newBuilder(previousState)
          .setLastCompletedStep(Step.SEARCHING_EASYNEWS)
          .setUrl(results.get(0).getUrl()).build();
    } catch (IOException e) {
      throw new TaskFailedException("Easynews query " + query.getSearch()
          + " failed.", e);
    }
  }
}

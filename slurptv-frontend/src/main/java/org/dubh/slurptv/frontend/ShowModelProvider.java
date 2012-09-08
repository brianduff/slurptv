package org.dubh.slurptv.frontend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.dubh.easynews.slurptv.Data.Episode;
import org.dubh.easynews.slurptv.SlurpTv.Configuration;
import org.dubh.easynews.slurptv.SlurpTv.Show;
import org.dubh.slurptv.EpisodeFormatter;
import org.dubh.slurptv.StateManager;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
class ShowModelProvider implements ModelProvider {
  private final StateManager stateManager;
  private final Provider<Configuration> configuration;
  private final EpisodeFormatter episodeFormatter;
  
  @Inject
  ShowModelProvider(StateManager stateManager, Provider<Configuration> configuration, EpisodeFormatter episodeFormatter) {
    this.stateManager = stateManager;
    this.configuration = configuration;
    this.episodeFormatter = episodeFormatter;
  }
  
  @Override
  public Map<Object, Object> provideModel(String path, Map<String, String[]> parameters)
      throws Exception {
    Show show = findShow(path);
    if (show == null) {
      throw new IllegalArgumentException("No such show " + Paths.getLastPathSegment(path));
    }
    
    List<String> episodeIds = new ArrayList<>();
    for (Episode episode : stateManager.getCandidateEpisodes(show)) {
      episodeIds.add(episodeFormatter.format(episode));
    }
    
    return ImmutableMap.<Object, Object>of(
        "show", show,
        "episodeIds", episodeIds);
  }

  private @Nullable Show findShow(String path) {
    String showId = Paths.getLastPathSegment(path);
    for (Show show : configuration.get().getShowList()) {
      if (show.getId().equals(showId)) {
        return show;
      }
    }
    return null;
  }
}

package org.dubh.slurptv;

import java.io.IOException;
import java.util.logging.Logger;

import org.dubh.easynews.slurptv.Data.EpisodeDetails;
import org.dubh.easynews.slurptv.SlurpTv.Show;
import org.dubh.easynews.slurptv.State.EpisodeState;
import org.dubh.easynews.slurptv.State.Step;
import org.joda.time.DateTime;

import com.google.inject.Inject;

/**
 * Checks whether an episode has aired. If it hasn't, just keeps it in the same
 * state, otherwise, transitions to start looking for it on easynews.
 */
class CheckWhetherAiredTask extends AbstractTask {
  private static final Logger log = Logger.getLogger(CheckWhetherAiredTask.class.getName());
  private final TVDatabase tvDatabase;
  private final EpisodeLog episodeLog;

  @Inject
  CheckWhetherAiredTask(TVDatabase tvDatabase, EpisodeLog episodeLog) {
    this.tvDatabase = tvDatabase;
    this.episodeLog = episodeLog;
  }

  @Override
  public EpisodeState perform(Show show, EpisodeState previousState) throws TaskFailedException,
      InterruptedException {
    if (!show.hasTvdbId()) {
      // No tv database id, so we can't check. Just transition to the next
      // state.
      return successState(previousState);
    }
    try {
      EpisodeDetails details = tvDatabase.findEpisodeDetails(show.getTvdbId(),
          previousState.getEpisode());
      if (details == null) {
        // No details for that episode. Assume it hasn't aired yet.
        log.log(episodeLog.info(show, previousState.getEpisode(),
            "Not found in tvdb. Assuming not yet aired."));
        return previousState;
      }
      // Otherwise, found it! Did the air date happen yet?
      if (!details.hasAirDateMillis()) {
        // Doesn't have an air date. Assume it hasn't aired yet.
        log.log(episodeLog.info(show, previousState.getEpisode(),
            "No airdate yet in tvdb. Assuming not yet aired."));
        return previousState;
      }

      DateTime airDate = new DateTime(details.getAirDateMillis());
      if (airDate.isBeforeNow()) {
        return successState(previousState);
      }
      // Otherwise, the episode hasn't aired yet. Stay in the current state.
      log.log(episodeLog.info(show, previousState.getEpisode(), "Will not air until " + airDate));
      return previousState;
    } catch (IOException e) {
      throw new TaskFailedException("Failed to look up tv database", e);
    }
  }

  private EpisodeState successState(EpisodeState previousState) {
    return EpisodeState.newBuilder(previousState).setLastCompletedStep(Step.CHECKING_WHETHER_AIRED)
        .build();
  }

}

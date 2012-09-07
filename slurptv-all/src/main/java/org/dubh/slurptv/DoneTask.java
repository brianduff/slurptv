package org.dubh.slurptv;

import org.dubh.easynews.slurptv.SlurpTv.Show;
import org.dubh.easynews.slurptv.State.EpisodeState;
import org.dubh.easynews.slurptv.State.EpisodeState.Step;

class DoneTask extends AbstractTask {
	@Override
  public EpisodeState perform(Show show, EpisodeState previousState) {
		return EpisodeState.newBuilder(previousState)
				.setLastCompletedStep(Step.DONE)
				.build();
  }
}

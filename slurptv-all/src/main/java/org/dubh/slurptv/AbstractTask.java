package org.dubh.slurptv;

import org.dubh.easynews.slurptv.SlurpTv.Show;
import org.dubh.easynews.slurptv.State.EpisodeState;

/**
 * Base class for tasks.
 */
public abstract class AbstractTask {
	public abstract EpisodeState perform(Show show, EpisodeState previousState)
			throws TaskFailedException, InterruptedException;
}

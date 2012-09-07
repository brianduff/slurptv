package org.dubh.slurptv;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.dubh.easynews.slurptv.Data.Episode;
import org.dubh.easynews.slurptv.SlurpTv.Configuration;
import org.dubh.easynews.slurptv.SlurpTv.Show;
import org.dubh.easynews.slurptv.State.EpisodeState;
import org.dubh.easynews.slurptv.State.EpisodeState.Step;

import com.google.common.collect.ImmutableMap;

public class ProcessEpisodeCallable implements Callable<Void> {
	private static final Logger log = Logger.getLogger(ProcessEpisodeCallable.class.getName());
	private final StateManager stateManager;
	private final EpisodeLog episodeLog;
	private final Show show;
	private final Episode episode;
	private final ImmutableMap<Step, ? extends AbstractTask> tasks;
	private final Configuration configuration;

	ProcessEpisodeCallable(StateManager stateManager, EpisodeLog episodeLog, 
			ImmutableMap<Step, ? extends AbstractTask> tasks, Configuration configuration,
			Show show, Episode episode) {
		this.stateManager = stateManager;
		this.episodeLog = episodeLog;
		this.tasks = tasks;
		this.configuration = configuration;
		this.show = show;
		this.episode = episode;
	}
	
	public Void call() throws IOException {
		EpisodeState episodeState = stateManager.readState(show, episode);
		if (episodeState.getRetryCount() > configuration.getMaxRetries()) {
			log.log(episodeLog.info(show, episode, "Skipping because retry count was exceeded"));
			return null;
		}
		while (episodeState.getLastCompletedStep() != Step.DONE) {
			EpisodeState oldState = episodeState;
			episodeState = runNextStep(show, episode, episodeState);
			checkForScrewedUpState(oldState, episodeState);
			stateManager.writeState(show, episodeState);
			// If we failed, give up this time. We'll try again some other time.
			if (episodeState.hasFailedReason()) {
				break;
			}
			if (oldState.getLastCompletedStep() == episodeState.getLastCompletedStep()) {
				log.log(episodeLog.info(show, episode, "Didn't make progress. Giving up for now."));
				break;
			}
		}
		return null;
	}
	

	private void checkForScrewedUpState(EpisodeState episodeState,
      EpisodeState newState) {
	  if (!newState.getEpisode().equals(episodeState.getEpisode())) {
	  	throw new IllegalStateException("Step " + findNextStep(episodeState.getLastCompletedStep()) 
	  			+ " screwed up the state by wiping episode.");
	  }
  }
	
	private EpisodeState runNextStep(Show show, Episode episode, EpisodeState oldState) {
		Step nextStep = findNextStep(oldState.getLastCompletedStep());
		AbstractTask task = tasks.get(nextStep);
		try {
			EpisodeState newState = task.perform(show, oldState);
			return EpisodeState.newBuilder(newState)
					.clearFailedReason()
					.clearFailedStep()
					.build();
		} catch (Throwable t) {
			log.log(episodeLog.severe(show, episode, "Failed step due to exception " + nextStep, t));
			return EpisodeState.newBuilder(oldState)
					.setFailedStep(nextStep)
					.setFailedReason(t.getMessage())
					.setRetryCount(oldState.getRetryCount() + 1)
					.build();
		}
		
	}
	
	private Step findNextStep(Step lastStep) {
		int lastStepNumber = lastStep.getNumber();
		if (lastStepNumber == Step.values().length) {
			return null;
		}
		return Step.valueOf(lastStepNumber + 1);
	}
}

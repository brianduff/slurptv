package org.dubh.slurptv;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.dubh.easynews.slurptv.Data.Episode;
import org.dubh.easynews.slurptv.SlurpTv.Configuration;
import org.dubh.easynews.slurptv.SlurpTv.Show;
import org.dubh.easynews.slurptv.State.EpisodeState.Step;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;

class TaskExecutor extends AbstractScheduledService {
  private static final Logger log = Logger.getLogger(TaskExecutor.class.getName());
  private final ImmutableMap<Step, ? extends AbstractTask> tasks;
  private final StateManager stateManager;
  private final Configuration configuration;
  private final EpisodeLog episodeLog;
  private final ListeningExecutorService executor;

  @Inject
  TaskExecutor(ImmutableMap<Step, AbstractTask> tasks, StateManager stateManager,
      Configuration configuration, EpisodeLog episodeLog) {
    this.tasks = tasks;
    this.stateManager = stateManager;
    this.configuration = configuration;
    this.episodeLog = episodeLog;

    this.executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(configuration
        .getMaxConcurrentEpisodes()));
  }

  @Override
  protected void runOneIteration() throws Exception {
    List<ListenableFuture<Void>> futures = Lists.newArrayList();
    for (Show show : configuration.getShowList()) {
      if (show.getPaused()) {
        continue;
      }
      for (Episode episode : stateManager.getMissingEpisodes(show)) {
        futures.add(executor.submit(new ProcessEpisodeCallable(stateManager, episodeLog, tasks,
            configuration, show, episode)));
      }
    }

    ListenableFuture<List<Void>> allTasks = Futures.allAsList(futures);
    log.info("Waiting for " + futures.size() + "tasks...");
    allTasks.get();
  }

  @Override
  protected Scheduler scheduler() {
    return Scheduler.newFixedRateSchedule(0, configuration.getTimeBetweenExecutions(),
        TimeUnit.MINUTES);
  }
}

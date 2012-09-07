package org.dubh.slurptv;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.dubh.easynews.slurptv.Data.Episode;
import org.dubh.easynews.slurptv.Data.EpisodeDate;
import org.dubh.easynews.slurptv.SlurpTv.Configuration;
import org.dubh.easynews.slurptv.SlurpTv.Show;
import org.dubh.easynews.slurptv.State.EpisodeState;
import org.dubh.easynews.slurptv.State.EpisodeState.Step;
import org.dubh.slurptv.ConfigurationModule.ConfiguredDirectory;
import org.dubh.slurptv.ConfigurationModule.Directory;
import org.joda.time.DateTime;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.TextFormat;

/**
 * Manages state configuration files.
 */
@Singleton
public class StateManager {
	private static final Logger log = Logger.getLogger(StateManager.class.getName());
	private final File stateDir;
	private final EpisodeFormatter episodeFormatter;
	private final Configuration configuration;
	private final LoadingCache<CacheKey, EpisodeState> cache;
	private final EpisodeLog episodeLog;
	
	@Inject
	StateManager(@ConfiguredDirectory(Directory.SETTINGS) File stateDir, EpisodeFormatter episodeFormatter,
			Configuration configuration, EpisodeLog episodeLog) {
		this.stateDir = stateDir;
		this.episodeFormatter = episodeFormatter;
		this.configuration = configuration;
		this.episodeLog = episodeLog;
		this.cache = CacheBuilder.newBuilder()
				.build(new CacheLoader<CacheKey, EpisodeState>() {
					@Override
          public EpisodeState load(CacheKey key) throws Exception {
						return loadState(key);
          }
				});
	}
	
	private class CacheKey {
		final Show show;
		final Episode episode;
		CacheKey(Show show, Episode episode) {
			this.show = show;
			this.episode = episode;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof CacheKey)) {
				return false;
			}
			CacheKey other = (CacheKey) o;
			return Objects.equal(show, other.show) && Objects.equal(episode, other.episode);
		}
		
		@Override
		public int hashCode() {
			return Objects.hashCode(show, episode);
		}
	}
	
	private File getEpisodeFile(Show show, Episode episode) {
		return new File(stateDir, show.getId() + "-" + episodeFormatter.format(episode) + ".config");
	}

	public EpisodeState readState(Show show, Episode episode) throws IOException {
		try {
			return cache.get(new CacheKey(show, episode));
		} catch (ExecutionException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			}
			throw Throwables.propagate(e);
		}
	}
	
	private EpisodeState loadState(CacheKey cacheKey) throws IOException {
		Show show = cacheKey.show;
		Episode episode = cacheKey.episode;
		File episodeFile = getEpisodeFile(show, episode);
		EpisodeState.Builder builder = EpisodeState.newBuilder();
		synchronized (episodeFile.getPath().intern()) {
			if (!episodeFile.exists()) {
				log.log(episodeLog.info(show, episode, "Returning shiny new state for previously unseen episode"));
				return builder.setEpisode(episode).build();
			}
			log.log(episodeLog.info(show, episode, "Loading state from " + episodeFile));
  		Reader r = null;
  		try {
  			r = Files.newReader(episodeFile, Charsets.UTF_8);
  			TextFormat.merge(r, builder);
  			return builder.build();
  		} finally {
  			Closeables.closeQuietly(r);
  		}
		}
	}
	
	public void writeState(Show show, EpisodeState episodeState) throws IOException {
		log.log(episodeLog.info(show, episodeState.getEpisode(), "Writing state"));
		cache.put(new CacheKey(show, episodeState.getEpisode()), episodeState);
		File episodeFile = getEpisodeFile(show, episodeState.getEpisode());
		stateDir.mkdirs();
		synchronized (episodeFile.getPath().intern()) {
			Writer w = null;
			try {
				w = Files.newWriter(episodeFile, Charsets.UTF_8);
				TextFormat.print(episodeState, w);
			} finally {
				Closeables.closeQuietly(w);
			}
		}
	}
	
	/**
	 * Gets episodes that are missing.
	 */
	public Collection<Episode> getMissingEpisodes(Show show) throws IOException {
		Set<Episode> missingEpisodes = Sets.newLinkedHashSet();
		if (show.getSeasonal()) {
			int firstSeason = show.getOldestSeason();
			int lastSeason = show.getMaxSeason();
			
			for (int season = firstSeason; season <= lastSeason; season++) {
				for (int episodeNum = 1; episodeNum <= show.getMaxEpisodesPerSeason(); episodeNum++) {
					Episode episode = Episode.newBuilder().setSeason(season).setEpisode(episodeNum).build();
					addIfMissing(show, missingEpisodes, episode);
				}
			}
		} else {
			DateTime oldestDate = new DateTime().minusDays(configuration.getMaxDays());
			if (show.hasOldestDate()) {
				DateTime showOldestDate = new DateTime(show.getOldestDate().getYear(), show.getOldestDate().getMonth(),
						show.getOldestDate().getDate(), 0, 0, 0, 0);
				if (showOldestDate.isAfter(oldestDate)) {
					oldestDate = showOldestDate;
				}
			}
			
			DateTime current = oldestDate;
			while (current.isBeforeNow()) {
				Episode episode = Episode.newBuilder().setDate(EpisodeDate.newBuilder()
						.setDate(current.getDayOfMonth()).setMonth(current.getMonthOfYear()).setYear(current.getYear())).build();
				addIfMissing(show, missingEpisodes, episode);
				current = current.plusDays(1);
			}
		}
		return missingEpisodes;
	}

	private void addIfMissing(Show show, Set<Episode> missingEpisodes, Episode episode) throws IOException {
	  EpisodeState state = readState(show, episode);
	  if (state.getLastCompletedStep() != Step.DONE) {
	  	missingEpisodes.add(episode);
	  }
	  // If we have a failed state, see if it's ok to retry.
	  else if (state.hasFailedStep()) {
	  	if (state.getRetryCount() < configuration.getMaxRetries()) {
	  		missingEpisodes.add(episode);
	  	}
	  }
  }
}

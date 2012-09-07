package org.dubh.slurptv;

import java.io.File;
import java.io.IOException;

import org.dubh.easynews.slurptv.Data.EpisodeDetails;
import org.dubh.easynews.slurptv.SlurpTv.Configuration;
import org.dubh.easynews.slurptv.SlurpTv.Show;
import org.dubh.easynews.slurptv.State.EpisodeState;
import org.dubh.easynews.slurptv.State.EpisodeState.Step;

import com.google.inject.Inject;

/**
 * Downloads artwork for an episode.
 * @author brianduff
 */
class DownloadArtTask extends AbstractTask {
	private final File artDir;
	private final Downloader downloader;
	private final TVDatabase tvDatabase;
	private final EpisodeFormatter formatter;
	
	@Inject
	DownloadArtTask(Configuration configuration, Downloader downloader,
			TVDatabase tvDatabase, EpisodeFormatter formatter) {
		this.artDir = new File(configuration.getArtDir());
		this.downloader = downloader;
		this.tvDatabase = tvDatabase;
		this.formatter = formatter;
	}

	@Override
  public EpisodeState perform(Show show, EpisodeState previousState)
      throws TaskFailedException, InterruptedException {
		if (!show.hasTvdbId()) {
			// Skip artwork.
			return EpisodeState.newBuilder(previousState)
					.setLastCompletedStep(Step.DOWNLOADING_ART)
					.build();
		}
		
		try {
			EpisodeDetails details = tvDatabase.findEpisodeDetails(show.getTvdbId(), previousState.getEpisode());
			if (details == null) {
				return EpisodeState.newBuilder(previousState)
						.setLastCompletedStep(Step.DOWNLOADING_ART)
						.build();
			}
			int lastDot = details.getArtworkUrl().lastIndexOf('.');
			String artExt = details.getArtworkUrl().substring(lastDot);
			File artFile = new File(artDir,
					show.getId() + "-art-" + formatter.format(previousState.getEpisode()) 
					+ "-" + previousState.getRetryCount()
					+ artExt);
			artDir.mkdirs();
			downloader.download(details.getArtworkUrl(), artFile);
			return EpisodeState.newBuilder(previousState)
					.setLastCompletedStep(Step.DOWNLOADING_ART)
					.setArtFile(artFile.getPath())
					.build();
		} catch (IOException e) {
			throw new TaskFailedException("Failed to get artwork", e);
		}
  }
}

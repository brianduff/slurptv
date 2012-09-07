package org.dubh.slurptv;

import java.io.File;
import java.io.IOException;

import org.dubh.easynews.slurptv.SlurpTv.Credentials;
import org.dubh.easynews.slurptv.SlurpTv.Show;
import org.dubh.easynews.slurptv.State.EpisodeState;
import org.dubh.easynews.slurptv.State.EpisodeState.Step;
import org.dubh.slurptv.ConfigurationModule.ConfiguredDirectory;
import org.dubh.slurptv.ConfigurationModule.Directory;
import org.dubh.slurptv.ConfigurationModule.Easynews;

import com.google.inject.Inject;

/**
 * Executes curl to download a URL.
 */
public class DownloadVideoTask extends AbstractTask {
  private final Credentials credentials;
  private final Downloader downloader;
  private final File downloadDir;
  private final EpisodeFormatter episodeFormatter;
  
  @Inject
  DownloadVideoTask(@Easynews Credentials credentials, 
  		Downloader downloader, 
  		@ConfiguredDirectory(Directory.DOWNLOAD) File downloadDir,
  		EpisodeFormatter episodeFormatter) {
  	this.credentials = credentials;
  	this.downloader = downloader;
    this.downloadDir = downloadDir;
    this.episodeFormatter = episodeFormatter;
  }
  
  @Override
  public EpisodeState perform(Show show, EpisodeState previousState) throws TaskFailedException {
  	downloadDir.mkdirs();
  	File destinationFile = new File(downloadDir, 
  			show.getId()
  			+ "-"
  			+ episodeFormatter.format(previousState.getEpisode())
  			+ "-"
  			+ previousState.getRetryCount() 
  			+ fileExtension(previousState.getUrl()));
  	
  	try {
  		downloader.download(credentials, previousState.getUrl(), destinationFile);
  		return EpisodeState.newBuilder(previousState)
  				.setLastCompletedStep(Step.DOWNLOADING)
  				.setDownloadFile(destinationFile.getPath())
  				.build();
  	} catch (InterruptedException e) {
  		throw new TaskFailedException(e);
  	} catch (IOException e) {
  		throw new TaskFailedException(e);
  	}
  }
  
  private String fileExtension(String path) {
    int lastDot = path.lastIndexOf('.');
    return path.substring(lastDot);
  }    
}

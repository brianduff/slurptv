package org.dubh.slurptv;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.dubh.easynews.slurptv.SlurpTv.Credentials;

import com.google.inject.Inject;

class Downloader {
  private static final Logger log = Logger.getLogger(DownloadVideoTask.class.getName());
	private final CommandExecutor executor;
	
	@Inject
	Downloader(CommandExecutorFactory executorFactory) {
		this.executor = executorFactory.newExecutor("download");
	}

  public void download(Credentials credentials, String url, File destinationFile) throws IOException, InterruptedException {
    log.info("Downloading " + url);
    destinationFile.getParentFile().mkdirs();
    executor.execute(new String[] {
        "curl",
        "--basic",
        "--user",
        credentials.getUsername() + ":" + credentials.getPassword(),
        url,
        "--output",
        destinationFile.getPath()
    }, destinationFile.getName());
  }
  
  public void download(String url, File destinationFile) throws IOException, InterruptedException {
    destinationFile.getParentFile().mkdirs();
    executor.execute(new String[] {
        "curl",
        url,
        "--output",
        destinationFile.getPath()
    }, destinationFile.getName());
  }
}

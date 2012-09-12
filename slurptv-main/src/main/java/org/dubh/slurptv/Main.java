package org.dubh.slurptv;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.logging.Logger;

import org.dubh.easynews.slurptv.State.Step;
import org.dubh.slurptv.easynews.FindDownloadFileTask;
import org.dubh.slurptv.frontend.FrontendModule;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;

public class Main {
	private static final Logger log = Logger.getLogger(Main.class.getName());
  private final Set<Service> services;
  
  @Inject
  Main(Set<Service> services) {
    this.services = services;
  }
  
  public void go() throws Exception {
  	for (Service service : services) {
  		log.info("Starting service " + service);
  		service.startAndWait();
  		log.info("Started service " + service);
  	}
  }

  public static void main(String[] args) throws Exception {
  	Injector injector = Guice.createInjector(
  			new CommandLineArgsModule(args),
  			new ConfigurationModule(),
  			new TasksModule(),
  			new FrontendModule(),
  			new TaskExecutorModule(),
  			new EventBusModule());
  	injector.getInstance(Main.class).go();
  }
  
  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public @interface ConfigurationFile {}
  
  private static class TasksModule extends AbstractModule {
  	@Override
  	public void configure() {}
  	
  	@Provides
  	ImmutableMap<Step, AbstractTask> provideTasks(CheckWhetherAiredTask checkAired,
  			FindDownloadFileTask findDownloadFile,
  			DownloadVideoTask download,
  			ConvertToMp4Task convertToMp4,
  			DownloadArtTask downloadArt,
  			TagTask tag,
  			CopyToITunesTask copyToITunes,
  			DoneTask done) {
    	ImmutableMap.Builder<Step, AbstractTask> taskBuilder = ImmutableMap.builder();
    	taskBuilder
    		.put(Step.CHECKING_WHETHER_AIRED, checkAired)
    		.put(Step.SEARCHING_EASYNEWS, findDownloadFile)
    		.put(Step.DOWNLOADING, download)
    		.put(Step.CONVERTING, convertToMp4)
    		.put(Step.DOWNLOADING_ART, downloadArt)
    		.put(Step.TAGGING, tag)
    		.put(Step.COPYING_TO_ITUNES, copyToITunes)
    		.put(Step.DONE, done);
    	return taskBuilder.build();
  	}
  }
}

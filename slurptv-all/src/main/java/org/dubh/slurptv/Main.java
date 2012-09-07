package org.dubh.slurptv;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.ExecutionException;

import org.dubh.easynews.slurptv.SlurpTv.Configuration;
import org.dubh.easynews.slurptv.State.EpisodeState.Step;
import org.dubh.slurptv.easynews.FindDownloadFileTask;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;

public class Main {
  private final Configuration configuration;
  private final TaskExecutor taskExecutor;
  
  @Inject
  Main(Configuration configuration, TaskExecutor taskExecutor) {
    this.configuration = configuration;
    this.taskExecutor = taskExecutor;
  }
  
  public void go() throws IOException, InterruptedException, ExecutionException {
  	taskExecutor.run(configuration.getShowList());
  }

  public static void main(String[] args) throws Exception {
  	Injector injector = Guice.createInjector(
  			new CommandLineArgsModule(args),
  			new ConfigurationModule(),
  			new TasksModule());
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

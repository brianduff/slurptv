package org.dubh.slurptv;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dubh.slurptv.ConfigurationModule.ConfigurationFile;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import com.google.inject.Singleton;

class CommandLineArgsModule extends AbstractModule {
	private final String[] args;
	
	CommandLineArgsModule(String[] args) {
		this.args = args;
	}
	
	@Override
  protected void configure() {
  }
	
	@Provides
	@Singleton
	@CommandLineArgs
	String[] provideCommandLineArgs() {
		return args;
	}
	
	@Provides
	@Singleton
	@ConfigurationFile
	File provideConfigurationFile(@CommandLineArgs String[] args) {
  	File configFile = new File(System.getProperty("user.home"), "slurptv.conf");
  	if (args.length > 0) {
  		configFile = new File(args[0]);
  	}
  	return configFile;
	}
	
  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  @interface CommandLineArgs {}
}

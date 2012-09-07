package org.dubh.slurptv;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dubh.easynews.slurptv.SlurpTv.Configuration;
import org.dubh.easynews.slurptv.SlurpTv.Credentials;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.protobuf.TextFormat;

public class ConfigurationModule extends AbstractModule {
	@Override
	public void configure() {}
	
	@Provides
	@Singleton
	Configuration provideConfiguration(@ConfigurationFile File configurationFile) {
		try {
    	Configuration.Builder configBuilder = Configuration.newBuilder();
    	BufferedReader br = null;
    	try {
    		br = new BufferedReader(new FileReader(configurationFile));
    		TextFormat.merge(br, configBuilder);
    	} finally {
    		Closeables.closeQuietly(br);
    	}
    	return configBuilder.build();
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
	
	@Provides
	@Singleton
	@Easynews
	Credentials provideEasynewsCredentials(Configuration configuration) {
		return configuration.getEasynewsCredentials();
	}
	
	@Provides
	@Singleton
	@ConfiguredDirectory(Directory.TEMP)
	File provideTempDir(Configuration configuration) {
		return new File(configuration.getTempDir());
	}
	
	@Provides
	@Singleton
	@ConfiguredDirectory(Directory.DOWNLOAD)
	File provideDownloadDir(Configuration configuration) {
		return new File(configuration.getDownloadDir());
	}
	
	@Provides
	@Singleton
	@ConfiguredDirectory(Directory.SETTINGS)
	File provideSettingsDir(Configuration configuration) {
		return new File(configuration.getSettingsDir());
	}
	
	enum Directory {
		TEMP,
		DOWNLOAD,
		SETTINGS
	}
	
  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  @interface ConfiguredDirectory {
  	Directory value();
  }

  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  @interface ConfigurationFile {}
  
  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public @interface Easynews {}
}

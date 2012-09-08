package org.dubh.slurptv;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dubh.easynews.slurptv.SlurpTv.Configuration;
import org.dubh.easynews.slurptv.SlurpTv.Credentials;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class ConfigurationModule extends AbstractModule {
  @Override
  public void configure() {
    Multibinder<Service> serviceBinder = Multibinder.newSetBinder(binder(), Service.class);
    serviceBinder.addBinding().to(ConfigurationFileWatcher.class);
  }

  @Provides
  Configuration provideConfiguration(ConfigurationManager configManager) {
    return configManager.getConfiguration();
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
    TEMP, DOWNLOAD, SETTINGS
  }

  @BindingAnnotation
  @Target({ FIELD, PARAMETER, METHOD })
  @Retention(RUNTIME)
  @interface ConfiguredDirectory {
    Directory value();
  }

  @BindingAnnotation
  @Target({ FIELD, PARAMETER, METHOD })
  @Retention(RUNTIME)
  @interface ConfigurationFile {
  }

  @BindingAnnotation
  @Target({ FIELD, PARAMETER, METHOD })
  @Retention(RUNTIME)
  public @interface Easynews {
  }
}

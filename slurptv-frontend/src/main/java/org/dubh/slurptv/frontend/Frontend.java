package org.dubh.slurptv.frontend;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.common.base.Throwables;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.servlet.GuiceFilter;
import com.google.opengse.ServletEngineConfiguration;
import com.google.opengse.ServletEngineConfigurationImpl;
import com.google.opengse.core.ServletEngineImpl;
import com.google.opengse.webapp.WebAppCollection;
import com.google.opengse.webapp.WebAppCollectionFactory;
import com.google.opengse.webapp.WebAppConfigurationBuilder;

public class Frontend {
	private static final int MAX_THREADS = 10;
	private final int port;
	
	@Inject
	Frontend(@FrontendPort int port) {
		this.port = port;
	}
	
	public void start() throws Exception {
		WebAppConfigurationBuilder configBuilder = new WebAppConfigurationBuilder();
		configBuilder.addFilter(GuiceFilter.class, "/*");

		File contextDir = new File(System.getProperty("java.io.tmpdir"));
		final WebAppCollection webapps = WebAppCollectionFactory
				.createWebAppCollectionWithOneContext(contextDir, "ROOT", configBuilder.getConfiguration());
		webapps.startAll();
		
		final ServletEngineConfiguration config = ServletEngineConfigurationImpl.create(port, MAX_THREADS);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ServletEngineImpl.create(webapps, config).run();
				} catch (Exception e) {
					Throwables.propagate(e);
				}
			}
		}).start();
	}
	
  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  @interface FrontendPort {
  }
}

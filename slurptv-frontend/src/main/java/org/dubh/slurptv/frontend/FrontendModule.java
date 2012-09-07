package org.dubh.slurptv.frontend;

import org.dubh.easynews.slurptv.SlurpTv.Configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.servlet.ServletModule;

public class FrontendModule extends AbstractModule {
	@Override
  protected void configure() {
		install(new Servlets());
  }
		
	@Provides
	@Frontend.FrontendPort
	int provideFrontendPort(Configuration config) {
		return config.getFrontendPort();
	}
	
	private static class Servlets extends ServletModule {
		@Override
    protected void configureServlets() {
			serve("/statusz").with(StatusServlet.class);
 			serve("*.html").with(StaticContentServlet.class);
    }
	}
}

package org.dubh.slurptv.frontend;

import org.dubh.easynews.slurptv.SlurpTv.Configuration;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;

public class FrontendModule extends AbstractModule {
	@Override
  protected void configure() {
		bind(GuiceFilter.class);
		install(new Servlets());
		Multibinder<Service> serviceBinder = Multibinder.newSetBinder(binder(), Service.class);
		serviceBinder.addBinding().to(Frontend.class);
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
    }
	}
}

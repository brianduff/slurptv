package org.dubh.slurptv.frontend;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.servlet.GuiceFilter;

public class Frontend {
	private static final int MAX_THREADS = 10;
	private final int port;
	
	@Inject
	Frontend(@FrontendPort int port) {
		this.port = port;
	}
	
	public void start() throws Exception {
		Server server = new Server(port);
		ServletContextHandler handler = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
		handler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
		server.start();
	}
	
  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  @interface FrontendPort {
  }
}

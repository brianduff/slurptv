package org.dubh.slurptv.frontend;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.servlet.GuiceFilter;

class Frontend extends AbstractIdleService {
  private final int port;
  private final GuiceFilter guiceFilter;

  private Server server;

  @Inject
  Frontend(@FrontendPort int port, GuiceFilter guiceFilter) {
    this.port = port;
    this.guiceFilter = guiceFilter;
  }

  @BindingAnnotation
  @Target({ FIELD, PARAMETER, METHOD })
  @Retention(RUNTIME)
  @interface FrontendPort {
  }

  @Override
  protected void startUp() throws Exception {
    Server server = new Server(port);
    ServletContextHandler handler = new ServletContextHandler();
    handler.setContextPath("/");
    // Needed to make jetty happy.
    handler.addServlet(new ServletHolder(new HttpServlet() {
      @Override
      protected void service(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
    }), "/*");
    server.setHandler(handler);
    FilterHolder holder = new FilterHolder(guiceFilter);
    handler.addFilter(holder, "/*", EnumSet.allOf(DispatcherType.class));
    server.start();
    this.server = server;
  }

  @Override
  protected void shutDown() throws Exception {
    server.stop();
  }
}

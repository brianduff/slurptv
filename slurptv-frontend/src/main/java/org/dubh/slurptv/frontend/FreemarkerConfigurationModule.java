package org.dubh.slurptv.frontend;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * A generalized module for installing freemarker support.
 * 
 * @author brianduff
 */
public abstract class FreemarkerConfigurationModule extends AbstractModule {
  private final Collection<ServeContext> serveContexts = Sets.newHashSet();

  @Provides
  @Singleton
  Configuration provideConfiguration() {
    Configuration cfg = new Configuration();
    cfg.setClassForTemplateLoading(FreemarkerConfigurationModule.class, "");
    cfg.setObjectWrapper(new DefaultObjectWrapper());
    return cfg;
  }

  @Provides
  @Singleton
  Collection<ServeContext> provideServeContexts() {
    return serveContexts;
  }

  ServeContext serve(String pattern) {
    return new ServeContext(pattern);
  }

  protected final class ServeContext {
    private final String pattern;
    private String template;
    private Key<? extends ModelProvider> modelKey;

    private ServeContext(String pattern) {
      this.pattern = pattern;
      serveContexts.add(this);
    }

    ServeContext usingTemplate(String template) {
      this.template = template;
      return this;
    }

    ServeContext withDataModel(Class<? extends ModelProvider> modelProvider) {
      this.modelKey = Key.get(modelProvider);
      return this;
    }
  }

  @Override
  protected final void configure() {
    configureTemplates();
    install(new ServletModule() {
      @Override
      public void configureServlets() {
        for (ServeContext context : serveContexts) {
          serve(context.pattern).with(TemplateServlet.class);
        }
      }
    });
  }

  protected abstract void configureTemplates();

  @Provides
  @RequestScoped
  @Nullable
  private ServeContext provideServeContext(HttpServletRequest request) {
    return findContext(request.getPathInfo());
  }

  @Provides
  @RequestScoped
  private ModelProvider provideModelProvider(@Nullable ServeContext context,
      Injector injector) {
    if (context == null) {
      return new ModelProvider() {
        @Override
        public Map<Object, Object> provideModel(String path,
            Map<String, String[]> parameters) throws Exception {
          return ImmutableMap.of();
        }
      };
    }
    return injector.getInstance(context.modelKey);
  }

  @Provides
  @RequestScoped
  @Nullable
  private Template provideTemplate(Configuration configuration,
      @Nullable ServeContext context) {
    if (context == null) {
      // TODO(bduff): handle no template.
      return null;
    }
    try {
      return configuration.getTemplate(context.template);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private @Nullable
  ServeContext findContext(String pathInfo) {
    for (ServeContext context : serveContexts) {
      if (context.pattern.endsWith("*")
          && pathInfo.startsWith(context.pattern.substring(0,
              context.pattern.length() - 1))) {
        return context;
      } else if (context.pattern.equals(pathInfo)) {
        return context;
      }
    }
    return null;
  }
}

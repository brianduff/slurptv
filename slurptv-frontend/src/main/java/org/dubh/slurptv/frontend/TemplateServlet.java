package org.dubh.slurptv.frontend;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import freemarker.template.Template;

/**
 * General template for freemarker rendering.
 */
@Singleton
class TemplateServlet extends HttpServlet {
	private final Provider<ModelProvider> modelProvider;
	private final @Nullable Provider<Template> template;
	
	@Inject
	TemplateServlet(Provider<ModelProvider> modelProvider, @Nullable Provider<Template> template) {
		this.modelProvider = modelProvider;
		this.template = template;
	}
	
	@Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
		if (template == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		resp.setContentType("text/html");
		try {
			template.get().process(modelProvider.get().provideModel(req.getPathInfo(), req.getParameterMap()), resp.getWriter());
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			resp.getWriter().close();
		}
  }

}

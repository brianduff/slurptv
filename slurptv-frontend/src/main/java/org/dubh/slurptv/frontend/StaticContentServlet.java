package org.dubh.slurptv.frontend;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;

/**
 * General servlet for serving static content (from the client directory).
 * @author brianduff
 */
public class StaticContentServlet extends HttpServlet {
	private static final ImmutableMap<String, String> extensionToMimeType = ImmutableMap.of(
			".html", "text/html");
	
	@Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
		
		String path = req.getPathInfo();
		URL resource = StaticContentServlet.class.getResource("/org/dubh/slurptv/frontent/client" + path);
		if (resource == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		int dotIndex = path.lastIndexOf('.');
		if (dotIndex == -1) {
			resp.setContentType("binary/octet-stream");
		} else {
			String contentType = extensionToMimeType.get(path.substring(dotIndex + 1));
			if (contentType == null) {
				contentType = "binary/octet-stream";
			}
			resp.setContentType(contentType);
		}
		
		ByteStreams.copy(resource.openStream(), resp.getOutputStream());
		resp.getOutputStream().close();
  }
	// TODO caching 'n stuff.
	
}

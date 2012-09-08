package org.dubh.slurptv.frontend;

import java.util.Map;

import org.dubh.easynews.slurptv.SlurpTv.Configuration;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

class ShowsModelProvider implements ModelProvider {
	private final Configuration configuration;
	
	@Inject
	ShowsModelProvider(Configuration configuration) {
		this.configuration = configuration;
	}
	
	@Override
  public Map<Object, Object> provideModel(String path, Map<String, String[]> parameters) throws Exception {
		return ImmutableMap.<Object, Object>of("shows", configuration.getShowList());
  }
}

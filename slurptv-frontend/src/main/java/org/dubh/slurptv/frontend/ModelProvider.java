package org.dubh.slurptv.frontend;

import java.util.Map;

/**
 * A provider of data models.
 * @author brianduff
 */
interface ModelProvider {
	Map<Object, Object> provideModel(String path, Map<String, String[]> parameters) throws Exception;
}

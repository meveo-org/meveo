/**
 * 
 */
package org.meveo.util;

import java.util.HashMap;
import java.util.Map;

import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.typereferences.GenericTypeReferences;

public class JsonMap extends HashMap<String, Object> {

	public JsonMap(String value) {
		super();
		Map<String, Object> tmpMap = JacksonUtil.fromString(value, GenericTypeReferences.MAP_STRING_OBJECT);
		this.putAll(tmpMap);
	}
}

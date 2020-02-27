package org.meveo.api.swagger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since
 * @version
 */
public class SwaggerPropertyField {

	private String fieldName;
	private Map<String, Object> properties;

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public Map<String, Object> getPropertiesNullSafe() {
		if (properties == null) {
			properties = new HashMap<>();
		}

		return properties;
	}
}

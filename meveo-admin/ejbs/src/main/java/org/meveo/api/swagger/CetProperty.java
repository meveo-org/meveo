package org.meveo.api.swagger;

import io.swagger.models.properties.AbstractProperty;
import io.swagger.models.properties.Property;

/**
 * Swagger property for CET.
 * 
 * @see Property
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.8.0
 * @version 6.8.0
 */
public class CetProperty extends AbstractProperty {

	public static final String TYPE = "cet";

	public CetProperty(String format) {
		super.type = TYPE;
		super.format = format;
	}
}

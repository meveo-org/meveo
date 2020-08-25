/**
 * 
 */
package org.meveo.jackson.serializers;

import java.io.IOException;

import org.meveo.model.customEntities.CustomEntityInstance;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
public class CustomEntityInstanceSerializer extends JsonSerializer<CustomEntityInstance> {

	@Override
	public void serialize(CustomEntityInstance value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("cetCode", value.getCetCode());
		gen.writeObjectField("values", value.getCfValuesAsValues());
		gen.writeStringField("uuid", value.getUuid());
		gen.writeEndObject();
	}
	
}

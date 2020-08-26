/**
 * 
 */
package org.meveo.jackson.deserializers;

import java.io.IOException;
import java.util.Map;

import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.persistence.JacksonUtil;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
public class CustomEntityInstanceDeserializer extends JsonDeserializer<CustomEntityInstance> {

	@Override
	public CustomEntityInstance deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonNode node = p.readValueAsTree();
		
		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setUuid(node.get("uuid").asText());
		cei.setCetCode(node.get("cetCode").asText());
		
		CustomFieldValues cfValues = new CustomFieldValues();
		var map = JacksonUtil.convert(node.get("values"), new TypeReference<Map<String, Object>>(){});
		map.forEach((k,v) -> cfValues.setValue(k, v));
		cei.setCfValues(cfValues);
		
		return cei;
	}

}

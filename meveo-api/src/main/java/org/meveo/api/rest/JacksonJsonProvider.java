package org.meveo.api.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

/**
 * @author clement.bareth
 *
 */
@Provider
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class JacksonJsonProvider extends JacksonJaxbJsonProvider {
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
			.setSerializationInclusion(JsonInclude.Include.ALWAYS)
			.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true)
			.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
			.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, false)
			.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
	
	public JacksonJsonProvider() {
		super();
		super.setAnnotationsToUse(DEFAULT_ANNOTATIONS);
		super.setMapper(OBJECT_MAPPER);
	}
}

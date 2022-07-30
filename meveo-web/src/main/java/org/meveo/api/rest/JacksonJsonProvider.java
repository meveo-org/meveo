package org.meveo.api.rest;

import java.time.Instant;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

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
			.registerModule(new Hibernate5Module()
					.enable(Hibernate5Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS)
					.enable(Hibernate5Module.Feature.REPLACE_PERSISTENT_COLLECTIONS))
			.registerModule(new Jdk8Module())
	        .registerModule(new JavaTimeModule())
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
	
    public static void main (String... args) throws JsonProcessingException {
    	Instant now = Instant.now();
    	Map<String, Object> map = Map.of("now", now);
    	System.out.println(OBJECT_MAPPER.writeValueAsString(map));
    }
}

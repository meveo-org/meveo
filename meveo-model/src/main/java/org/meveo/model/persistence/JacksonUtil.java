package org.meveo.model.persistence;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Map;

import org.meveo.commons.utils.FileDeserializer;
import org.meveo.model.customEntities.CustomEntityInstance;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.FileSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Helper class for processing JSON.
 *
 * @author Clément Bareth
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @lastModifiedVersion 6.14.0
 */
@SuppressWarnings("deprecation")
public class JacksonUtil {

    static {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(om.getVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        om.setVisibility(om.getVisibilityChecker().withGetterVisibility(JsonAutoDetect.Visibility.NONE));
        om.setVisibility(om.getVisibilityChecker().withIsGetterVisibility(Visibility.NONE));
		om.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        om.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		om.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
		om.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        om.setSerializationInclusion(Include.NON_NULL);
        om.registerModule(new JavaTimeModule());

        SimpleModule fileModule = new SimpleModule()
                .addSerializer(File.class, new FileSerializer())
                .addDeserializer(File.class, new FileDeserializer());

        om.registerModule(fileModule);
        OBJECT_MAPPER = om;
    }
    
    public static void main (String... args) {
    	Instant now = Instant.now();
    	Map<String, Object> map = Map.of("now", now);
    	System.out.println(toString(map));
    }

    public static ObjectMapper OBJECT_MAPPER;

    public static <T> T fromString(String string, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(string, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("The given string value: " + string + " cannot be transformed to Json object", e);
        }
    }
    
    public static <T> T fromString(String string, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(string, typeReference);
        } catch (IOException e) {
            throw new IllegalArgumentException("The given string value: " + string + " cannot be transformed to Json object", e);
        }
    }

    public static String toString(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("The given Json object value: " + value + " cannot be transformed to a String", e);
        }
    }
    
    public static String toStringPrettyPrinted(Object value) {
        try {
        	return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("The given Json object value: " + value + " cannot be transformed to a String", e);
        }
    }
    
    public static String beautifyString(String jsonString) {
    	Object obj = fromString(jsonString, Object.class);
    	return toStringPrettyPrinted(obj);
    }

    public static JsonNode toJsonNode(String value) {
        try {
            return OBJECT_MAPPER.readTree(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
	public static Object convert(Object value, JavaType jacksonType) {
        return OBJECT_MAPPER.convertValue(value, jacksonType);
	}
    
    public static <T> T convert(Object value, Class<T> clazz) {
        return OBJECT_MAPPER.convertValue(value, clazz);
    }
    
    public static <T> T convert(Object value, TypeReference<T> typeref) {
        return OBJECT_MAPPER.convertValue(value, typeref);
    }
    
    public static Map<String, Object> convertToMap(CustomEntityInstance value) {
    	return OBJECT_MAPPER.convertValue(value, new TypeReference<Map<String, Object>>() {});
    }
    
    public static <T> T read(String value, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        return OBJECT_MAPPER.readValue(value, clazz);
    }
    
    public static <T> T read(File value, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        return OBJECT_MAPPER.readValue(value, clazz);
    }
    
    public static <T> T read(File value, TypeReference<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        return OBJECT_MAPPER.readValue(value, clazz);
    }
    
    public static <T> T read(InputStream value, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        return OBJECT_MAPPER.readValue(value, clazz);
    }
    
    public static <T> T read(InputStream value, TypeReference<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        return OBJECT_MAPPER.readValue(value, clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T clone(T value) {
        return fromString(toString(value), (Class<T>) value.getClass());
    }

}
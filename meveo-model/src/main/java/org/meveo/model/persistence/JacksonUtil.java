package org.meveo.model.persistence;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.FileSerializer;
import org.meveo.commons.utils.FileDeserializer;
import org.meveo.model.customEntities.CustomEntityInstance;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Helper class for processing JSON.
 *
 * @author Clément Bareth
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @lastModifiedVersion 6.3.0
 */
@SuppressWarnings("deprecation")
public class JacksonUtil {

    static {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(om.getVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        om.setVisibility(om.getVisibilityChecker().withGetterVisibility(JsonAutoDetect.Visibility.NONE));
        om.setVisibility(om.getVisibilityChecker().withIsGetterVisibility(Visibility.NONE));
        om.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.setSerializationInclusion(Include.NON_NULL);

        SimpleModule fileModule = new SimpleModule()
                .addSerializer(File.class, new FileSerializer())
                .addDeserializer(File.class, new FileDeserializer());

        om.registerModule(fileModule);
        OBJECT_MAPPER = om;

        /*                 .setSerializationInclusion(JsonInclude.Include.ALWAYS)
                .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true)
                .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true) */
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

    public static JsonNode toJsonNode(String value) {
        try {
            return OBJECT_MAPPER.readTree(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
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

    @SuppressWarnings("unchecked")
    public static <T> T clone(T value) {
        return fromString(toString(value), (Class<T>) value.getClass());
    }
}
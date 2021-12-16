/**
 * 
 */
package org.meveo.commons.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.hibernate.annotations.NaturalId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author heros
 * @since 
 * @version
 */
public class JpaUtils {
	
	private static Logger LOGGER = LoggerFactory.getLogger(JpaUtils.class);

	public static Optional<String> extractNaturalId(Object entity) {
		Class<?> entityClass = entity.getClass();
		return Arrays.stream(entityClass.getDeclaredFields())
			.filter(field -> field.isAnnotationPresent(NaturalId.class))
			.filter(field -> field.getType().equals(String.class))
			.filter(Field::trySetAccessible)
			.findFirst()
			.map(field -> {
				try {
					return (String) field.get(entity);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					LOGGER.error("Failed to get natural id of {}", entity, e);
					return null;
				}
			});
	}
}

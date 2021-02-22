/**
 * 
 */
package org.meveo.model.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.model.CustomEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.security.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Utilitary class for manipulating {@link CustomEntityInstance}
 * 
 * @author clement.bareth
 * @since 6.8.0
 * @version 6.12.0
 */
public class CEIUtils {
	
	private static Logger LOG = LoggerFactory.getLogger(CEIUtils.class);
	
	/**
	 * @param cei  The entity to hash
	 * @param cfts Custom fields of the entity template
	 * @return a hash of the cei based on the uuid and the clear fields
	 */
	public static String getHash(CustomEntityInstance cei, Map<String, CustomFieldTemplate> cfts) {
		List<Object> objectsToHash = new ArrayList<>();
		if(cei.getUuid() == null) {
			throw new IllegalArgumentException("Can't hash a CEI without uuid");
		}
		objectsToHash.add(cei.getUuid());
		
		cei.getCfValuesAsValues().forEach((key, value) -> {
			if(value != null && cfts.get(key) != null && cfts.get(key).getFieldType() != CustomFieldTypeEnum.SECRET) {
				objectsToHash.add(value);
			}
		});
		
		return PasswordUtils.getSalt(objectsToHash.toArray());
	}
	
	public static CustomEntityInstance fromMap(Map<String, Object> map, CustomEntityTemplate cet) {
		var cei = pojoToCei(map);
		cei.setCet(cet);
		return cei;
	}
	
	/**
	 * Converts a POJO to a CEI
	 * 
	 * @param pojo to convert
	 * @return converted CEI
	 */
	public static CustomEntityInstance pojoToCei(Object pojo) {
		Map<String, Object> pojoAsMap;
		if(pojo instanceof Map) {
			pojoAsMap = (Map<String, Object>) pojo;
		} else { 
			// Transform POJO into Map
			Map<String, Object> values = new HashMap<>();
			Stream.of(pojo.getClass().getMethods())
				.filter(m -> m.getName().startsWith("get") | m.getName().startsWith("is"))
				.filter(m -> m.getParameterCount() == 0)
				.forEach(m -> {
					var key = getFieldForGetter(pojo.getClass(), m);
					try {
						if(key != null) {
							var value = m.invoke(pojo);
							values.put(key, value);
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				});
				
			pojoAsMap = new HashMap<>();
			values.entrySet()
				.stream()
				.forEach(e -> {
					if(e.getValue() != null && e.getValue().getClass().getAnnotation(Entity.class) != null) {
						pojoAsMap.put(e.getKey(), getIdValue(e.getValue()));
					} else {
						pojoAsMap.put(e.getKey(), e.getValue());
					}
				});
		}
				
				
				//JacksonUtil.convert(pojo, GenericTypeReferences.MAP_STRING_OBJECT);
		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setUuid((String) pojoAsMap.get("uuid"));
		cei.setCetCode(pojo.getClass().getSimpleName());
		CustomFieldValues customFieldValues = new CustomFieldValues();
		pojoAsMap.forEach(customFieldValues::setValue);
		cei.setCfValues(customFieldValues);
		return cei;
	}
	
	/**
	 * Converts a CEI to a POJO
	 * 
	 * @param <T> type of the pojo
	 * @param cei the cei to convert
	 * @param pojoClass the class of the pojo
	 * @return the instance of the POJO
	 */
	public static <T> T ceiToPojo(CustomEntityInstance cei, Class<T> pojoClass) {
		Map<String, Object> pojoValues = cei.getCfValuesAsValues();
		pojoValues.put("uuid", cei.getUuid());
		return deserialize(pojoValues, pojoClass);
	}
	
	private static String getFieldForGetter(Class<?> clazz, Method getter) {
		String fieldName;
		if(getter.getName().startsWith("is")) {
			fieldName = getter.getName().substring(2);
		} else {
			fieldName = getter.getName().substring(3);
		}
		
		return ReflectionUtils.getAllFields(new ArrayList<>(),clazz)
			.stream()
			.filter(f -> f.getName().toUpperCase().equals(fieldName.toUpperCase()))
			.findFirst()
			.map(Field::getName)
			.orElse(null);
	}
	
	private static void setIdField(Object object, Object id) {
		ReflectionUtils.getAllFields(new ArrayList<>(),object.getClass())
			.stream()
			.filter(f -> f.getAnnotation(Id.class) != null)
			.findFirst()
			.ifPresent(f -> {
				try {
					f.setAccessible(true);
					f.set(object, id);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			});
	}
	
	private static void setUUIDField(Object object, String value) {
		var setter = findSetter("uuid", object.getClass());
		if (setter != null) {
			try {
				setter.invoke(object, value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {

			}
		}
	}
	
	private static Object getIdValue(Object object) {
		return ReflectionUtils.getAllFields(new ArrayList<>(),object.getClass())
				.stream()
				.filter(f -> f.getAnnotation(Id.class) != null)
				.findFirst()
				.map(f -> {
					try {
						f.setAccessible(true);
						return f.get(object);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}).orElse(null);
	}
	
	/**
	 * @param value
	 * @return 
	 * @throws RuntimeException
	 */
	public static <T> T deserialize(Map<String, Object> value, Class<T> clazz) throws RuntimeException {
		try {
			T instance = clazz.getDeclaredConstructor().newInstance();
			for (var entry : value.entrySet()) {
				var setter = findSetter(entry.getKey(), clazz);

				Object lazyInitInstance = null;

				Class<?> paramType = setter.getParameters()[0].getType();

				// if type extends CustomEntity set the UUID
				if (CustomEntity.class.isAssignableFrom(paramType)) {
					lazyInitInstance = paramType
							.getDeclaredConstructor()
							.newInstance();
					setUUIDField(lazyInitInstance, (String) entry.getValue());
					setter.invoke(instance, lazyInitInstance);

				} else {
					try {
						var type = setter.getParameters()[0].getParameterizedType();
						var jacksonType = TypeFactory.defaultInstance().constructType(type);
						var convertedValue = JacksonUtil.convert(entry.getValue(), jacksonType);
						setter.invoke(instance, convertedValue);

					} catch (IllegalArgumentException e) {
						try {
							lazyInitInstance = paramType
									.getDeclaredConstructor()
									.newInstance();
							setIdField(lazyInitInstance, entry.getValue());
							setter.invoke(instance, lazyInitInstance);

						} catch (NoSuchMethodException nm) {
							// convert to factory if there are more types in this group
							if (setter.getParameters()[0].getType().isAssignableFrom(Instant.class)) {
								Instant val = ((Timestamp) entry.getValue()).toInstant();
								setter.invoke(instance, val);
							} else {
								LOG.error("Failed to deserialize {}", entry.getValue(),e);
							}
						}
					}
				}
			}

			return instance;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static Method findSetter(String fieldName, Class<?> clazz) {
		return Stream.of(clazz.getMethods())
			.filter(m -> m.getName().toUpperCase().equals("SET" + fieldName.toUpperCase()))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("No setter found for field " + fieldName + " in " + clazz));
	}

}

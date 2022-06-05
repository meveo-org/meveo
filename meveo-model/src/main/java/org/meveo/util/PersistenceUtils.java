package org.meveo.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.meveo.model.IEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.persistence.DBStorageType;

/**
 * JPA utility classes.
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.8.0
 */
public class PersistenceUtils {

	@SuppressWarnings("unchecked")
	public static <T> T initializeAndUnproxy(T entity) {
		if (entity == null) {
			return null;
			// throw new NullPointerException("Entity passed for initialization is null");
		}

		Hibernate.initialize(entity);
		if (entity instanceof HibernateProxy) {
			entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public static Class<IEntity> getClassForHibernateObject(IEntity object) {
		if (object instanceof HibernateProxy) {
			LazyInitializer lazyInitializer = ((HibernateProxy) object).getHibernateLazyInitializer();
			return lazyInitializer.getPersistentClass();
		} else {
			return (Class<IEntity>) object.getClass();
		}
	}

	/**
	 * Retrieves the name of the jpa field annotated with @Id.
	 * 
	 * @param jpaEntityClazz the jpa entity class
	 * @return name of id field
	 */
	public static String getPKColumnName(Class<?> jpaEntityClazz) {

		if (jpaEntityClazz == null)
			return null;

		String name = null;

		for (Field f : jpaEntityClazz.getDeclaredFields()) {

			Id id = null;
			Column column = null;

			Annotation[] as = f.getAnnotations();
			for (Annotation a : as) {
				if (a.annotationType() == Id.class)
					id = (Id) a;
				else if (a.annotationType() == Column.class)
					column = (Column) a;
			}

			if (id != null && column != null) {
				name = column.name();
				break;
			}
		}

		if (name == null && jpaEntityClazz.getSuperclass() != Object.class)
			name = getPKColumnName(jpaEntityClazz.getSuperclass());

		return name;
	}

	public static String getPKColumnType(Class<?> jpaEntityClass, String fieldName) {

		String dbFieldType = null;
		Optional<Field> optionalField = Arrays.stream(jpaEntityClass.getDeclaredFields()).filter(e -> e.getName().equals(fieldName)).findFirst();

		if (!optionalField.isPresent() && jpaEntityClass.getSuperclass() != Object.class) {
			dbFieldType = getPKColumnType(jpaEntityClass.getSuperclass(), fieldName);

		} else {
			dbFieldType = optionalField.get().getType().getSimpleName();
		}

		return dbFieldType;
	}

	public static String getTableName(Class<?> c) {

		return c.getAnnotation(Table.class).name();
	}
	
	public static Map<String, Object> filterValues(Map<String, CustomFieldTemplate> cfts, Map<String, Object> values, CustomModelObject cet, DBStorageType storageType) {
		return filterValues(cfts, values, cet, storageType, false);
	}

	public static Map<String, Object> filterValues(Map<String, CustomFieldTemplate> cfts, Map<String, Object> values, CustomModelObject cet, DBStorageType storageType, boolean isRequiredOnly) {
		Map<String, Object> filteredValues = new HashMap<>();

		values.entrySet().stream().filter(entry -> {
			// Always include UUID
			if (entry.getKey().equals("uuid")) {
				return true;
			}

			// For CEI storage, always include code
			if (cet instanceof CustomEntityTemplate && entry.getKey().equals("code") && storageType == DBStorageType.SQL && !((CustomEntityTemplate) cet).getSqlStorageConfiguration().isStoreAsTable()) {
				return true;
			}

			CustomFieldTemplate cft = cfts.get(entry.getKey());

			if(cft == null) {
				return false;
			}
			
			if(isRequiredOnly) {
				if(!cft.isValueRequired() && !cft.isUnique()) {
					return false;
				}
			}

			return cft.getStoragesNullSafe().contains(storageType);
		}).filter(v -> v != null && v.getKey() != null)
		.forEach(v -> filteredValues.put(v.getKey(), v.getValue()));

		return filteredValues;
	}
	
	public static List<String> filterFields(Collection<String> fields, Map<String, CustomFieldTemplate> customFieldTemplates, DBStorageType storageType) {
		// If fields are null return all avaiblable fields for the given storage
		if (fields == null) {
			return new ArrayList<>();
		}

		return fields.stream().filter(entry -> {
			CustomFieldTemplate cft = customFieldTemplates.get(entry);
			if (cft == null) {
				return false;
			}
			return cft.getStoragesNullSafe().contains(storageType);
		}).collect(Collectors.toList());
	}
	
	public static Map<String, Set<String>> extractSubFields(final Set<String> actualFetchFields) {
		final Map<String, Set<String>> subFields = new HashMap<>();
		for (var fetchField : List.copyOf(actualFetchFields)) {
			if(fetchField.contains(".")) {
				actualFetchFields.remove(fetchField);
				String[] fieldQuery = fetchField.split("\\.", 2);
				actualFetchFields.add(fieldQuery[0]);
				subFields.computeIfAbsent(fieldQuery[0], key -> new HashSet<>())
					.add(fieldQuery[1]);
			}
		}
		return subFields;
	}

}

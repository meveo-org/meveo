package org.meveo.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.meveo.model.IEntity;

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
}

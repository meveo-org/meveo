package org.meveo.exceptions;

import org.meveo.admin.exception.BusinessException;


/**
 * @author Rachid
 *
 */
public class EntityDoesNotExistsException extends BusinessException {

	private static final long serialVersionUID = 4814463369593237028L;

	public EntityDoesNotExistsException(String entityName, String code) {
		super(entityName + " with code=" + code + " does not exists.");
	}

	public EntityDoesNotExistsException(String message) {
		super(message);
	}

	public EntityDoesNotExistsException(Class<?> clazz, String code) {
		super(clazz.getSimpleName() + " with code=" + code
				+ " does not exists.");
	}

	public EntityDoesNotExistsException(Class<?> clazz, Long id) {
		super(clazz.getSimpleName() + " with id=" + id + " does not exists.");
	}

	public EntityDoesNotExistsException(Class<?> clazz, String value,
			String field) {
		super(clazz.getSimpleName() + " with " + field + "=" + value
				+ " does not exists.");
	}
	
	public EntityDoesNotExistsException(Class<?> clazz, String value1,
			String field1,String value2,String field2) {
		super(clazz.getSimpleName() + " with " + field1 + "=" + value1 +" and " + field2 + "=" + value2+ " does not exists.");
		
	}

}

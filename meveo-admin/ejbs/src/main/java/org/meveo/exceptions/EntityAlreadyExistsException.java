package org.meveo.exceptions;

import org.meveo.admin.exception.BusinessException;


/**
 * @author Rachid
 **/
public class EntityAlreadyExistsException extends BusinessException {

	private static final long serialVersionUID = -979336515558555662L;

	public EntityAlreadyExistsException(String entityName, String code) {
		super(entityName + " with code=" + code + " already exists.");
	}

	public EntityAlreadyExistsException(String message) {
		super(message);
	}

	public EntityAlreadyExistsException(Class<?> clazz, String code) {
		super(clazz.getSimpleName() + " with code=" + code + " already exists.");
	}

	public EntityAlreadyExistsException(Class<?> clazz, String value,
			String field) {
		super(clazz.getSimpleName() + " with " + field + "=" + value
				+ " already exists.");
	}

}

package org.meveo.exceptions;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.BusinessEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityDoesNotExistsException extends BusinessException {

	private static final long serialVersionUID = 4814463369593237028L;
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityDoesNotExistsException.class);
	
	private String code;
	private Long id;
	private Class<?> clazz;
	
	public EntityDoesNotExistsException(BusinessEntity e) {
		super(e.getClass().getSimpleName() + " with code = " + e.getCode() + " does not exists");
		this.id = e.getId();
		this.clazz = e.getClass();
		this.code = e.getCode();
	}

	public EntityDoesNotExistsException(String entityName, String code) {
		super(entityName + " with code=" + code + " does not exists.");
		this.code = code;
	}

	public EntityDoesNotExistsException(String message) {
		super(message);
	}

	public EntityDoesNotExistsException(Class<?> clazz, String code) {
		super(clazz.getSimpleName() + " with code=" + code + " does not exists.");
		this.clazz = clazz;
		this.code = code;
	}

	public EntityDoesNotExistsException(Class<?> clazz, Long id) {
		super(clazz.getSimpleName() + " with id=" + id + " does not exists.");
		this.id = id;
		this.clazz = clazz;
	}

	public EntityDoesNotExistsException(Class<?> clazz, String value, String field) {
		super(clazz.getSimpleName() + " with " + field + "=" + value + " does not exists.");
		this.clazz = clazz;
	}
	
	public EntityDoesNotExistsException(Class<?> clazz, String value1, String field1, String value2, String field2) {
		super(clazz.getSimpleName() + " with " + field1 + "=" + value1 +" and " + field2 + "=" + value2+ " does not exists.");
		this.clazz = clazz;
	}
	
	/**
	 * @param entity the entity to compare
	 * @return {@code true} if the exception concerns the given entity
	 */
	public boolean concerns(BusinessEntity entity) {
		// We can't compare the entities if we don't know the class of the non-existing entity
		if(this.clazz == null) {
			LOGGER.warn("Class of missing entity not provided");
			return false;
		}
		
		// We can't compare the entities if we don't know the id or the code of the non-existing entity
		if(this.code == null && this.id == null) {
			LOGGER.warn("Code and Id of missing entity not provided");
			return false;
		}
		
		// We can't compare the entities if we don't know the id or the code of the entity to compare
		if(entity.getCode() == null && entity.getId() == null) {
			LOGGER.warn("Code and Id of entity to compare not provided");
			return false;
		}
		
		boolean result = this.clazz.isAssignableFrom(entity.getClass());
		
		if(this.code != null && entity.getCode() != null) {
			result = result && entity.getCode().equals(this.code);
		}
		
		if(this.id != null && entity.getId() != null) {
			result = result && entity.getId().equals(this.id);
		}
		
		return result;
	}

}

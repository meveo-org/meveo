/**
 * 
 */
package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;

/**
 * Interface for CRUD events concerning custom entities
 * 
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 * @param <T> Type of custom entity
 */
public interface CrudEventListenerScript<T extends CustomEntity> {
	
	/**
	 * @return the class of the entity
	 */
	Class<T> getEntityClass();

	/**
	 * Called just before entity persistence
	 * 
	 * @param entity entity being persisted
	 */
	void prePersist(T entity);

	/**
	 * Called just before entity update
	 * 
	 * @param entity entity being updated
	 */
	void preUpdate(T entity);

	/**
	 * Called just before entity removal
	 * 
	 * @param entity entity being removed
	 */
	void preRemove(T entity);

	/**
	 * Called just after entity persistence
	 * 
	 * @param entity persisted entity
	 */
	void postPersist(T entity);

	/**
	 * Called just after entity update
	 * 
	 * @param entity updated entity
	 */
	void postUpdate(T entity);

	/**
	 * Called just after entity removal
	 * 
	 * @param entity removed entity
	 */
	void postRemove(T entity);

}

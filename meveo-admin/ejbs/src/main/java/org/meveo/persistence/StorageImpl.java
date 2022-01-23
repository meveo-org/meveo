/**
 * 
 */
package org.meveo.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.storage.Repository;

public interface StorageImpl {
	
	boolean exists(Repository repository, CustomEntityTemplate cet, String uuid);
	
    String findEntityIdByValues(Repository repository, CustomEntityInstance cei);
    
	public Map<String, Object> findById(Repository repository, CustomEntityTemplate cet, String uuid, Map<String, CustomFieldTemplate> cfts, Collection<String> fetchFields, boolean withEntityReferences);
    
	public List<Map<String, Object>> find(StorageQuery query) throws EntityDoesNotExistsException;
	
	PersistenceActionResult createOrUpdate(Repository repository, CustomEntityInstance cei, Map<String, CustomFieldTemplate> customFieldTemplates, String foundUuid) throws BusinessException;

	void update(Repository repository, CustomEntityInstance cei) throws BusinessException;
	
	void setBinaries(Repository repository, CustomEntityTemplate cet, CustomFieldTemplate cft, String uuid, List<String> paths);
	
	public void remove(Repository repository, CustomEntityTemplate cet, String uuid) throws BusinessException;
	
	DBStorageType getStorageType();
}

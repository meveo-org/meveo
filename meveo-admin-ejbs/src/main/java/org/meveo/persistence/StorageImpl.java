/**
 * 
 */
package org.meveo.persistence;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.storage.IStorageConfiguration;
import org.meveo.model.storage.Repository;

public interface StorageImpl {
	
	// -------------------------- CRUD operations -----------------------------
	boolean exists(IStorageConfiguration repository, CustomEntityTemplate cet, String uuid);
	
    default String findEntityIdByValues(Repository repository, IStorageConfiguration conf, CustomEntityInstance cei) {
		StorageQuery query = StorageQuery.fromCei(cei, conf);
		if (query.getFilters().isEmpty()) {
			return null;
		}
		
		try {
			var result = this.find(query);
			if (result.size() == 1) {
				return (String) result.get(0).get("uuid");
			} else if (result.size() > 1) {
				throw new PersistenceException("Many possible entity for values " + query.getFilters().toString());
			}

		} catch (EntityDoesNotExistsException e) {
			throw new PersistenceException("Template does not exists", e);
		}

		return null;
    }
    
	public Map<String, Object> findById(IStorageConfiguration repository, CustomEntityTemplate cet, String uuid, Map<String, CustomFieldTemplate> cfts, Collection<String> fetchFields, boolean withEntityReferences);
    
	public List<Map<String, Object>> find(StorageQuery query) throws EntityDoesNotExistsException;
	
	PersistenceActionResult createOrUpdate(Repository repository, IStorageConfiguration storageConf, CustomEntityInstance cei, Map<String, CustomFieldTemplate> customFieldTemplates, String foundUuid) throws BusinessException;

	PersistenceActionResult addCRTByUuids(IStorageConfiguration repository, CustomRelationshipTemplate crt, Map<String, Object> relationValues, String sourceUuid, String targetUuid) throws BusinessException;

	void update(Repository repository, IStorageConfiguration conf, CustomEntityInstance cei) throws BusinessException;
	
	void setBinaries(IStorageConfiguration repository, CustomEntityTemplate cet, CustomFieldTemplate cft, String uuid, List<File> binaries) throws BusinessException;
	
	public void remove(IStorageConfiguration repository, CustomEntityTemplate cet, String uuid) throws BusinessException;
	
	public Integer count(IStorageConfiguration repository, CustomEntityTemplate cet, PaginationConfiguration paginationConfiguration);
	
	// ----------------- Database schema management ---------------------
	public void cetCreated(CustomEntityTemplate cet);
	
	public void crtCreated(CustomRelationshipTemplate crt) throws BusinessException;
	
	public void cftCreated(CustomModelObject template, CustomFieldTemplate cft);
	
	public void cetUpdated(CustomEntityTemplate oldCet, CustomEntityTemplate cet);
	
	public void crtUpdated(CustomRelationshipTemplate cet) throws BusinessException;
	
	public void cftUpdated(CustomModelObject template, CustomFieldTemplate oldCft, CustomFieldTemplate cft);
	
	public void removeCft(CustomModelObject template, CustomFieldTemplate cft);
	
	public void removeCet(CustomEntityTemplate cet);
	
	public void removeCrt(CustomRelationshipTemplate crt);
	
	// -------------------- Transaction management -----------------
	
	public void init();

	public <T> T beginTransaction(IStorageConfiguration repository, int stackedCalls);
	
	public void commitTransaction(IStorageConfiguration repository);
	
	public void rollbackTransaction(int stackedCalls);
	
	public void destroy();
	
	default public Map<String, Map<String, Object>> findByIds(IStorageConfiguration repository, CustomEntityTemplate cet, List<String> uuids, Map<String, CustomFieldTemplate> cfts, Collection<String> fetchFields, boolean withEntityReferences) {
		Map<String, Map<String, Object>> results = new HashMap<>();
		for (String uuid : uuids) {
			results.put(uuid, findById(repository, cet, uuid, cfts, fetchFields, withEntityReferences));
		}
		return results;
	}
	
}

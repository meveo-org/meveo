/**
 * 
 */
package org.meveo.persistence;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.elresolver.ELException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.storage.Repository;

public interface StorageImpl {
	
	// -------------------------- CRUD operations -----------------------------
	boolean exists(Repository repository, CustomEntityTemplate cet, String uuid);
	
    String findEntityIdByValues(Repository repository, CustomEntityInstance cei);
    
	public Map<String, Object> findById(Repository repository, CustomEntityTemplate cet, String uuid, Map<String, CustomFieldTemplate> cfts, Collection<String> fetchFields, boolean withEntityReferences);
    
	public List<Map<String, Object>> find(StorageQuery query) throws EntityDoesNotExistsException;
	
	PersistenceActionResult createOrUpdate(Repository repository, CustomEntityInstance cei, Map<String, CustomFieldTemplate> customFieldTemplates, String foundUuid) throws BusinessException;

	PersistenceActionResult addCRTByUuids(Repository repository, CustomRelationshipTemplate crt, Map<String, Object> relationValues, String sourceUuid, String targetUuid) throws BusinessException;

	void update(Repository repository, CustomEntityInstance cei) throws BusinessException;
	
	void setBinaries(Repository repository, CustomEntityTemplate cet, CustomFieldTemplate cft, String uuid, List<File> binaries) throws BusinessException;
	
	public void remove(Repository repository, CustomEntityTemplate cet, String uuid) throws BusinessException;
	
	public Integer count(Repository repository, CustomEntityTemplate cet, PaginationConfiguration paginationConfiguration);
	
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

	public <T> T beginTransaction(Repository repository, int stackedCalls);
	
	public void commitTransaction(Repository repository);
	
	public void rollbackTransaction(int stackedCalls);
	
	public void destroy();
	
}

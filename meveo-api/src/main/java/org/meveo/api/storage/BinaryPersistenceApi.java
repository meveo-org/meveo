/**
 * 
 */
package org.meveo.api.storage;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.customEntities.BinaryProvider;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.storage.Repository;
import org.meveo.service.storage.RepositoryService;

public class BinaryPersistenceApi {
	
	@Inject
	private CrossStorageApi crossStorageApi;
	
	@Inject	
	private RepositoryService repositoryService;

	@Transactional
	public List<BinaryProvider> getBinaries(String repositoryCode, String cetCode, String uuid, String cftCode) throws EntityDoesNotExistsException {
		Repository repository = repositoryService.findByCode(repositoryCode);
		
		CustomEntityInstance cei = crossStorageApi.find(repository, cetCode)
			.id(uuid)
			.select(cftCode)
			.getResult();
		
		return new ArrayList<BinaryProvider>(cei.getCfValues().getCfValue(cftCode).getBinaries());
	}
	
}

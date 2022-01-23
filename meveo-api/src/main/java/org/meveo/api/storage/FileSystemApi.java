package org.meveo.api.storage;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.api.BaseApi;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.meveo.service.storage.BinaryStorageUtils;
import org.meveo.service.storage.FileSystemService;
import org.meveo.service.storage.RepositoryService;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @author clement.bareth
 */
@Stateless
public class FileSystemApi extends BaseApi {

	@Inject
	private FileSystemService fileSystemService;

	@Inject
	private CustomFieldsCacheContainerProvider cache;

	@Inject
	private RepositoryService repositoryService;

	@Inject
	private CrossStorageService crossStorageService;

	public File findBinary(String repositoryCode, String cetCode, String uuid, String cftCode, Integer index) throws EntityDoesNotExistsException, BusinessApiException, IOException, org.meveo.api.exception.EntityDoesNotExistsException {

		// Retrieve repository
		Repository repository = repositoryService.findByCode(repositoryCode);
		if(repository == null){
			throw new EntityDoesNotExistsException(Repository.class, repositoryCode);
		}
		
		// Retrieve CFT
		CustomFieldTemplate cft = cache.getCustomFieldTemplate(cftCode, CustomEntityTemplate.getAppliesTo(cetCode));
		if (cft == null) {
			throw new EntityDoesNotExistsException(CustomFieldTemplate.class, cftCode);
		}

		// The file path expression contains an EL, so we need to retrieve the entity to get the evaluated file path
		if(BinaryStorageUtils.filePathContainsEL(cft)){
			// Retrieve CET
			CustomEntityTemplate cet = cache.getCustomEntityTemplate(cetCode);
			if (cet == null) {
				throw new EntityDoesNotExistsException(CustomEntityTemplate.class, cetCode);
			}

			// Retrieve the entity
			Map<String, Object> values = crossStorageService.findById(repository, 
					cet, 
					uuid, 
					Collections.singletonList(cftCode),
					new HashMap<>(),
					true);
			if(values == null){
				throw new EntityDoesNotExistsException("EntityInstance", uuid);
			}
			
			return fileSystemService.findBinaryDynamicPath(values, cftCode, index);

		// The file path expression does not contains an EL, so we can re-build the path to the desired binary
		} else {
			return fileSystemService.findBinaryStaticPath(repository, cetCode, uuid, cft, index);
		}

	}

}

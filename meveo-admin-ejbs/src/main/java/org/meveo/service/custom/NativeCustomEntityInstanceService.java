package org.meveo.service.custom;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.model.crm.custom.CustomFieldValue;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.meveo.service.base.NativePersistenceService;

/**
 * Service class for managing a {@link CustomEntityInstance}. This class extends
 * the {@link NativePersistenceService} so that it can execute the query on a
 * given datasource provided by sql connection code.
 * 
 * @see SqlConfiguration
 * @see NativePersistenceService
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.7.0
 * @version 6.7.0
 */
@Stateless
public class NativeCustomEntityInstanceService extends NativePersistenceService {

	@Inject
	private CrossStorageService crossStorageService;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void createOrUpdate(Repository repository, CustomEntityInstance entity, Map<String, List<CustomFieldValue>> cfValues)
			throws BusinessApiException, EntityDoesNotExistsException, BusinessException, IOException {

		CustomEntityInstance ceiToSave = new CustomEntityInstance();
		ceiToSave.setUuid(entity.getUuid());
		ceiToSave.setCet(entity.getCet());
		ceiToSave.setCode(entity.getCode());
		ceiToSave.setCetCode(entity.getCetCode());
		ceiToSave.getCfValuesNullSafe().setValuesByCode(cfValues);
		ceiToSave.setDescription(entity.getDescription());

		createOrUpdateInCrossStorage(repository, ceiToSave);
	}

	public void createOrUpdateInCrossStorage(Repository repository, CustomEntityInstance entity)
			throws BusinessApiException, EntityDoesNotExistsException, BusinessException, IOException {
		crossStorageService.createOrUpdate(repository, entity);
	}

	public void removeInCrossStorage(Repository repository, CustomEntityTemplate cet, String uuid) throws BusinessException {
		crossStorageService.remove(repository, cet, uuid);
	}

	public Map<String, Object> findInCrossStorage(Repository repository, CustomEntityTemplate cet, String uuid) throws EntityDoesNotExistsException {
		return crossStorageService.find(repository, cet, uuid, true);
	}
	
}

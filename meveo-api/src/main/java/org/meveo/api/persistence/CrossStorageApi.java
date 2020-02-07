/**
 * 
 */
package org.meveo.api.persistence;

import java.io.IOException;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.CEIUtils;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;

/**
 * The Class CrossStorageApi.
 *
 * @author clement.bareth
 * @version 6.8.0
 * @since 6.8.0
 */
@Stateless
public class CrossStorageApi{

	@Inject
	private CrossStorageService crossStorageService;
	
	@Inject
	private CustomFieldsCacheContainerProvider cache;
	
	/**
	 * Find an instance of a given CET
	 *
	 * @param repository the repository where the instance is stored
	 * @param uuid       the uuid of the instance
	 * @param clazz      the clazz of the cet's type
	 * @return the instanc of the cet
	 * @throws EntityDoesNotExistsException the entity does not exists exception
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <T> T find(Repository repository, String uuid, Class<T> cetClass) throws EntityDoesNotExistsException {
		CustomEntityTemplate cet = getCet(cetClass);
		Map<String, Object> values = crossStorageService.find(repository, cet, uuid, true);
		return JacksonUtil.convert(values, cetClass);
	}

	/**
	 * 
	 * @param repository the repository where to save data
	 * @param value      the data to save
	 * @throws BusinessApiException         See {@link CrossStorageService#createOrUpdate(Repository, org.meveo.model.customEntities.CustomEntityInstance)}
	 * @throws EntityDoesNotExistsException See {@link CrossStorageService#createOrUpdate(Repository, org.meveo.model.customEntities.CustomEntityInstance)}
	 * @throws BusinessException            See {@link CrossStorageService#createOrUpdate(Repository, org.meveo.model.customEntities.CustomEntityInstance)}
	 * @throws IOException                  See {@link CrossStorageService#createOrUpdate(Repository, org.meveo.model.customEntities.CustomEntityInstance)}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void createOrUpdate(Repository repository, Object value) throws BusinessApiException, EntityDoesNotExistsException, BusinessException, IOException {
		CustomEntityInstance cei = CEIUtils.pojoToCei(value);
		crossStorageService.createOrUpdate(repository, cei);
	}
	
	/**
	 * @param cetClass
	 * @return
	 * @throws IllegalArgumentException
	 */
	private CustomEntityTemplate getCet(Class<?> cetClass) throws IllegalArgumentException {
		CustomEntityTemplate cet = cache.getCustomEntityTemplate(cetClass.getSimpleName());
		if (cet == null) {
			throw new IllegalArgumentException("CET " + cetClass.getSimpleName() + " does not exists");
		}
		return cet;
	}

}

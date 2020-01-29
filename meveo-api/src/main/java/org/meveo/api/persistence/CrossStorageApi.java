/**
 * 
 */
package org.meveo.api.persistence;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
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
public class CrossStorageApi<T> {

	private CrossStorageService crossStorageService;
	private CustomFieldsCacheContainerProvider cache;
	private Class<T> clazz;
	private CustomEntityTemplate cet;
	
	public CrossStorageApi(CrossStorageService crossStorageService, CustomFieldsCacheContainerProvider cache, Class<T> clazz) {
		this.crossStorageService = crossStorageService;
		this.cache = cache;
		this.clazz = clazz;
		this.cet = cache.getCustomEntityTemplate(clazz.getSimpleName());
	}

	/**
	 * Find an instance of a given CET
	 *
	 * @param repository the repository where the instance is stored
	 * @param uuid       the uuid of the instance
	 * @param clazz      the clazz of the cet's type
	 * @return the instanc of the cet
	 * @throws EntityDoesNotExistsException the entity does not exists exception
	 */
	public T find(Repository repository, String uuid) throws EntityDoesNotExistsException {
		if (cet == null) {
			throw new IllegalArgumentException("CET " + clazz.getSimpleName() + " does not exists");
		}
		
		Map<String, Object> values = crossStorageService.find(repository, cet, uuid);
		return JacksonUtil.convert(values, clazz);
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
	public void createOrUpdate(Repository repository, T value) throws BusinessApiException, EntityDoesNotExistsException, BusinessException, IOException {
		if (cet == null) {
			throw new IllegalArgumentException("CET " + clazz.getSimpleName() + " does not exists");
		}
		
		crossStorageService.createOrUpdate(repository, CEIUtils.pojoToCei(value));
	}

}

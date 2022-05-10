/**
 * 
 */
package org.meveo.api.persistence;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.elresolver.ELException;
import org.meveo.interfaces.EntityGraph;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.CEIUtils;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.meveo.persistence.scheduler.AtomicPersistencePlan;
import org.meveo.persistence.scheduler.CyclicDependencyException;
import org.meveo.persistence.scheduler.OrderedPersistenceService;
import org.meveo.persistence.scheduler.PersistedItem;
import org.meveo.persistence.scheduler.SchedulingService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.storage.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class CrossStorageApi.
 *
 * @author clement.bareth
 * @version 6.15
 * @since 6.8.0
 */
public class CrossStorageApi{

	@Inject
	private CrossStorageService crossStorageService;
	
	@Inject
	private CustomEntityTemplateService cetService;
	
    @Inject
    protected SchedulingService schedulingService;
    
    @Inject
    protected OrderedPersistenceService<CrossStorageService> scheduledPersistenceService;
	
	@Inject
	private CustomFieldsCacheContainerProvider cache;
	
	@Inject
	private RepositoryService repositoryService;
	
	
    protected static final Logger LOGGER = LoggerFactory.getLogger(CrossStorageApi.class);
    
	public <T> CrossStorageRequest<T> find(Class<T> cetClass) {
		return find(repositoryService.findDefaultRepository(), cetClass);
	}
	
	public <T> CrossStorageRequest<T> find(Repository repository, Class<T> cetClass) {
		return new CrossStorageRequest<T>(repository, crossStorageService, cetClass, getCet(cetClass));
	}
	
	public CrossStorageRequest<CustomEntityInstance> find(Repository repository, String cetCode) {
		CustomEntityTemplate cet = cetService.findByCode(cetCode);
		if(cet == null) {
			throw new IllegalArgumentException("Cet with code " + cetCode + " does not exists");
		}
		
		return new CrossStorageRequest<CustomEntityInstance>(repository, crossStorageService, CustomEntityInstance.class, cet);
	}
	
    public List<PersistedItem> persistEntities(Repository repository, EntityGraph entityGraph) throws CyclicDependencyException, ELException, EntityDoesNotExistsException, IOException, BusinessApiException, BusinessException {
        AtomicPersistencePlan atomicPersistencePlan = schedulingService.schedule(entityGraph.getAll());
        return scheduledPersistenceService.persist(repository.getCode(), atomicPersistencePlan);
    }
	
	/**
	 * Find an instance of a given CET
	 *
	 * @param repository the repository where the instance is stored
	 * @param uuid       the uuid of the instance
	 * @param cetClass   the clazz of the cet's type
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
	 * Find an instance of a given CET
	 *
	 * @param uuid       the uuid of the instance
	 * @param cetClass   the clazz of the cet's type
	 * @return the instanc of the cet
	 * @throws EntityDoesNotExistsException the entity does not exists exception
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <T> T find(String uuid, Class<T> cetClass) throws EntityDoesNotExistsException {
		return find(repositoryService.findDefaultRepository(), uuid, cetClass);
	}
//	/**
//	 * Find an instance (Pojo) of a given CET
//	 *
//	 * @param repository the repository where the instance is stored
//	 * @param uuid       the uuid of the instance
//	 * @param cetCode    the code of the ce
//	 * @return the instanc of the cet (package org.meveo.model.customentities)
//	 * @throws EntityDoesNotExistsException the entity does not exists exception
//	 */
//	@TransactionAttribute(TransactionAttributeType.REQUIRED)
//	public Object findPojo(Repository repository, String uuid, String cetCode) throws EntityDoesNotExistsException {
//		try {
//			Class<?> object = customScriptService.loadCustomEntityClass(cetCode, false, Optional.empty());
//			return find(repository, uuid, Class.forName("org.meveo.model.customEntities." + cetCode));
//		} catch (ClassNotFoundException | CharSequenceCompilerException e) {
//			throw new EntityDoesNotExistsException(e.getClass().getSimpleName() + " for cet: " + cetCode);
//		}
//		
//	}

	/**
	 * Find an instance of a given CET
	 *
	 * @param repository the repository where the instance is stored
	 * @param uuid       the uuid of the instance
	 * @param cetCode    the code of the ce
	 * @return the instance (CustomEntityInstance) of the cet
	 * @throws EntityDoesNotExistsException the entity does not exists exception
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public CustomEntityInstance find(Repository repository, String uuid, String cetCode) throws EntityDoesNotExistsException {
		CustomEntityTemplate cet = cache.getCustomEntityTemplate(cetCode);
		Map<String, Object> values = crossStorageService.find(repository, cet, uuid, true);
		var cei = CEIUtils.pojoToCei(values);
		cei.setCetCode(cetCode);
		cei.setCet(cet);
		return cei;
	}
	
	/**
	 * Find an instance of a given CET
	 *
	 * @param repository the repository where the instance is stored
	 * @param uuid       the uuid of the instance
	 * @param cetCode    the code of the ce
	 * @return the instance (CustomEntityInstance) of the cet
	 * @throws EntityDoesNotExistsException the entity does not exists exception
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public CustomEntityInstance find(String uuid, String cetCode) throws EntityDoesNotExistsException {
		return find(repositoryService.findDefaultRepository(), uuid, cetCode);
	}

	/**
	 * 
	 * @param repository the repository where to save data
	 * @param value      the data to save
	 * @return the UUID of the created / updated data
	 * @throws BusinessApiException         See {@link CrossStorageService#createOrUpdate(Repository, org.meveo.model.customEntities.CustomEntityInstance)}
	 * @throws EntityDoesNotExistsException See {@link CrossStorageService#createOrUpdate(Repository, org.meveo.model.customEntities.CustomEntityInstance)}
	 * @throws BusinessException            See {@link CrossStorageService#createOrUpdate(Repository, org.meveo.model.customEntities.CustomEntityInstance)}
	 * @throws IOException                  See {@link CrossStorageService#createOrUpdate(Repository, org.meveo.model.customEntities.CustomEntityInstance)}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String createOrUpdate(Repository repository, Object value) throws BusinessApiException, EntityDoesNotExistsException, BusinessException, IOException {
		CustomEntityInstance cei = value instanceof CustomEntityInstance
				? (CustomEntityInstance) value 
				: CEIUtils.pojoToCei(value);
				
		var result = crossStorageService.createOrUpdate(repository, cei);
		return result.getBaseEntityUuid();
	}
	
	/**
	 * 
	 * @param value      the data to save
	 * @return the UUID of the created / updated data
	 * @throws BusinessApiException         See {@link CrossStorageService#createOrUpdate(Repository, org.meveo.model.customEntities.CustomEntityInstance)}
	 * @throws EntityDoesNotExistsException See {@link CrossStorageService#createOrUpdate(Repository, org.meveo.model.customEntities.CustomEntityInstance)}
	 * @throws BusinessException            See {@link CrossStorageService#createOrUpdate(Repository, org.meveo.model.customEntities.CustomEntityInstance)}
	 * @throws IOException                  See {@link CrossStorageService#createOrUpdate(Repository, org.meveo.model.customEntities.CustomEntityInstance)}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String createOrUpdate(Object value) throws BusinessApiException, EntityDoesNotExistsException, BusinessException, IOException {
		return createOrUpdate(repositoryService.findDefaultRepository(), value);
	}
	
	/**
	 * Remove an instance of a given CET
	 * 
	 * @param repository the repository where the entity is stored
	 * @param uuid       the uuid of the instance
	 * @param cetCode 	 code of the cet
	 * @throws BusinessException if error occurs
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void remove(Repository repository, String uuid, String cetCode) throws BusinessException {
		crossStorageService.remove(repository, cache.getCustomEntityTemplate(cetCode), uuid);
	}
	
	/**
	 * Remove an instance of a given CET
	 *
	 * @param uuid       the uuid of the instance
	 * @param cetCode 	 code of the cet
	 * @throws BusinessException if error occurs
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void remove(String uuid, String cetCode) throws BusinessException {
		remove(repositoryService.findDefaultRepository(), uuid, cetCode);
	}
	
	/**
	 * Remove an instance of a given CET
	 *
	 * @param repository the repository where the instance is stored
	 * @param uuid       the uuid of the instance
	 * @param cetClass   the clazz of the cet's type
	 * @throws BusinessException if error occurs
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void remove(Repository repository, String uuid, Class<?> cetClass) throws BusinessException {
		crossStorageService.remove(repository, getCet(cetClass), uuid);
	}
	
	/**
	 * Remove an instance of a given CET
	 *
	 * @param uuid       the uuid of the instance
	 * @param cetClass   the clazz of the cet's type
	 * @throws BusinessException if error occurs
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void remove(String uuid, Class<?> cetClass) throws BusinessException {
		remove(repositoryService.findDefaultRepository(), uuid, cetClass);
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

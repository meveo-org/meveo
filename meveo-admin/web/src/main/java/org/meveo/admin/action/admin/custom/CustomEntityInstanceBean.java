package org.meveo.admin.action.admin.custom;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.CustomFieldBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.elresolver.ELException;
import org.meveo.jpa.CurrentRepositoryProvider;
import org.meveo.model.crm.custom.CustomFieldValue;
import org.meveo.model.crm.custom.CustomFieldValueHolder;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomizedEntity;
import org.meveo.service.custom.CustomizedEntityService;
import org.meveo.service.storage.RepositoryService;
import org.omnifaces.cdi.Cookie;
import org.slf4j.Logger;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Named
@ViewScoped
public class CustomEntityInstanceBean extends CustomFieldBean<CustomEntityInstance> {

	private static final long serialVersionUID = -459772193950603406L;
	
	@Inject
	private Logger log;

	@Inject
	private CustomizedEntityService customizedEntityService;
	
	@Inject
	protected CrossStorageService crossStorageService;

	@Inject
	protected CustomEntityInstanceService customEntityInstanceService;

	@Inject
	protected CustomEntityTemplateService customEntityTemplateService;

	@Inject
	private CustomFieldInstanceService customFieldInstanceService;

	@Inject
	protected RepositoryService repositoryService;

	@Inject
	private CustomFieldsCacheContainerProvider cacheContainerProvider;
	
	@Inject
	protected CurrentRepositoryProvider repositoryProvider;

	protected CustomEntityTemplate customEntityTemplate;
	
	private Repository repository;
	
	protected String customEntityTemplateCode;
	protected String customTableName;
	private String uuid;
	
	@Inject @Cookie(name = "repository")
	private String repositoryCode;

	public CustomEntityInstanceBean() {
		super(CustomEntityInstance.class);
	}

	@Override
	protected IPersistenceService<CustomEntityInstance> getPersistenceService() {
		return customEntityInstanceService;
	}

	@Override
	public CustomEntityInstance initEntity() {
		repository = repositoryService.findByCode(repositoryCode);

		customEntityTemplate = cacheContainerProvider.getCustomEntityTemplate(customEntityTemplateCode);

		entity = new CustomEntityInstance();
		entity.setCetCode(customEntityTemplateCode);

		if (!StringUtils.isBlank(uuid)) {
			try {
				Map<String, Object> cfValues = crossStorageService.find(repository, customEntityTemplate, uuid, true);

				if (cfValues != null) {
					log.debug("Loading cfValues={}", cfValues);
					entity.setCode((String) cfValues.get("code"));
					entity.setCet(customEntityTemplate);
					entity.setDescription((String) cfValues.get("description"));
					entity.setUuid(uuid);

					customFieldInstanceService.setCfValues(entity, customEntityTemplateCode, cfValues);
				}

			} catch (EntityDoesNotExistsException | BusinessException e) {
				log.error(e.getMessage());
			}
		}

		return entity;
	}

	@Override
	public String getEditViewName() {
		return "customEntity";
	}

	@Override
	public String getListViewName() {
		return "customEntities";
	}

	@Override
	@ActionMethod
	public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {

		if (StringUtils.isBlank(entity.getCetCode())) {
			messages.error(new BundleKey("messages", "customEntityInstance.noCetCodeSet"));
			return null;
		}

		String result = getListViewName();

		boolean isNew = StringUtils.isBlank(uuid);

		try {

			Map<String, List<CustomFieldValue>> cfValues = customFieldDataEntryBean.getFieldValueHolderByUUID(entity.getUuid()).getValuesByCode();
			
			String message = entity.isTransient() ? "save.successful" : "update.successful";
			entity.setCfValues(new CustomFieldValues(cfValues));
			
			crossStorageService.createOrUpdate(repository, entity);

			if (killConversation) {
				endConversation();
			}

			messages.info(new BundleKey("messages", message));

			// Delete old binaries
			for (String fileToDelete : customFieldDataEntryBean.getFilesToDeleteOnExit()) {
				File file = new File(fileToDelete);
				if (file.exists()) {
					file.delete();
				}
			}

		} catch (Exception e) {
			messages.error(new BundleKey("messages", "customEntityInstance.save.ko"));
			log.error("Canno't create or update CEI", e);
			return null;
		}

		return result;
	}

	@Override
	public void delete() throws BusinessException {
		repository = repository == null ? repositoryService.findByCode(repositoryCode) : repository;
		String cetCode = org.meveo.commons.utils.StringUtils.isBlank(customEntityTemplateCode) ? entity.getCetCode() : customEntityTemplateCode;
		crossStorageService.remove(repository, customEntityTemplateService.findByCode(cetCode), uuid);
		messages.info(new BundleKey("messages", "delete.successful"));
	}

	public void updateInBaseBean() {

		Map<String, List<CustomFieldValue>> newValuesByCode = new HashMap<>();

		CustomFieldValueHolder entityFieldsValues = customFieldDataEntryBean.getFieldValueHolderByUUID(entity.getUuid());
		GroupedCustomField groupedCustomFields = customFieldDataEntryBean.groupedFieldTemplates.get(entity.getUuid());
	}

	public String getSqlConnectionCode() {
		return repository == null ? null : repository.getSqlConfigurationCode();
	}

	public CustomEntityTemplate getCustomEntityTemplateNullSafe() {

		if (customEntityTemplate == null && customEntityTemplateCode != null) {
			customEntityTemplate = customEntityTemplateService.findByCode(customEntityTemplateCode);
		}
		return customEntityTemplate;

	}

	public CustomEntityTemplate getCustomEntityTemplate() {
		return customEntityTemplate;
	}

	@Override
	protected Map<String, Object> supplementSearchCriteria(Map<String, Object> searchCriteria) {

		searchCriteria.put("cetCode", customEntityTemplateCode);
		return searchCriteria;
	}

	public List<String> autocompleteClassNamesHuman(String input) {
		return customizedEntityService.getCustomizedEntities(input, true, true, true, "code", "ASC", false).stream().map(CustomizedEntity::getEntityCode)
				.collect(Collectors.toList());
	}

	public void setCustomEntityTemplateCode(String customEntityTemplateCode) {
		this.customEntityTemplateCode = customEntityTemplateCode;
	}

	public String getCustomEntityTemplateCode() {
		if(customEntityTemplateCode == null && getCustomEntityTemplate() != null) {
			customEntityTemplateCode = getCustomEntityTemplate().getCode();
		}
		
		return customEntityTemplateCode;
	}

	public Repository getRepository() {
		if(repository == null && repositoryCode != null) {
			repository = this.repositoryService.findByCode(repositoryCode);
			repositoryProvider.setRepository(repository);
		}
		
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public String getCustomTableName() {
		return customTableName;
	}

	public void setCustomTableName(String customTableName) {
		this.customTableName = customTableName;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getRepositoryCode() {
		return repositoryCode;
	}

	public void setRepositoryCode(String repositoryCode) {
		this.repositoryCode = repositoryCode;
	}

	public void setCustomEntityTemplate(CustomEntityTemplate customEntityTemplate) {
		this.customEntityTemplate = customEntityTemplate;
	}
}
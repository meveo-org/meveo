package org.meveo.admin.action.admin.custom;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.CustomFieldBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.elresolver.ELException;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValue;
import org.meveo.model.crm.custom.CustomFieldValueHolder;
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

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Named
@ViewScoped
public class CustomEntityInstanceBean extends CustomFieldBean<CustomEntityInstance> {

	private static final long serialVersionUID = -459772193950603406L;

	@Inject
	private CustomizedEntityService customizedEntityService;

	@Inject
	private CustomEntityInstanceService customEntityInstanceService;

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

	@Inject
	private CustomFieldInstanceService customFieldInstanceService;

	@Inject
	private transient CrossStorageService crossStorageService;

	@Inject
	private RepositoryService repositoryService;

	private CustomEntityTemplate customEntityTemplate;
	private String customEntityTemplateCode;
	private Repository defaultRepository;

	public CustomEntityInstanceBean() {
		super(CustomEntityInstance.class);
	}

	@Override
	protected IPersistenceService<CustomEntityInstance> getPersistenceService() {
		return customEntityInstanceService;
	}

	@Override
	public CustomEntityInstance initEntity() {

		CustomEntityInstance entity = super.initEntity();

		customEntityTemplate = customEntityTemplateService.findByCode(customEntityTemplateCode);

		defaultRepository = repositoryService.findDefaultRepository();
		try {
			Map<String, Object> cfValues = crossStorageService.find(defaultRepository, customEntityTemplate, entity.getUuid());
			customFieldInstanceService.setCfValues(entity, customEntityTemplateCode, cfValues);
			entity.setCetCode(customEntityTemplateCode);

		} catch (EntityDoesNotExistsException | BusinessException e) {
			log.error(e.getMessage());
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

		// Check for unicity of code
		CustomEntityInstance ceiSameCode = customEntityInstanceService.findByCodeByCet(entity.getCetCode(), entity.getCode());
		if ((entity.isTransient() && ceiSameCode != null)
				|| (!entity.isTransient() && ceiSameCode != null && ceiSameCode.getId() != null && entity.getId().longValue() != ceiSameCode.getId().longValue())) {
			messages.error(new BundleKey("messages", "commons.uniqueField.code"));
			return null;
		}

		try {
			boolean isNew = entity.isTransient();
	        customFieldDataEntryBean.saveCustomFieldsToEntity((ICustomFieldEntity) entity, isNew);
	        
			customEntityInstanceService.createOrUpdateInCrossStorage(defaultRepository, entity);

			ceiSameCode = customEntityInstanceService.findByCodeByCet(entity.getCetCode(), entity.getCode());
			Map<String, Object> fieldValues = new HashMap<>();
			Map<String, CustomFieldTemplate> customFieldTemplates = customFieldTemplateService.findByAppliesTo(ceiSameCode);
			for (Iterator<CustomFieldTemplate> iterator = customFieldTemplates.values().iterator(); iterator.hasNext();) {
				CustomFieldTemplate cft = iterator.next();
				if (cft.getFieldType() != CustomFieldTypeEnum.CHILD_ENTITY) {
					Object value = customFieldInstanceService.getCFValue(ceiSameCode, cft.getCode());
					log.info("value : {}", value);
					log.info("Code of cft : {}", cft.getCode());
					fieldValues.put(cft.getCode(), value);
				}
			}

			if (!fieldValues.isEmpty()) {
				log.info("fieldValues : {}", fieldValues);
			}

			// Delete old binaries
			for (String fileToDelete : customFieldDataEntryBean.getFilesToDeleteOnExit()) {
				File file = new File(fileToDelete);
				if (file.exists()) {
					file.delete();
				}
			}

		} catch (BusinessApiException | EntityDoesNotExistsException | IOException e) {
			messages.error(new BundleKey("messages", "customEntityInstance.save.ko"));
			return null;
		}

		endConversation();
		return getListViewName();
	}

	@Override
	public void delete() throws BusinessException {
		
		customEntityInstanceService.removeInCrossStorage(defaultRepository, entity.getCet(), entity.getUuid());
		messages.info(new BundleKey("messages", "delete.successful"));
	}

	public void updateInBaseBean() {

		Map<String, List<CustomFieldValue>> newValuesByCode = new HashMap<>();

		CustomFieldValueHolder entityFieldsValues = customFieldDataEntryBean.getFieldValueHolderByUUID(entity.getUuid());
		GroupedCustomField groupedCustomFields = customFieldDataEntryBean.groupedFieldTemplates.get(entity.getUuid());
	}

	public CustomEntityTemplate getCustomEntityTemplate() {

		if (customEntityTemplate == null && customEntityTemplateCode != null) {
			customEntityTemplate = customEntityTemplateService.findByCode(customEntityTemplateCode);
		}
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
		return customEntityTemplateCode;
	}
}
package org.meveo.admin.action.admin.custom;

import java.io.File;
import java.util.HashMap;
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
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.elresolver.ELException;
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

		if (entity.isTransient()) {
			entity.setCetCode(customEntityTemplateCode);
			entity.setCet(customEntityTemplate);

		} else {
			try {
				Map<String, Object> cfValues = customEntityInstanceService.findInCrossStorage(defaultRepository, customEntityTemplate, entity.getUuid());
				log.debug("Loading cfValues={}", cfValues);
				if (cfValues != null) {
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
		// Check for unicity of code
		CustomEntityInstance ceiSameCode = customEntityInstanceService.findByCodeByCet(entity.getCetCode(), entity.getCode());
		if ((entity.isTransient() && ceiSameCode != null)
				|| (!entity.isTransient() && ceiSameCode != null && ceiSameCode.getId() != null && entity.getId().longValue() != ceiSameCode.getId().longValue())) {
			messages.error(new BundleKey("messages", "commons.uniqueField.code"));
			return null;
		}

		boolean isNew = entity.isTransient();

		try {

			Map<String, List<CustomFieldValue>> cfValues = customFieldDataEntryBean.saveCustomFieldsToEntity(entity, isNew, false);

			String message = entity.isTransient() ? "save.successful" : "update.successful";

			customEntityInstanceService.createOrUpdate(defaultRepository, entity, cfValues);

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
			return null;
		}

		return result;
	}

	@Override
	public void delete() throws BusinessException {

		String cetCode = org.meveo.commons.utils.StringUtils.isBlank(customEntityTemplateCode) ? entity.getCetCode() : customEntityTemplateCode;
		customEntityInstanceService.removeInCrossStorage(repositoryService.findDefaultRepository(), customEntityTemplateService.findByCode(cetCode), entity.getUuid());
		messages.info(new BundleKey("messages", "delete.successful"));
	}

	public void updateInBaseBean() {

		Map<String, List<CustomFieldValue>> newValuesByCode = new HashMap<>();

		CustomFieldValueHolder entityFieldsValues = customFieldDataEntryBean.getFieldValueHolderByUUID(entity.getUuid());
		GroupedCustomField groupedCustomFields = customFieldDataEntryBean.groupedFieldTemplates.get(entity.getUuid());
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
		return customEntityTemplateCode;
	}
}
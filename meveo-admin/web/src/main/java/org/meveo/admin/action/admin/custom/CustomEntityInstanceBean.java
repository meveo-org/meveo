package org.meveo.admin.action.admin.custom;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.CustomFieldBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.IllegalTransitionException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.elresolver.ELException;
import org.meveo.jpa.CurrentRepositoryProvider;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.IEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.EntityReferenceWrapper;
import org.meveo.model.crm.custom.CustomFieldValue;
import org.meveo.model.crm.custom.CustomFieldValueHolder;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.CEIUtils;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.storage.Repository;
import org.meveo.model.util.KeyValuePair;
import org.meveo.persistence.CrossStorageService;
import org.meveo.service.base.MeveoValueExpressionWrapper;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomizedEntity;
import org.meveo.service.custom.CustomizedEntityService;
import org.meveo.service.custom.EntityCustomActionService;
import org.meveo.service.script.CustomScriptService;
import org.meveo.service.script.Script;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.storage.RepositoryService;
import org.meveo.util.view.CrossStorageDataModel;
import org.omnifaces.cdi.Cookie;
import org.primefaces.PrimeFaces;
import org.primefaces.model.LazyDataModel;
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
	protected transient CrossStorageService crossStorageService;

	@Inject
	protected CustomEntityInstanceService customEntityInstanceService;

	@Inject
	protected CustomEntityTemplateService customEntityTemplateService;

	@Inject
	protected CustomFieldInstanceService customFieldInstanceService;

	@Inject
	protected RepositoryService repositoryService;

	@Inject
	private CustomFieldsCacheContainerProvider cacheContainerProvider;

	@Inject
	protected CurrentRepositoryProvider repositoryProvider;
	
	@Inject
	private transient EntityCustomActionService entityActionScriptService;

	@Inject
	private transient ScriptInstanceService scriptInstanceService;
	
	private Map<String, Boolean> secretToDisplayInClear = new HashMap<>();

	private LazyDataModel<Map<String, Object>> nativeDataModel;
	protected CustomEntityTemplate customEntityTemplate;
	protected Repository repository;

	protected String customEntityTemplateCode;
	protected String customTableName;
	private String uuid;
	private String hash;
	
	protected EntityCustomAction action;
	protected Set<KeyValuePair> overrideParams = new HashSet<>();

	@Inject
	@Cookie(name = "repository")
	protected String repositoryCode;

	private Map<String, CustomFieldTemplate> customFieldTemplates;

	public CustomEntityInstanceBean() {
		super(CustomEntityInstance.class);
	}
	
	/**
	 * @return the {@link #action}
	 */
	public EntityCustomAction getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(EntityCustomAction action) {
		if(action == null) {
			return;
		}
		
		if(action == this.action) {
			return;
		}
		
		this.action = action;
		
		Map<Object, Object> elContext = new HashMap<>();
		elContext.put("entity", entity);
		
		overrideParams.clear();
		this.action.getScriptParameters().forEach((key, value) -> {
			try {
				overrideParams.add(new KeyValuePair(key, MeveoValueExpressionWrapper.evaluateExpression(value, elContext, Object.class)));
			} catch (ELException e) {
				log.error("Failed to evaluate el for custom action", e);
			}
		});
		
		PrimeFaces.current().ajax().update("formId:buttons:executeDialog");
	}
	
	/**
	 * @return the {@link #overrideParams}
	 */
	public Set<KeyValuePair> getOverrideParams() {
		return overrideParams;
	}

	/**
	 * @param cft the scret cft
	 * @return whether the given secret cft should be displayed in clear on GUI
	 */
	public boolean isDisplayedInClear(CustomFieldTemplate cft) {
		return secretToDisplayInClear.getOrDefault(cft.getCode(), false);
	}
	
	/**
	 * @param cft the scret cft
	 * @param value whether the given secret cft should be displayed in clear on GUI
	 */
	public void setDisplayedInClear(CustomFieldTemplate cft, boolean value) {
		secretToDisplayInClear.put(cft.getCode(), value);
	}
	
	/**
	 * @return the initial hash of the CEI before it was modified
	 */
	public String getHash() {
		return hash;
	}

	@Override
	protected IPersistenceService<CustomEntityInstance> getPersistenceService() {
		return customEntityInstanceService;
	}

	@Override
	public CustomEntityInstance initEntity() {
		repository = repositoryService.findByCode(repositoryCode);

		customEntityTemplate = customEntityTemplateService.findByCode(customEntityTemplateCode, List.of("availableStorages"));
		customFieldTemplates = cacheContainerProvider.getCustomFieldTemplates(customEntityTemplate.getAppliesTo());

		entity = new CustomEntityInstance();
		entity.setCetCode(customEntityTemplateCode);
		entity.setCet(customEntityTemplate);

		if (!StringUtils.isBlank(uuid) && !uuid.equals("null")) {
			try {
				Map<String, Object> cfValues = crossStorageService.find(repository, customEntityTemplate, uuid, true);

				if (cfValues != null) {
					log.debug("Loading cfValues={}", cfValues);
					entity.setCode((String) cfValues.get("code"));
					entity.setCet(customEntityTemplate);
					entity.setDescription((String) cfValues.get("description"));
					entity.setUuid(uuid);

					customFieldInstanceService.setCfValues(entity, customEntityTemplateCode, cfValues);
					entity.setCfValuesOld((CustomFieldValues) SerializationUtils.clone(entity.getCfValues()));
				}

			} catch (Exception e) {
				log.error("Error during entity init", e);
			}
		}
		
		if(entity.getCfValuesAsValues() != null && customFieldTemplates != null) {
			hash = CEIUtils.getHash(entity, customFieldTemplates);
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
	
	public void executeWithParameters() {
		executeCustomAction(this.entity, action, null);
		this.action = null;
		overrideParams.clear();
	}
	
	/**
	 * Execute custom action on an entity
	 *
	 * @param entity            Entity to execute action on
	 * @param action            Action to execute
	 * @param encodedParameters Additional parameters encoded in URL like style
	 *                          param=value&amp;param=value
	 * @return A script execution result value from Script.RESULT_GUI_OUTCOME
	 *         variable
	 */
	public String executeCustomAction(ICustomFieldEntity entity, EntityCustomAction action, String encodedParameters) {

		try {

			action = entityActionScriptService.findByCode(action.getCode());

			Map<String, Object> context = CustomScriptService.parseParameters(encodedParameters);
			context.put(Script.CONTEXT_ACTION, action.getCode());
			
			if(overrideParams != null && !overrideParams.isEmpty()) {
				overrideParams.forEach(entry -> {
					context.put(entry.getKey(), entry.getValue());
				});
			} else {
				Map<Object, Object> elContext = new HashMap<>(context);
				elContext.put("entity", entity);
				
				action.getScriptParameters().forEach((key, value) -> {
					try {
						context.put(key, MeveoValueExpressionWrapper.evaluateExpression(value, elContext, Object.class));
					} catch (ELException e) {
						log.error("Failed to evaluate el for custom action", e);
					}
				});
			}
			
			Map<String, Object> result = scriptInstanceService.execute((IEntity) entity, repository, action.getScript().getCode(), context);

			// Display a message accordingly on what is set in result
			if (result.containsKey(Script.RESULT_GUI_MESSAGE_KEY)) {
				messages.info(new BundleKey("messages", (String) result.get(Script.RESULT_GUI_MESSAGE_KEY)));

			} else if (result.containsKey(Script.RESULT_GUI_MESSAGE)) {
				messages.info((String) result.get(Script.RESULT_GUI_MESSAGE));

			} else {
				messages.info(new BundleKey("messages", "scriptInstance.actionExecutionSuccessfull"), action.getLabel());
			}

			if (result.containsKey(Script.RESULT_GUI_OUTCOME)) {
				return (String) result.get(Script.RESULT_GUI_OUTCOME);
			}

		} catch (BusinessException e) {
			log.error("Failed to execute a script {} on entity {}", action.getCode(), entity, e);
			messages.error(new BundleKey("messages", "scriptInstance.actionExecutionFailed"), action.getLabel(), e.getMessage());
		}

		return null;
	}


	@Override
	@ActionMethod
	public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {

		if (StringUtils.isBlank(entity.getCetCode())) {
			messages.error(new BundleKey("messages", "customEntityInstance.noCetCodeSet"));
			return null;
		}

		String result = getListViewName();

		boolean isNew = StringUtils.isBlank(uuid) || "null".equals(uuid);

		try {

			customFieldDataEntryBean.saveCustomFieldsToEntity(entity, isNew);

			String message = entity.isTransient() ? "save.successful" : "update.successful";

			try {
				// Raise an error if the value already exist
				String entityId = crossStorageService.findEntityId(repository, entity);
				if (entityId == null || entityId == entity.getUuid()) {
					crossStorageService.createOrUpdate(repository, entity);
				} else {
					messages.error("Entity with same unique values already exsits with id " + entityId);
					return null;
				}
			} catch (IllegalTransitionException e) {
				messages.error(new BundleKey("messages", "customEntityInstance.update.illegalTransition"), e.getField(), e.getFrom(), e.getTo());
				return null;
			}

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
			log.error("Cannot create or update CEI", e);
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
		if (customEntityTemplateCode == null && getCustomEntityTemplate() != null) {
			customEntityTemplateCode = getCustomEntityTemplate().getCode();
		}

		return customEntityTemplateCode;
	}

	public Repository getRepository() {
		if (repository == null && repositoryCode != null) {
			repository = this.repositoryService.findByCode(repositoryCode);
			repositoryProvider.setRepository(repository);
		}

		return repository;
	}

	public String getCeiCode(CustomFieldTemplate cft, Map<String, Object> getMapValues) {
		if (getMapValues != null) {
			for (Map.Entry<String, Object> ceiMap : getMapValues.entrySet()) {
				if (ceiMap.getValue() instanceof EntityReferenceWrapper) {
					EntityReferenceWrapper wrapper = (EntityReferenceWrapper) ceiMap.getValue();
					return wrapper.getCode();
				}

				if(ceiMap.getValue() instanceof BusinessEntity) {
					BusinessEntity cei = (BusinessEntity) ceiMap.getValue();
					return cei.getCode();
				} else if(ceiMap.getValue() instanceof Map) {
					Map<String, Object> map = (Map<String, Object>) ceiMap.getValue();
					return (String) map.get("uuid");
				} else {
					return ceiMap.getValue().toString();
				}
			}
		}

		return null;
	}

	public String getCeiUuid(CustomFieldTemplate customFieldTemplate, Map<String, Object> getMapValues) {
		if (getMapValues != null) {
			for (Map.Entry<String, Object> ceiMap : getMapValues.entrySet()) {
				if (ceiMap.getValue() instanceof CustomEntityInstance) {
					CustomEntityInstance cei = (CustomEntityInstance) ceiMap.getValue();
					return cei.getUuid();

				} else if (ceiMap.getValue() instanceof BusinessEntity) {
					BusinessEntity be = (BusinessEntity) ceiMap.getValue();
					if (be.getCode() != null) {
						CustomEntityInstance cei = new CustomEntityInstance();
						cei.setCode(be.getCode());
						cei.setCetCode(customFieldTemplate.getEntityClazzCetCode());
						cei.setCet(customEntityTemplateService.findByCode(cei.getCetCode()));

						CustomFieldValues values = new CustomFieldValues();
						values.setValue("code", be.getCode());
						cei.setCfValues(values);

						return crossStorageService.findEntityId(repository, cei);
					}

				} else if (ceiMap.getValue() instanceof EntityReferenceWrapper) {
					EntityReferenceWrapper wrapper = (EntityReferenceWrapper) ceiMap.getValue();
					return wrapper.getUuid();
				} else if(ceiMap.getValue() instanceof Map) {
					Map<String, Object> map = (Map<String, Object>) ceiMap.getValue();
					return (String) map.get("uuid");
				}

			}
		}

		return null;
	}

	public String getCetCode(CustomFieldTemplate cft, Map<String, Object> getMapValues) {
		return cft.getEntityClazzCetCode();
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

	public Repository getDefaultRepository() {
		return repositoryService.findDefaultRepository();
	}

	public LazyDataModel<Map<String, Object>> getNativeDataModel() throws NamingException {
		return getNativeDataModel(filters);
	}

	public LazyDataModel<Map<String, Object>> getNativeDataModel(Map<String, Object> inputFilters) throws NamingException {

		if (nativeDataModel == null) {

			final Map<String, Object> filters = inputFilters;

			nativeDataModel = new CrossStorageDataModel() {

				private static final long serialVersionUID = 6682319740448829853L;

				@Override
				protected Map<String, Object> getSearchCriteria() {
					return filters;
				}

				@Override
				protected Repository getRepository() {
					return CustomEntityInstanceBean.this.getDefaultRepository();
				}

				@Override
				protected CustomEntityTemplate getCustomEntityTemplate() {
					CustomEntityTemplate cet = new CustomEntityTemplate();
					cet.getSqlStorageConfigurationNullSafe().setStoreAsTable(true);
					cet.setCode(CustomEntityTemplate.AUDIT_PREFIX + CustomEntityInstanceBean.this.getCustomEntityTemplateCode());
					cet.getAvailableStorages().add(DBStorageType.SQL);
					return cet;
				}

			};
		}

		log.debug("{}", nativeDataModel);

		return nativeDataModel;
	}

	@Override
	public boolean canUserUpdateEntity() {
		return currentUser.hasRole(customEntityTemplate.getModifyPermission());
	}
	
	
}
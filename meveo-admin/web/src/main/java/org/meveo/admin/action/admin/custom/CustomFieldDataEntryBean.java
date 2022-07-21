package org.meveo.admin.action.admin.custom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.ResourceBundle;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.CETUtils;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.elresolver.ELException;
import org.meveo.model.BusinessCFEntity;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.IEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.EntityReferenceWrapper;
import org.meveo.model.crm.custom.CustomFieldMapKeyEnum;
import org.meveo.model.crm.custom.CustomFieldMatrixColumn;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValue;
import org.meveo.model.crm.custom.CustomFieldValueHolder;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.CEIUtils;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.shared.DateUtils;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.base.MeveoValueExpressionWrapper;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomTableService;
import org.meveo.service.custom.EntityCustomActionService;
import org.meveo.service.script.CustomScriptService;
import org.meveo.service.script.Script;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.storage.BinaryStoragePathParam;
import org.meveo.service.storage.FileSystemService;
import org.meveo.service.storage.RepositoryService;
import org.meveo.util.EntityCustomizationUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support for custom field value data entry
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @author akadid abdelmounaim
 * @lastModifiedVersion 6.12
 */
@Named
@ViewScoped
public class CustomFieldDataEntryBean implements Serializable {

	private static final long serialVersionUID = 2587695185934268809L;

	/** Logger. */
	protected Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Custom field templates grouped into tabs and field groups
	 */
	Map<String, GroupedCustomField> groupedFieldTemplates = new HashMap<String, GroupedCustomField>();

	/**
	 * Custom actions applicable to the entity
	 */
	private Map<String, List<EntityCustomAction>> customActions = new HashMap<String, List<EntityCustomAction>>();

	/**
	 * Custom field values and new value GUI data entry values
	 */
	private Map<String, CustomFieldValueHolder> fieldsValues = new HashMap<String, CustomFieldValueHolder>();

	@Inject
	private transient CustomFieldInstanceService customFieldInstanceService;

	@Inject
	private transient CustomFieldTemplateService customFieldTemplateService;

	@Inject
	private ResourceBundle resourceMessages;

	@Inject
	private transient EntityCustomActionService entityActionScriptService;

	@Inject
	private transient ScriptInstanceService scriptInstanceService;

	@Inject
	private transient CustomEntityInstanceService customEntityInstanceService;

	@Inject
	private transient CustomEntityTemplateService customEntityTemplateService;

	@Inject
	private transient CustomTableService customTableService;

	@Inject
	private CustomFieldsCacheContainerProvider cache;

	@Inject
	@CurrentUser
	protected MeveoUser currentUser;

	/** paramBeanFactory */
	@Inject
	private ParamBeanFactory paramBeanFactory;

	@Inject
	protected Messages messages;

	@Inject
	private FileSystemService fileSystemService;

	@Inject
	private RepositoryService repositoryService;

	@Inject
	private transient CrossStorageService crossStorageService;

	private ICustomFieldEntity entity;

	private String customEntityTemplateCode;

	private String customEntityInstanceCode;

	private String customEntityInstanceUuid;

	private String fieldName;

	private List<CustomEntityInstance> entityInstances;

	private List<Map<String, Object>> entityInstancesForTable;

	private Map<String, Object> entityInstanceTable;

	private CustomEntityInstance entityInstance;

	private CustomEntityTemplate customEntityTemplate;

	private List<BusinessEntity> availableEntities = new ArrayList<>();

	private transient Map<String, UploadedFile> binaries = new HashMap<>();
	
	private transient Repository repository;
	private Map<String, Object> tempValues = new HashMap<>();

	private List<String> filesToDeleteOnExit = new ArrayList<>();

	private List<Object> listValues;

	public List<Object> getListValues() {
		return listValues;
	}

	public void setListValues(List<Object> listValues) {
		this.listValues = listValues;
	}

	/**
	 * Explicitly refresh fields and action definitions. Should be used on some
	 * field value change event when that field is used to determine what fields and
	 * actions apply. E.g. Job template.
	 *
	 * @param entity Entity to [re]load definitions and field values for
	 */
	public void refreshFieldsAndActions(ICustomFieldEntity entity) {

		this.entity = entity;
		initFields(entity);
		initCustomActions(entity);
	}

	public Map<String, Object> getTempValues() {
		return tempValues;
	}

	public void setTempValues(Map<String, Object> tempValue) {
		this.tempValues = tempValue;
	}

	/**
	 * Explicitly refresh fields and action definitions while preserving field
	 * values. Should be used when entity customization is managed as part of some
	 * page that contains CF data entry and CF fields should be refreshed when
	 * entity customization is finished. Job template.
	 *
	 * @param entity Entity to [re]load definitions and field values for
	 */
	public void refreshFieldsAndActionsWhilePreserveValues(ICustomFieldEntity entity) {

		this.entity = entity;
		refreshFieldsWhilePreservingValues(entity);
		initCustomActions(entity);
	}

	/**
	 * Get a grouped list of custom field definitions. If needed, load applicable
	 * custom fields (templates) and their values for a given entity
	 *
	 * @param entity Entity to load definitions and field values for
	 * @return Custom field information
	 */
	public GroupedCustomField getGroupedFieldTemplates(ICustomFieldEntity entity) {
		if (entity == null) {
			return null;
		}
		
		if (!groupedFieldTemplates.containsKey(entity.getUuid())) {
			initFields(entity);
		}

		return groupedFieldTemplates.get(entity.getUuid());
	}

	public List<EntityCustomAction> getCustomActionsInDetail(IEntity entity) {

		List<EntityCustomAction> customActions = getCustomActions(entity);
		return customActions.stream().filter(e -> e.getApplicableToEntityInstance()).collect(Collectors.toList());
	}
	
	public List<EntityCustomAction> getCustomActionsInList(IEntity entity) {
		List<EntityCustomAction> customActions = getCustomActions(entity);
		List<EntityCustomAction> results = customActions.stream().filter(e -> e.getApplicableToEntityList()).collect(Collectors.toList());
		return results;
	}
	
	/**
	 * Get a list of actions applicable for an entity. If needed, load them.
	 *
	 * @param entity Entity to load action definitions
	 * @return A list of actions
	 */
	public List<EntityCustomAction> getCustomActions(IEntity entity) {

		if (!(entity instanceof ICustomFieldEntity)) {
			return null;
		}

		if (!customActions.containsKey(((ICustomFieldEntity) entity).getUuid())) {
			initCustomActions((ICustomFieldEntity) entity);
		}
		return customActions.get(((ICustomFieldEntity) entity).getUuid());
	}

	/**
	 * Get a custom field value holder for a given entity
	 *
	 * @param entityUuid Entity uuid identifier
	 * @return Custom field value holder
	 */
	public CustomFieldValueHolder getFieldValueHolderByUUID(String entityUuid) {
		return fieldsValues.get(entityUuid);
	}

	/**
	 * Load applicable custom actions for a given entity
	 *
	 * @param entity Entity to load action definitions
	 */
	private void initCustomActions(ICustomFieldEntity entity) {

		Map<String, EntityCustomAction> actions = entityActionScriptService.findByAppliesTo(entity);

		List<EntityCustomAction> actionList = new ArrayList<EntityCustomAction>(actions.values());
		customActions.put(entity.getUuid(), actionList);
	}

	private static Map<String, CustomFieldTemplate> sortByValue(Map<String, CustomFieldTemplate> map) {
		List<Entry<String, CustomFieldTemplate>> list = new LinkedList<>(map.entrySet());
		list.sort(Comparator.comparing(entry -> entry.getValue().getCode()));

		Map<String, CustomFieldTemplate> result = new LinkedHashMap<>();
		for (Entry<String, CustomFieldTemplate> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	/**
	 * Load available custom fields (templates) and their values for a given entity
	 *
	 * @param entity Entity to load definitions and field values for
	 */
	private void initFields(ICustomFieldEntity entity) {
		this.entity = entity;

		Map<String, CustomFieldTemplate> customFieldTemplates = customFieldTemplateService.findByAppliesTo(entity);
		
		// Load inherited fields as well
		if (entity instanceof CustomEntityInstance) {
			String cetCode = ((CustomEntityInstance) entity).getCetCode();
			CustomEntityTemplate cet = customEntityTemplateService.findByCode(cetCode);
			for (CustomEntityTemplate e = cet.getSuperTemplate(); e != null; e = e.getSuperTemplate()) {
				customFieldTemplates.putAll(customFieldTemplateService.findByAppliesTo(e.getAppliesTo()));
			}
		}
		
		log.trace("Found {} custom field templates for entity {}", customFieldTemplates.size(), entity.getClass());

		customFieldTemplates = sortByValue(customFieldTemplates);

		GroupedCustomField groupedCustomField = new GroupedCustomField(customFieldTemplates.values(), "Custom fields", false);
		groupedFieldTemplates.put(entity.getUuid(), groupedCustomField);

		Map<String, List<CustomFieldValue>> cfValuesByCode = null;
		// Get custom field instances mapped by a CFT code if entity has any field
		// defined
		// if (!((IEntity) entity).isTransient() && customFieldTemplates != null &&
		// customFieldTemplates.size() > 0) {
		// No longer checking for isTransient as for offer new version creation, CFs are
		// duplicated, but entity is not persisted, offering to review it in GUI before
		// saving it.
		if (customFieldTemplates != null && customFieldTemplates.size() > 0 && ((ICustomFieldEntity) entity).getCfValues() != null) {
			cfValuesByCode = ((ICustomFieldEntity) entity).getCfValues().getValuesByCode();
		}
		cfValuesByCode = prepareCFIForGUI(customFieldTemplates, cfValuesByCode, entity);
		CustomFieldValueHolder entityFieldsValues = new CustomFieldValueHolder(customFieldTemplates, cfValuesByCode, entity);
		fieldsValues.put(entity.getUuid(), entityFieldsValues);
	}

	/**
	 * Load available custom fields (templates) while preserving their values for a
	 * given entity
	 *
	 * @param entity Entity to load definitions and field values for
	 */
	private void refreshFieldsWhilePreservingValues(ICustomFieldEntity entity) {

		Map<String, CustomFieldTemplate> customFieldTemplates = customFieldTemplateService.findByAppliesTo(entity);
		log.trace("Refreshing CFTS while preserving values. Found {} custom field templates for entity {}", customFieldTemplates.size(), entity.getClass());

		GroupedCustomField groupedCustomField = new GroupedCustomField(customFieldTemplates.values(), "Custom fields", false);
		groupedFieldTemplates.put(entity.getUuid(), groupedCustomField);

		CustomFieldValueHolder entityFieldsValues = fieldsValues.get(entity.getUuid());

		// Populate new value defaults formap, list and matrix fields
		entityFieldsValues.populateNewValueDefaults(customFieldTemplates.values(), null);

		// Populate new value defaults for simple fields
		for (CustomFieldTemplate cft : customFieldTemplates.values()) {
			if (entityFieldsValues.getValues(cft) == null && !cft.isVersionable()) {
				entityFieldsValues.getValuesByCode().put(cft.getCode(), Arrays.asList(cft.toDefaultCFValue()));
			}
		}
	}

	/**
	 * Prepare custom field values for GUI - instantiate fields with default values,
	 * deserialize values for GUI
	 *
	 * @param customFieldTemplates Custom field templates applicable for the entity,
	 *                             mapped by a custom CFT code
	 * @param cfValuesByCode       Custom field values mapped by a CFT code
	 * @param entity               Entity containing custom field values
	 *
	 * @return Prepared for GUI custom fields instances
	 */
	private Map<String, List<CustomFieldValue>> prepareCFIForGUI(Map<String, CustomFieldTemplate> customFieldTemplates, Map<String, List<CustomFieldValue>> cfValuesByCode,
			ICustomFieldEntity entity) {

		Map<String, List<CustomFieldValue>> cfisPrepared = new HashMap<>();

		// For each template, check if custom field value exists, and instantiate one if
		// needed with a default value
		for (CustomFieldTemplate cft : customFieldTemplates.values()) {

			List<CustomFieldValue> cfValuesByTemplate = null;
			if (cfValuesByCode != null) {
				cfValuesByTemplate = cfValuesByCode.get(cft.getCode());
			}
			if (cfValuesByTemplate == null) {
				cfValuesByTemplate = new ArrayList<>();
			}

			// Instantiate with a default value if no value found
			if (cfValuesByTemplate.isEmpty() && !cft.isVersionable()) {
				CustomFieldValue cfValue = cft.toDefaultCFValue();

				// Overwrite with inherited value if needed
				if (cft.isUseInheritedAsDefaultValue()) {
					Object inheritedValue = customFieldInstanceService.getInheritedOnlyCFValue(entity, cft.getCode());
					if (inheritedValue != null) {
						cfValue.setValue(inheritedValue);
					}
				}

				cfValuesByTemplate.add(cfValue);
			}

			// Deserialize values if applicable
			for (CustomFieldValue cfValue : cfValuesByTemplate) {
				deserializeForGUI(cfValue, cft);
			}

			// Make sure that only one value is retrieved
			if (!cft.isVersionable()) {
				cfValuesByTemplate = new ArrayList<CustomFieldValue>(cfValuesByTemplate.subList(0, 1));
			}
			cfisPrepared.put(cft.getCode(), cfValuesByTemplate);
		}

		return cfisPrepared;
	}

	/**
	 * Increase priority of a custom field value period
	 *
	 * @param entityValueHolder   custom field value holder
	 * @param cft                 Custom field definition
	 * @param valuePeriodToChange Custom field value period to change
	 */
	public void increasePriority(CustomFieldValueHolder entityValueHolder, CustomFieldTemplate cft, CustomFieldValue valuePeriodToChange) {

		boolean changed = entityValueHolder.changePriority(cft, valuePeriodToChange, true);

		if (changed) {
			messages.info(new BundleKey("messages", "customFieldTemplate.periodValue.priorityIncreased"));
		}
	}

	/**
	 * Decrease priority of a custom field value period
	 *
	 * @param entityValueHolder   custom field value holder
	 * @param cft                 Custom field definition
	 * @param valuePeriodToChange Custom field value period to change
	 */
	public void decreasePriority(CustomFieldValueHolder entityValueHolder, CustomFieldTemplate cft, CustomFieldValue valuePeriodToChange) {

		boolean changed = entityValueHolder.changePriority(cft, valuePeriodToChange, false);

		if (changed) {
			messages.info(new BundleKey("messages", "customFieldTemplate.periodValue.priorityDecreased"));
		}
	}

	/**
	 * Remove a customField period
	 *
	 * @param entityValueHolder   Entity custom field value holder
	 * @param cft                 Custom field definition
	 * @param valuePeriodToRemove Custom field value period to remove
	 */
	public void removePeriod(CustomFieldValueHolder entityValueHolder, CustomFieldTemplate cft, CustomFieldValue valuePeriodToRemove) {

		boolean removed = entityValueHolder.removeValuePeriod(cft, valuePeriodToRemove);

		if (removed) {
			messages.info(new BundleKey("messages", "customFieldTemplate.periodValue.removedPeriod"));
		}
	}

	/**
	 * Add a new customField period with a previous validation that matching period
	 * does not exists
	 *
	 * @param entityValueHolder Entity custom field value holder
	 * @param cft               Custom field definition
	 */
	public void addNewValuePeriod(CustomFieldValueHolder entityValueHolder, CustomFieldTemplate cft) {

		Date periodStartDate = (Date) entityValueHolder.getNewValue(cft.getCode() + "_periodStartDate");
		Date periodEndDate = (Date) entityValueHolder.getNewValue(cft.getCode() + "_periodEndDate");
		Object value = entityValueHolder.getNewValue(cft.getCode() + "_value");

		// Check that two dates are one after another
		if (periodStartDate != null && periodEndDate != null && periodStartDate.compareTo(periodEndDate) >= 0) {
			messages.error(new BundleKey("messages", "customFieldTemplate.periodIntervalIncorrect"));
			FacesContext.getCurrentInstance().validationFailed();
			return;
		}

		// Validate that value is set
		if (cft.getStorageType() == CustomFieldStorageTypeEnum.SINGLE && value == null) {
			messages.error(new BundleKey("messages", "customFieldTemplate.valueNotSpecified"));
			FacesContext.getCurrentInstance().validationFailed();
			return;
		}

		CustomFieldValue cfValue = null;
		// First check if any period matches the dates
		if (entityValueHolder.getValuePeriodMatched() == null || !entityValueHolder.getValuePeriodMatched()) {
			if (periodStartDate == null && periodEndDate == null) {
				messages.error(new BundleKey("messages", "customFieldTemplate.periodDatesBothNull"));
				entityValueHolder.setValuePeriodMatched(true);
				FacesContext.getCurrentInstance().validationFailed();
				return;
			}

			boolean strictMatch = false;
			if (cft.getCalendar() != null) {
				cfValue = entityValueHolder.getValuePeriod(cft, periodStartDate, false);
				strictMatch = true;
			} else {
				cfValue = entityValueHolder.getValuePeriod(cft, periodStartDate, periodEndDate, false, false);
				if (cfValue != null && cfValue.getPeriod() != null) {
					strictMatch = cfValue.getPeriod().isCorrespondsToPeriod(
							periodStartDate.toInstant(), 
							periodEndDate.toInstant(), 
							true
						);
				}
			}

			if (cfValue != null) {
				entityValueHolder.setValuePeriodMatched(true);
				ParamBean paramBean = paramBeanFactory.getInstance();
				String datePattern = paramBean.getDateFormat();

				// For a strict match need to edit an existing period
				if (strictMatch) {
					messages.error(new BundleKey("messages", "customFieldTemplate.matchingPeriodFound.noNew"),
							cfValue.getPeriod() == null ? "" : DateUtils.formatDateWithPattern(cfValue.getPeriod().getFrom(), datePattern),
							cfValue.getPeriod() == null ? "" : DateUtils.formatDateWithPattern(cfValue.getPeriod().getTo(), datePattern));
					entityValueHolder.setValuePeriodMatched(false);

					// For a non-strict match user has an option to create a period with a higher
					// priority
				} else {
					messages.warn(new BundleKey("messages", "customFieldTemplate.matchingPeriodFound"),
							cfValue.getPeriod() == null ? "" : DateUtils.formatDateWithPattern(cfValue.getPeriod().getFrom(), datePattern),
							cfValue.getPeriod() == null ? "" : DateUtils.formatDateWithPattern(cfValue.getPeriod().getTo(), datePattern));
					entityValueHolder.setValuePeriodMatched(true);
				}
				FacesContext.getCurrentInstance().validationFailed();
				return;
			}
		}

		// Create period if passed a period check or if user decided to create it anyway
		if (cft.getCalendar() != null) {
			cfValue = entityValueHolder.addValuePeriod(cft, periodStartDate);

		} else {
			cfValue = entityValueHolder.addValuePeriod(cft, periodStartDate, periodEndDate);
		}

		// Set value
		if (cft.getStorageType() == CustomFieldStorageTypeEnum.SINGLE) {
			cfValue.setSingleValue(value, cft.getFieldType());
			if (cft.getFieldType() == CustomFieldTypeEnum.ENTITY) {
				cfValue.setEntityReferenceValueForGUI((BusinessEntity) value);
			}
		}

		// } else {
		// Map<String, Object> newValue = new HashMap<String, Object>();
		// if (cft.getStorageType() == CustomFieldStorageTypeEnum.MAP) {
		// newValue.put("key", key);
		// }
		// newValue.put("value", value);
		// period.getCfValue().getMapValuesForGUI().add(newValue);
		// }

		entityValueHolder.populateNewValueDefaults(null, cft);
		entityValueHolder.setValuePeriodMatched(false);
		entityValueHolder.setSelectedFieldTemplate(cft);
		entityValueHolder.setSelectedValuePeriod(cfValue);
	}

	/**
	 * Add value to a map of values, setting a default value if applicable
	 *
	 * @param entityValueHolder Entity custom field value holder
	 * @param cfv               Map value holder
	 * @param cft               Custom field definition
	 */
	public void addValueToMap(CustomFieldValueHolder entityValueHolder, CustomFieldValue cfv, CustomFieldTemplate cft) {

		String newKey = null;
		if (cft.getStorageType() == CustomFieldStorageTypeEnum.MAP) {
			if (cft.getMapKeyType() == CustomFieldMapKeyEnum.STRING) {
				newKey = (String) entityValueHolder.getNewValue(cft.getCode() + "_key");

			} else if (cft.getMapKeyType() == CustomFieldMapKeyEnum.RON) {
				// Validate that at least one value is provided and in correct order
				Double from = (Double) entityValueHolder.getNewValue(cft.getCode() + "_key_one_from");
				Double to = (Double) entityValueHolder.getNewValue(cft.getCode() + "_key_one_to");

				if (from == null && to == null) {
					messages.error(new BundleKey("messages", "customFieldTemplate.eitherFromOrToRequired"));
					FacesContext.getCurrentInstance().validationFailed();
					return;

				} else if (from != null && to != null && from.compareTo(to) >= 0) {
					messages.error(new BundleKey("messages", "customFieldTemplate.fromOrToOrder"));
					FacesContext.getCurrentInstance().validationFailed();
					return;
				}
				newKey = (from == null ? "" : from) + CustomFieldValue.RON_VALUE_SEPARATOR + (to == null ? "" : to);
			}

			if (newKey == null) {
				messages.error(new BundleKey("messages", "customFieldTemplate.mapKeyNotSpecified"));
				FacesContext.getCurrentInstance().validationFailed();
				return;
			}
		}

		Object newValue = entityValueHolder.getNewValue(cft.getCode() + "_value");
		if (newValue == null) {
			messages.error(new BundleKey("messages", "customFieldTemplate.valueNotSpecified"));
			FacesContext.getCurrentInstance().validationFailed();
			return;
		}

		Map<String, Object> value = new HashMap<String, Object>();
		if (cft.getStorageType() == CustomFieldStorageTypeEnum.MAP) {
			value.put(CustomFieldValue.MAP_KEY, newKey);
		}
		value.put(CustomFieldValue.MAP_VALUE, newValue);

		// Validate that key or value is not duplicate
		for (Map<String, Object> mapItem : cfv.getMapValuesForGUI()) {
			if (cft.getStorageType() == CustomFieldStorageTypeEnum.MAP && mapItem.get(CustomFieldValue.MAP_KEY).equals(newKey)) {
				messages.error(new BundleKey("messages", "customFieldTemplate.mapKeyExists"));
				FacesContext.getCurrentInstance().validationFailed();
				return;
			} else if (cft.getStorageType() == CustomFieldStorageTypeEnum.LIST && mapItem.get(CustomFieldValue.MAP_VALUE).equals(newValue)) {
				messages.error(new BundleKey("messages", "customFieldTemplate.listValueExists"));
				FacesContext.getCurrentInstance().validationFailed();
				return;
			}
		}

		cfv.getMapValuesForGUI().add(value);

		entityValueHolder.clearNewValues();
	}

	/**
	 * Autocomplete method for listing entities for "Reference to entity" type
	 * custom field values
	 *
	 * @param wildcode A partial entity code match
	 * @return A list of entities [partially] matching code
	 */
	public List<BusinessEntity> autocompleteEntityForCFV(String wildcode) {
		String classname = (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get("classname");
		return customFieldInstanceService.findBusinessEntityForCFVByCode(classname, wildcode);
	}

	public List<BusinessEntity> allEntityForCFV() {
		String classname = (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get("classname");
		return customFieldInstanceService.findBusinessEntityForCFVByCode(classname, "");
	}

	public List<BusinessEntity> getAvailableEntities() {
		if (CollectionUtils.isNotEmpty(availableEntities)) {
			return availableEntities;
		}
		return new ArrayList<>();
	}

	public void setAvailableEntities(List<BusinessEntity> availableEntities) {
		this.availableEntities = availableEntities;
	}

	/**
	 * Validate complex custom fields
	 *
	 * @param entity Entity, to which custom fields are related to
	 */
	public boolean validateCustomFields(ICustomFieldEntity entity) {
		boolean valid = true;
		boolean isNewEntity = ((IEntity) entity).isTransient();

		FacesContext fc = FacesContext.getCurrentInstance();
		for (CustomFieldTemplate cft : groupedFieldTemplates.get(entity.getUuid()).getFields()) {

			// Ignore the validation on a field when creating entity and CFT.hideOnNew=true
			// or editing entity and CFT.allowEdit=false or when CFT.applicableOnEL
			// expression
			// evaluates to false
			if (cft.isDisabled() || !cft.isValueRequired() || (isNewEntity && cft.isHideOnNew()) || (!isNewEntity && !cft.isAllowEdit())
					|| !MeveoValueExpressionWrapper.evaluateToBooleanIgnoreErrors(cft.getApplicableOnEl(), "entity", entity)) {
				continue;

				// Single field's mandatory requirement are taken care in GUI level, new values
				// are not available yet here at validation stage
			} else if ((cft.getStorageType() != CustomFieldStorageTypeEnum.SINGLE || cft.isVersionable())) {

				List<CustomFieldValue> cfValues = getFieldValueHolderByUUID(entity.getUuid()).getValues(cft);

				// Fail validation on non empty values only if it does not have inherited value
				if (cfValues == null || cfValues.isEmpty()) {
					if (!customFieldInstanceService.hasInheritedOnlyCFValue(entity, cft.getCode())) {
						FacesMessage msg = new FacesMessage(resourceMessages.getString("javax.faces.component.UIInput.REQUIRED", cft.getDescription()));
						msg.setSeverity(FacesMessage.SEVERITY_ERROR);
						fc.addMessage(null, msg);
						valid = false;
					}
				} else {
					for (CustomFieldValue cfValue : cfValues) {
						if (cfValue.isValueEmptyForGui()) {
							if (customFieldInstanceService.hasInheritedOnlyCFValue(entity, cft.getCode())) {
								break;
							}
							FacesMessage msg = new FacesMessage(resourceMessages.getString("javax.faces.component.UIInput.REQUIRED", cft.getDescription()));
							msg.setSeverity(FacesMessage.SEVERITY_ERROR);
							fc.addMessage(null, msg);
							valid = false;
						}
					}
				}
			}
		}

		if (!valid) {

			fc.validationFailed();
			fc.renderResponse();
		}
		return valid;
	}

	/**
	 * Get inherited custom field value for a given entity
	 *
	 * @param entity to get the inherited value for
	 * @param cfCode Custom field code
	 * @return Custom field value
	 */
	public Object getInheritedCFValue(ICustomFieldEntity entity, String cfCode) {
		return customFieldInstanceService.getInheritedOnlyCFValue(entity, cfCode);
	}

	/**
	 * Get a a list of custom field CFvalues for a given entity's parent's hierarchy
	 * up. (DOES NOT include a given entity)
	 *
	 * @param entity Entity
	 * @param cft    Custom field definition
	 * @return A list of Custom field CFvalues. From all the entities CF entity
	 *         hierarchy up.
	 */
	public List<CustomFieldValue> getInheritedVersionableCFValue(ICustomFieldEntity entity, CustomFieldTemplate cft) {
		List<CustomFieldValue> values = new ArrayList<>();
		if (cft != null) {
			values.addAll(customFieldInstanceService.getInheritedOnlyAllCFValues(entity, cft.getCode()));

			for (CustomFieldValue cfv : values) {
				deserializeForGUI(cfv, cft);
			}
		}

		return values;
	}

	/**
	 * Get inherited custom field value for a given entity. A cumulative custom
	 * field value is calculated for Map(Matrix) type fields
	 *
	 * @param entity to get the inherited value for
	 * @param cft    Custom field definition
	 * @return Custom field value
	 */
	public CustomFieldValue getInheritedCumulativeCFValue(ICustomFieldEntity entity, CustomFieldTemplate cft) {
		if (cft == null) {
			return null;
		}

		Object inheritedValue = customFieldInstanceService.getInheritedOnlyCFValueCumulative(entity, cft.getCode());
		if (inheritedValue == null) {
			return null;
		}

		CustomFieldValue cfv = new CustomFieldValue();
		cfv.setValue(inheritedValue);
		deserializeForGUI(cfv, cft);

		return cfv;
	}

	/**
	 * Add row to a matrix. v5.0: Fix for save values on a multi values CF type
	 * problem
	 *
	 * @param entityValueHolder Entity custom field value holder
	 * @param cfValue           Map value holder
	 * @param cft               Custom field definition
	 *
	 * @author akadid abdelmounaim
	 * @lastModifiedVersion 5.0
	 */
	public void addMatrixRow(CustomFieldValueHolder entityValueHolder, CustomFieldValue cfValue, CustomFieldTemplate cft) {

		Map<String, Object> rowKeysAndValues = new HashMap<String, Object>();

		// Process keys
		for (CustomFieldMatrixColumn column : cft.getMatrixKeyColumns()) {

			Object newKey = null;

			if (column.getKeyType() == CustomFieldMapKeyEnum.STRING || column.getKeyType() == CustomFieldMapKeyEnum.TEXT_AREA) {
				newKey = (String) entityValueHolder.getNewValue(cft.getCode() + "_" + column.getCode());

				// No reason to support Long and Double as key values as it us covered by a
				// range, but - why not??
			} else if (column.getKeyType() == CustomFieldMapKeyEnum.LONG) {
				newKey = (Long) entityValueHolder.getNewValue(cft.getCode() + "_" + column.getCode());

			} else if (column.getKeyType() == CustomFieldMapKeyEnum.DOUBLE) {
				newKey = (Double) entityValueHolder.getNewValue(cft.getCode() + "_" + column.getCode());

			} else if (column.getKeyType() == CustomFieldMapKeyEnum.RON) {
				// Validate that at least one value is provided and in correct order
				Double from = (Double) entityValueHolder.getNewValue(cft.getCode() + "_" + column.getCode() + "_from");
				Double to = (Double) entityValueHolder.getNewValue(cft.getCode() + "_" + column.getCode() + "_to");

				if (from == null && to == null) {
					messages.error(new BundleKey("messages", "customFieldTemplate.eitherFromOrToRequired"));
					FacesContext.getCurrentInstance().validationFailed();
					return;

				} else if (from != null && to != null && from.compareTo(to) >= 0) {
					messages.error(new BundleKey("messages", "customFieldTemplate.fromOrToOrder"));
					FacesContext.getCurrentInstance().validationFailed();
					return;
				}
				newKey = (from == null ? "" : from) + CustomFieldValue.RON_VALUE_SEPARATOR + (to == null ? "" : to);
			}

			if (newKey != null) {
				rowKeysAndValues.put(column.getCode(), newKey);
			}
		}

		if (rowKeysAndValues.isEmpty()) {
			messages.error(new BundleKey("messages", "customFieldTemplate.matrixKeyNotSpecified"));
			FacesContext.getCurrentInstance().validationFailed();
			return;
		}

		// Process values

		// Multiple value columns
		if (cft.getFieldType() == CustomFieldTypeEnum.MULTI_VALUE) {

			Map<String, Object> rowValues = new HashMap<String, Object>();

			for (CustomFieldMatrixColumn column : cft.getMatrixValueColumns()) {

				Object newValue = null;

				if (column.getKeyType() == CustomFieldMapKeyEnum.STRING || column.getKeyType() == CustomFieldMapKeyEnum.TEXT_AREA) {
					newValue = (String) entityValueHolder.getNewValue(cft.getCode() + "_" + column.getCode());

				} else if (column.getKeyType() == CustomFieldMapKeyEnum.LONG) {
					newValue = (Long) entityValueHolder.getNewValue(cft.getCode() + "_" + column.getCode());

				} else if (column.getKeyType() == CustomFieldMapKeyEnum.DOUBLE) {
					newValue = (Double) entityValueHolder.getNewValue(cft.getCode() + "_" + column.getCode());

					// No reason to support RON as value data type - but code is copied - so why not
				} else if (column.getKeyType() == CustomFieldMapKeyEnum.RON) {
					// Validate that at least one value is provided and in correct order
					Double from = (Double) entityValueHolder.getNewValue(cft.getCode() + "_" + column.getCode() + "_from");
					Double to = (Double) entityValueHolder.getNewValue(cft.getCode() + "_" + column.getCode() + "_to");

					if (from == null && to == null) {
						messages.error(new BundleKey("messages", "customFieldTemplate.eitherFromOrToRequired"));
						FacesContext.getCurrentInstance().validationFailed();
						return;

					} else if (from != null && to != null && from.compareTo(to) >= 0) {
						messages.error(new BundleKey("messages", "customFieldTemplate.fromOrToOrder"));
						FacesContext.getCurrentInstance().validationFailed();
						return;
					}
					newValue = (from == null ? "" : from) + CustomFieldValue.RON_VALUE_SEPARATOR + (to == null ? "" : to);
				}

				if (newValue != null) {
					rowValues.put(column.getCode(), newValue);
				}
			}

			if (rowValues.isEmpty()) {
				messages.error(new BundleKey("messages", "customFieldTemplate.valuesNotSpecified"));
				FacesContext.getCurrentInstance().validationFailed();
				return;
			}

			rowKeysAndValues.putAll(rowValues);

			// Single value column
		} else {
			Object newValue = entityValueHolder.getNewValue(cft.getCode() + "_value");
			if (newValue == null) {
				messages.error(new BundleKey("messages", "customFieldTemplate.valueNotSpecified"));
				FacesContext.getCurrentInstance().validationFailed();
				return;
			}

			rowKeysAndValues.put(CustomFieldValue.MAP_VALUE, newValue);
		}

		// Validate that key or value is not duplicate
		for (Map<String, Object> mapItem : cfValue.getMatrixValuesForGUI()) {
			boolean allMatch = true;
			for (CustomFieldMatrixColumn column : cft.getMatrixColumns()) {
				if (mapItem.get(column.getCode()) == null && rowKeysAndValues.get(column.getCode()) == null) {

				} else if (mapItem.get(column.getCode()) != null && !mapItem.get(column.getCode()).equals(rowKeysAndValues.get(column.getCode()))) {
					allMatch = false;
					break;
				} else if (rowKeysAndValues.get(column.getCode()) != null && !rowKeysAndValues.get(column.getCode()).equals(mapItem.get(column.getCode()))) {
					allMatch = false;
					break;
				}
			}

			if (allMatch) {
				messages.error(new BundleKey("messages", "customFieldTemplate.matrixKeyExists"));
				FacesContext.getCurrentInstance().validationFailed();
				return;
			}
		}

		cfValue.getMatrixValuesForGUI().add(rowKeysAndValues);

		entityValueHolder.clearNewValues();
	}

	/**
	 * Execute custom action on a child entity
	 *
	 * @param parentEntity      Parent entity, entity is related to
	 * @param childEntity       Entity to execute action on
	 * @param action            Action to execute
	 * @param encodedParameters Additional parameters encoded in URL like style
	 *                          param=value&amp;param=value
	 * @return A script execution result value from Script.RESULT_GUI_OUTCOME
	 *         variable
	 */
	public String executeCustomActionOnChildEntity(ICustomFieldEntity parentEntity, ICustomFieldEntity childEntity, EntityCustomAction action, String encodedParameters) {

		try {

			Map<String, Object> context = CustomScriptService.parseParameters(encodedParameters);
			context.put(Script.CONTEXT_PARENT_ENTITY, parentEntity);
			context.put(Script.CONTEXT_ACTION, action.getCode());
			
			Map<Object, Object> elContext = new HashMap<>(context);
			elContext.put("entity", entity);
			
			action.getScriptParameters().forEach((key, value) -> {
				try {
					context.put(key, MeveoValueExpressionWrapper.evaluateExpression(value, elContext, Object.class));
				} catch (ELException e) {
					log.error("Failed to evaluate el for custom action", e);
				}
			});

			Map<String, Object> result = scriptInstanceService.execute((IEntity) childEntity, repository, action.getScript().getCode(), context);

			// Display a message accordingly on what is set in result
			if (result.containsKey(Script.RESULT_GUI_MESSAGE_KEY)) {
				messages.info(new BundleKey("messages", (String) result.get(Script.RESULT_GUI_MESSAGE_KEY)));

			} else if (result.containsKey(Script.RESULT_GUI_MESSAGE_KEY)) {
				messages.info((String) result.get(Script.RESULT_GUI_MESSAGE));

			} else {
				messages.info(new BundleKey("messages", "scriptInstance.actionExecutionSuccessfull"), action.getLabel());
			}

			if (result.containsKey(Script.RESULT_GUI_OUTCOME)) {
				return (String) result.get(Script.RESULT_GUI_OUTCOME);
			}

		} catch (BusinessException e) {
			log.error("Failed to execute a script {} on entity {}", action.getCode(), childEntity, e);
			messages.error(new BundleKey("messages", "scriptInstance.actionExecutionFailed"), action.getLabel(), e.getMessage());
		}

		return null;
	}

	public Map<String, List<CustomFieldValue>> saveCustomFieldsToEntity(ICustomFieldEntity entity, boolean isNewEntity) throws BusinessException, ELException {
		return saveCustomFieldsToEntity(entity, isNewEntity, true);
	}

	/**
	 * Save custom fields for a given entity
	 *
	 * @param entity      Entity, the fields relate to
	 * @param isNewEntity Is it a new entity
	 * @return CustomFieldValue Map
	 * @throws BusinessException
	 */
	public Map<String, List<CustomFieldValue>> saveCustomFieldsToEntity(ICustomFieldEntity entity, boolean isNewEntity, boolean isSaveEntity)
			throws BusinessException, ELException {
		String uuid = entity.getUuid();
		return saveCustomFieldsToEntity(entity, uuid, false, isNewEntity, isSaveEntity);
	}

	public Map<String, List<CustomFieldValue>> saveCustomFieldsToEntity(ICustomFieldEntity entity, String uuid, boolean duplicateCFI, boolean isNewEntity)
			throws BusinessException, ELException {
		return saveCustomFieldsToEntity(entity, uuid, duplicateCFI, isNewEntity, true);
	}

	public Map<String, List<CustomFieldValue>> saveCustomFieldsToEntity(ICustomFieldEntity entity, String uuid, boolean duplicateCFI, boolean isNewEntity, boolean isSaveEntity)
			throws BusinessException, ELException {
		return saveCustomFieldsToEntity(entity, uuid, duplicateCFI, isNewEntity, false, isSaveEntity);
	}

	/**
	 * Save custom fields for a given entity
	 *
	 * @param entity             Entity, the fields relate to
	 * @param isNewEntity        Is it a new entity
	 * @param removedOriginalCFI - When duplicating a CFI, this boolean is true when
	 *                           we want to remove the original CFI. Use specially
	 *                           in offer instantiation where we assigned CFT values
	 *                           on entity a but then save it on entity b. Entity a
	 *                           is then reverted. This flag is needed because on
	 *                           some part CFI is duplicated first, but is not
	 *                           updated, instead we duplicate again.
	 * @return CustomFieldValue Map
	 * @throws BusinessException
	 */
	public Map<String, List<CustomFieldValue>> saveCustomFieldsToEntity(ICustomFieldEntity entity, String uuid, boolean duplicateCFI, boolean isNewEntity,
			boolean removedOriginalCFI, boolean isSaveEntity) throws BusinessException, ELException {
		
		Map<String, List<CustomFieldValue>> newValuesByCode = new HashMap<>();

		CustomFieldValueHolder entityFieldsValues = getFieldValueHolderByUUID(uuid);
		GroupedCustomField groupedCustomFields = groupedFieldTemplates.get(uuid);
		if (groupedCustomFields != null) {
			for (CustomFieldTemplate cft : groupedCustomFields.getFields()) {

				// Do not update existing CF value if it is not updatable
				if (!isNewEntity && !cft.isAllowEdit()) {

					if (entity != null && entity.getCfValues() != null) {
						List<CustomFieldValue> previousCfValues = entity.getCfValues().getValuesByCode().get(cft.getCode());
						if (previousCfValues != null && !previousCfValues.isEmpty()) {
							newValuesByCode.put(cft.getCode(), previousCfValues);
						}
					}
					continue;
				}

				for (CustomFieldValue cfValue : entityFieldsValues.getValues(cft)) {
					// Not saving empty values unless template has a default value or is versionable
					// (to prevent that for SINGLE type CFT with a default value, value is
					// instantiates automatically)
					// Also don't save if CFT does not apply in a given entity lifecycle or because
					// cft.applicableOnEL evaluates to false
					if ((cfValue.isValueEmptyForGui() //
							&& (cft.getDefaultValue() == null || cft.getStorageType() != CustomFieldStorageTypeEnum.SINGLE) && !cft.isVersionable()) //
							|| ((isNewEntity && cft.isHideOnNew()) //
									|| (entity != null && !MeveoValueExpressionWrapper.evaluateToBooleanOneVariable(cft.getApplicableOnEl(), "entity", entity))) //
					) {
						log.trace("Will ommit from saving cfi {}", cfValue);

						// Existing value update
					} else {
						serializeFromGUI(cfValue, cft);

						if (!newValuesByCode.containsKey(cft.getCode())) {
							newValuesByCode.put(cft.getCode(), new ArrayList<>());
						}
						newValuesByCode.get(cft.getCode()).add(cfValue);
					}
				}
			}
		}
		// Update entity custom values field

		if (isSaveEntity && entity != null) {
			if (newValuesByCode.isEmpty()) {
				entity.clearCfValues();
				
			} else {
				entity.getCfValuesNullSafe().setValuesByCode(newValuesByCode);
			}
		}

		return newValuesByCode;
	}

	/**
	 * Get a child entity column corresponding to a given code
	 *
	 * @param childEntityTypeFieldDefinition Child entity type field definition
	 * @param childFieldCode                 Child entity field code
	 * @return customFieldTemplate
	 */
	public CustomFieldTemplate getChildEntityField(CustomFieldTemplate childEntityTypeFieldDefinition, String childFieldCode) {

		Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(
				EntityCustomizationUtils.getAppliesTo(CustomEntityTemplate.class, CustomFieldTemplate.retrieveCetCode(childEntityTypeFieldDefinition.getEntityClazz())));

		return cfts.get(childFieldCode);
	}

	/**
	 * Prepare new child entity record for data entry
	 *
	 * @param mainEntityValueHolder      Entity custom field value holder
	 * @param mainEntityCfv              Main entity's custom field value containing
	 *                                   child entities
	 * @param childEntityFieldDefinition Custom field template of child entity type,
	 *                                   definition, corresponding to cfv
	 */
	public void newChildEntity(CustomFieldValueHolder mainEntityValueHolder, CustomFieldValue mainEntityCfv, CustomFieldTemplate childEntityFieldDefinition) {

		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setCetCode(CustomFieldTemplate.retrieveCetCode(childEntityFieldDefinition.getEntityClazz()));
		cei.setParentEntityUuid(mainEntityValueHolder.getEntityUuid());

		initFields(cei);

		CustomFieldValueHolder childEntityValueHolder = getFieldValueHolderByUUID(cei.getUuid());

		mainEntityValueHolder.setSelectedChildEntity(childEntityValueHolder);
	}

	/**
	 * Save child entity record
	 *
	 * @param mainEntityValueHolder      Main entity custom field value holder
	 * @param mainEntityCfv              Main entity's custom field value containing
	 *                                   child entities
	 * @param childEntityFieldDefinition Custom field template of child entity type,
	 *                                   definition, corresponding to cfv
	 */
	public void saveChildEntity(CustomFieldValueHolder mainEntityValueHolder, CustomFieldValue mainEntityCfv, CustomFieldTemplate childEntityFieldDefinition) {

		CustomEntityInstance cei = (CustomEntityInstance) mainEntityValueHolder.getSelectedChildEntity().getEntity();
		if (!validateCustomFields(cei)) {
			return;
		}

		// try {
		String message = "customFieldInstance.childEntity.save.successful";

		CustomFieldValueHolder childEntityValueHolder = mainEntityValueHolder.getSelectedChildEntity();
		childEntityValueHolder.setUpdated(true);

		if (mainEntityCfv.getChildEntityValuesForGUI().contains(childEntityValueHolder)) {
			mainEntityCfv.getChildEntityValuesForGUI().set(mainEntityCfv.getChildEntityValuesForGUI().indexOf(childEntityValueHolder), childEntityValueHolder);
			message = "customFieldInstance.childEntity.update.successful";

		} else {
			mainEntityCfv.getChildEntityValuesForGUI().add(childEntityValueHolder);
		}
		messages.info(new BundleKey("messages", message));

		// } catch (BusinessException e) {
		// log.error("Failed to save child entity {} {}",
		// childEntityFieldDefinition.getCode(), mainEntityValueHolder, e);
		// messages.error(new BundleKey("messages", "error.action.failed"),
		// e.getMessage());
		// }
	}

	/**
	 * Prepare to edit child entity
	 *
	 * @param mainEntityValueHolder Main entity custom field value holder
	 * @param selectedChildEntity   Child entity custom field value holder
	 */
	public void editChildEntity(CustomFieldValueHolder mainEntityValueHolder, CustomFieldValueHolder selectedChildEntity) {
		mainEntityValueHolder.setSelectedChildEntity(selectedChildEntity);
		fieldsValues.put(selectedChildEntity.getEntityUuid(), selectedChildEntity);
	}

	/**
	 * Remove child entity record from a given field
	 *
	 * @param mainEntityCfv       Main entity's custom field value containing child
	 *                            entities
	 * @param selectedChildEntity Child entity record to remove
	 */
	public void removeChildEntity(CustomFieldValue mainEntityCfv, CustomFieldValueHolder selectedChildEntity) {

		mainEntityCfv.getChildEntityValuesForGUI().remove(selectedChildEntity);
		fieldsValues.remove(selectedChildEntity.getEntityUuid());
		messages.info(new BundleKey("messages", "customFieldInstance.childEntity.delete.successful"));
	}

	/**
	 * Prepare new child entity record for data entry
	 *
	 * @param mainEntityCfv Main entity's custom field value containing child
	 *                      entities
	 */
	public void attachChildEntity(CustomFieldValue mainEntityCfv) throws BusinessException {
		CustomEntityInstance cei = new CustomEntityInstance();
		Map<String, CustomFieldTemplate> customFieldTemplates = null;
		if (!customEntityTemplate.isStoreAsTable()) {
			customFieldTemplates = customFieldTemplateService.findByAppliesTo(entityInstance);
			cei = entityInstance;

	 	} else {
			customFieldTemplates = customFieldTemplateService.findByAppliesTo(customEntityTemplate.getAppliesTo());
			CustomEntityInstance customEntityInstance = new CustomEntityInstance();
			customEntityInstance.setCet(customEntityTemplate);
			customEntityInstance.setCetCode(customEntityTemplateCode);
			customEntityInstance.setUuid((String) entityInstanceTable.get("uuid"));
			customFieldInstanceService.setCfValues(customEntityInstance, customEntityTemplate.getCode(), entityInstanceTable);

			cei = customEntityInstance;
		}

		customFieldTemplates = sortByValue(customFieldTemplates);

		GroupedCustomField groupedCustomField = new GroupedCustomField(customFieldTemplates.values(), "Custom fields", false);
		groupedFieldTemplates.put(cei.getUuid(), groupedCustomField);

		Map<String, List<CustomFieldValue>> cfValuesByCode = null;
		// Get custom field instances mapped by a CFT code if entity has any field
		// defined
		// if (!((IEntity) entity).isTransient() && customFieldTemplates != null &&
		// customFieldTemplates.size() > 0) {
		// No longer checking for isTransient as for offer new version creation, CFs are
		// duplicated, but entity is not persisted, offering to review it in GUI before
		// saving it.
		if (customFieldTemplates != null && customFieldTemplates.size() > 0 && ((ICustomFieldEntity) cei).getCfValues() != null) {
			cfValuesByCode = ((ICustomFieldEntity) cei).getCfValues().getValuesByCode();
		}
		cfValuesByCode = prepareCFIForGUI(customFieldTemplates, cfValuesByCode, cei);
		CustomFieldValueHolder entityFieldsValues = new CustomFieldValueHolder(customFieldTemplates, cfValuesByCode, cei);
		fieldsValues.put(cei.getUuid(), entityFieldsValues);
		CustomFieldValueHolder childEntityValueHolder = getFieldValueHolderByUUID(cei.getUuid());

		String message = "customFieldInstance.childEntity.save.successful";

		mainEntityCfv.getChildEntityValuesForGUI().add(childEntityValueHolder);
		messages.info(new BundleKey("messages", message));
	}

	/**
	 * Serialize map, list and entity reference values that were adapted for GUI
	 * data entry. See CustomFieldValue.xxxGUI fields for transformation description
	 *
	 * @param customFieldValue Value to serialize
	 * @param cft              Custom field template
	 * @throws BusinessException
	 */
	private void serializeFromGUI(CustomFieldValue customFieldValue, CustomFieldTemplate cft) {

		// Convert JPA object to Entity reference - just Single storage fields
		if (cft.getStorageType() == CustomFieldStorageTypeEnum.SINGLE && cft.getFieldType() == CustomFieldTypeEnum.ENTITY) {
			if (customFieldValue.getEntityReferenceValueForGUI() == null) {
				customFieldValue.setEntityReferenceValue(null);
			} else {
				customFieldValue.setEntityReferenceValue(new EntityReferenceWrapper(customFieldValue.getEntityReferenceValueForGUI()));
			}

		// Convert CustomFieldValueHolder object to EntityReferenceWrapper- ONLY LISTstorage type field
		} else if (cft.getFieldType() == CustomFieldTypeEnum.CHILD_ENTITY) {

			if(cft.getStorageType().equals(CustomFieldStorageTypeEnum.LIST)) {
				List<Object> listValue = new ArrayList<Object>();
				for (CustomFieldValueHolder childEntityValueHolder : customFieldValue.getChildEntityValuesForGUI()) {
					ICustomFieldEntity cei = childEntityValueHolder.getEntity();
					cei.getCfValuesNullSafe().getValuesByCode().putAll(childEntityValueHolder.getValuesByCode());
					listValue.add(cei.getCfValuesAsValues());
				}
				customFieldValue.setListValue(listValue);
				
			} else if(cft.getStorageType().equals(CustomFieldStorageTypeEnum.SINGLE)) {
				CustomFieldValueHolder childEntityValueHolder = customFieldValue.getChildEntityValuesForGUI().get(0);
				ICustomFieldEntity cei = childEntityValueHolder.getEntity();
				cei.getCfValuesNullSafe().getValuesByCode().putAll(childEntityValueHolder.getValuesByCode());
				customFieldValue.setMapValue(cei.getCfValuesAsValues());
			}

			// Populate customFieldValue.listValue from mapValuesForGUI field
		} else if (cft.getStorageType() == CustomFieldStorageTypeEnum.LIST) {

			List<Object> listValue = new ArrayList<Object>();
			for (Map<String, Object> listItem : customFieldValue.getMapValuesForGUI()) {
				if (cft.getFieldType() == CustomFieldTypeEnum.ENTITY && !(listItem.get(CustomFieldValue.MAP_VALUE) instanceof EntityReferenceWrapper)) {
					listValue.add(new EntityReferenceWrapper((BusinessEntity) listItem.get(CustomFieldValue.MAP_VALUE)));

				} else {
					listValue.add(listItem.get(CustomFieldValue.MAP_VALUE));
				}
			}
			customFieldValue.setListValue(listValue);

			// Populate customFieldValue.mapValue from mapValuesForGUI field
		} else if (cft.getStorageType() == CustomFieldStorageTypeEnum.MAP) {

			Map<String, Object> mapValue = new LinkedHashMap<String, Object>();

			for (Map<String, Object> listItem : customFieldValue.getMapValuesForGUI()) {
				String key = (String) listItem.get(CustomFieldValue.MAP_KEY);
				Object value = listItem.get(CustomFieldValue.MAP_VALUE);
				
				if (cft.getFieldType() == CustomFieldTypeEnum.ENTITY) {
					if (value instanceof BusinessEntity) {
						mapValue.put(key, new EntityReferenceWrapper((BusinessEntity) value));
					} else if (value instanceof String) {
						EntityReferenceWrapper entityRefWrapper = new EntityReferenceWrapper();
						entityRefWrapper.setClassnameCode(cft.getEntityClazzCetCode());
						entityRefWrapper.setUuid((String) value);
						mapValue.put(key, entityRefWrapper);
					} else {
						mapValue.put(key, new EntityReferenceWrapper((BusinessEntity) value));
					}

				} else {
					mapValue.put((String) listItem.get(CustomFieldValue.MAP_KEY), listItem.get(CustomFieldValue.MAP_VALUE));
				}
			}
			customFieldValue.setMapValue(mapValue);

			// Populate customFieldValue.mapValue from matrixValuesForGUI field
		} else if (cft.getStorageType() == CustomFieldStorageTypeEnum.MATRIX) {

			Map<String, Object> mapValue = new LinkedHashMap<String, Object>();

			List<String> keyColumns = cft.getMatrixKeyColumnCodes();

			for (Map<String, Object> mapItem : customFieldValue.getMatrixValuesForGUI()) {

				Object value = null;

				// Multi-value values need to be concatenated and stored as string
				if (cft.getFieldType() == CustomFieldTypeEnum.MULTI_VALUE) {

					value = cft.serializeMultiValue(mapItem);
					if (value == null) {
						continue;
					}

				} else {
					value = mapItem.get(CustomFieldValue.MAP_VALUE);
					if (StringUtils.isBlank(value)) {
						continue;
					}

					if (cft.getFieldType() == CustomFieldTypeEnum.ENTITY) {
						value = new EntityReferenceWrapper((BusinessEntity) value);
					}
				}

				StringBuilder keyBuilder = new StringBuilder();
				for (String column : keyColumns) {
					keyBuilder.append(keyBuilder.length() == 0 ? "" : CustomFieldValue.MATRIX_KEY_SEPARATOR);
					keyBuilder.append(mapItem.get(column));
				}

				mapValue.put(keyBuilder.toString(), value);

			}

			customFieldValue.setMapValue(mapValue);
		}
	}

	/**
	 * Deserialize map, list and entity reference values to adapt them for GUI data
	 * entry. See CustomFieldValue.xxxGUI fields for transformation description
	 *
	 * @param cft Custom field template
	 */
	@SuppressWarnings("unchecked")
	private void deserializeForGUI(CustomFieldValue customFieldValue, CustomFieldTemplate cft) {

		// Convert just Entity type field to a JPA object - just Single storage fields
		if (cft.getStorageType() == CustomFieldStorageTypeEnum.SINGLE && cft.getFieldType() == CustomFieldTypeEnum.ENTITY) {
			customFieldValue.setEntityReferenceValueForGUI(deserializeEntityReferenceForGUI(customFieldValue.getEntityReferenceValue()));

		} else if (cft.getFieldType() == CustomFieldTypeEnum.CHILD_ENTITY) {
			// Populate childEntityValuesForGUI field - ONLY LIST storage is supported
			List<CustomFieldValueHolder> cheHolderList = new ArrayList<>();

			String childCetCode = cft.getEntityClazzCetCode();
			var cet = customEntityTemplateService.findByCode(childCetCode);
			var childFields = customFieldTemplateService.findByAppliesTo(CustomEntityTemplate.getAppliesTo(childCetCode));
			
			if (customFieldValue.getListValue() != null) {
				for (Object listItem : customFieldValue.getListValue()) {
					EntityReferenceWrapper entityReferenceWrapper = null;
					CustomFieldValueHolder childEntityValueHolder = null;

					if (listItem instanceof EntityReferenceWrapper) {
						entityReferenceWrapper = (EntityReferenceWrapper) listItem;
						childEntityValueHolder = loadChildEntityForGUI(entityReferenceWrapper);

					} else if (listItem instanceof Map) {
						Map<String, Object> values = (Map<String, Object>) listItem;
						String entityCode = (String) values.get("code");
						Long entityId = (Long) values.getOrDefault("id", null);

						String classnameCode = (String) values.get("classnameCode");
						if(classnameCode != null) {
							entityReferenceWrapper = new EntityReferenceWrapper(CustomEntityInstance.class.getName(), classnameCode, entityCode, entityId);
							childEntityValueHolder = initCustomFieldValueHolderFromMap(entityReferenceWrapper, values);
						} else {
							var childCei = CEIUtils.fromMap(values, cet);
							childCei.setUuid(UUID.randomUUID().toString());
							childEntityValueHolder = new CustomFieldValueHolder(childFields, childCei.getCfValues().getValuesByCode(), childCei);
						}
					}

					if (childEntityValueHolder != null) {
						cheHolderList.add(childEntityValueHolder);
					}
				}
			} else if(customFieldValue.getMapValue() != null) {
				var childCei = CEIUtils.fromMap(customFieldValue.getMapValue(), cet);
				childCei.setUuid(UUID.randomUUID().toString());
				CustomFieldValueHolder childEntityValueHolder = new CustomFieldValueHolder(childFields, childCei.getCfValues().getValuesByCode(), childCei);
				cheHolderList.add(childEntityValueHolder);
			}
			
			customFieldValue.setChildEntityValuesForGUI(cheHolderList);

		} else if (cft.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
			// Populate mapValuesForGUI field
			List<Map<String, Object>> listOfMapValues = new ArrayList<Map<String, Object>>();

			if (customFieldValue.getListValue() != null) {
				for (Object listItem : customFieldValue.getListValue()) {
					Map<String, Object> listEntry = new HashMap<String, Object>();
					listEntry.put(CustomFieldValue.MAP_VALUE, listItem);
					listOfMapValues.add(listEntry);
				}
			}
			customFieldValue.setMapValuesForGUI(listOfMapValues);

			// Populate mapValuesForGUI field
		} else if (cft.getStorageType() == CustomFieldStorageTypeEnum.MAP) {

			List<Map<String, Object>> listOfMapValues = new ArrayList<Map<String, Object>>();

			if (customFieldValue.getMapValue() != null) {
				for (Entry<String, Object> mapInfo : ((Map<String, Object>) customFieldValue.getMapValue()).entrySet()) {
					Map<String, Object> listEntry = new HashMap<String, Object>();
					listEntry.put(CustomFieldValue.MAP_KEY, mapInfo.getKey());
					listEntry.put(CustomFieldValue.MAP_VALUE, mapInfo.getValue());
					listOfMapValues.add(listEntry);
				}
			}
			customFieldValue.setMapValuesForGUI(listOfMapValues);

			// Populate matrixValuesForGUI field
		} else if (cft.getStorageType() == CustomFieldStorageTypeEnum.MATRIX) {

			List<Map<String, Object>> mapValues = new ArrayList<Map<String, Object>>();
			customFieldValue.setMatrixValuesForGUI(mapValues);

			if (customFieldValue.getMapValue() != null) {

				List<String> keyColumnCodes = cft.getMatrixKeyColumnCodes();

				for (Entry<String, Object> mapItem : ((Map<String, Object>) customFieldValue.getMapValue()).entrySet()) {
					if (mapItem.getKey().equals(CustomFieldValue.MAP_KEY)) {
						continue;
					}

					Map<String, Object> mapItemKeysAndValues = new HashMap<String, Object>();
					if (cft.getFieldType() == CustomFieldTypeEnum.ENTITY) {
						mapItemKeysAndValues.put(CustomFieldValue.MAP_VALUE, deserializeEntityReferenceForGUI((EntityReferenceWrapper) mapItem.getValue()));

					} else if (cft.getFieldType() == CustomFieldTypeEnum.MULTI_VALUE) {
						cft.deserializeMultiValue((String) mapItem.getValue(), mapItemKeysAndValues);

					} else {
						mapItemKeysAndValues.put(CustomFieldValue.MAP_VALUE, mapItem.getValue());
					}

					// Matrix keys are concatenated when stored - split them and set as separate map
					// key/values
					String[] keys = mapItem.getKey().split("\\" + CustomFieldValue.MATRIX_KEY_SEPARATOR);
					for (int i = 0; i < keys.length; i++) {
						mapItemKeysAndValues.put(keyColumnCodes.get(i), keys[i]);
					}

					mapValues.add(mapItemKeysAndValues);
				}
			}
		}
	}

	/**
	 * Covert entity reference to a Business entity JPA object.
	 *
	 * @param entityReferenceValue Entity reference value
	 * @return Business entity JPA object
	 */
	private BusinessEntity deserializeEntityReferenceForGUI(EntityReferenceWrapper entityReferenceValue) {
		if (entityReferenceValue == null) {
			return null;
		}
		// NOTE: For PF autocomplete seems that fake BusinessEntity object with code
		// value filled is sufficient - it does not have to be a full loaded JPA object

		try {
			BusinessEntity convertedEntity = entityReferenceValue.getClassname() != null
					? (BusinessEntity) ReflectionUtils.createObject(entityReferenceValue.getClassname())
					: null;
					
			if (convertedEntity != null) {
				if (convertedEntity instanceof CustomEntityInstance) {
					((CustomEntityInstance) convertedEntity).setCetCode(entityReferenceValue.getClassnameCode());
				}

				convertedEntity.setCode(entityReferenceValue.getCode());
			} else {
				convertedEntity = new BusinessEntity();
				convertedEntity.setCode(entityReferenceValue.getCode() != null ? entityReferenceValue.getCode() : entityReferenceValue.getUuid());
			}
			
			return convertedEntity;

		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(this.getClass());
			log.error("Unknown entity class specified in a custom field value {} ", entityReferenceValue);
			return null;
		}
	}

	/**
	 * Convert childEntity field type value of EntityReferenceWrapper type to GUI
	 * suitable format - CustomFieldValueHolder. Entity is loaded from db with all
	 * related custom fields.
	 *
	 * @param childEntityWrapper EntityReferenceWrapper value to convert
	 * @return CustomFieldValueHolder instance
	 */
	private CustomFieldValueHolder loadChildEntityForGUI(EntityReferenceWrapper childEntityWrapper) {
		if (childEntityWrapper == null) {
			return null;
		}

		CustomEntityInstance cei = customEntityInstanceService.findByCodeByCet(childEntityWrapper.getClassnameCode(), childEntityWrapper.getCode());
		if (cei == null) {
			return null;
		}
		initFields(cei);
		return fieldsValues.get(cei.getUuid());
	}

	/**
	 * Convert childEntity field type value of EntityReferenceWrapper type to GUI
	 * suitable format - CustomFieldValueHolder. Entity is created from map values.
	 *
	 * @param childEntityWrapper EntityReferenceWrapper value to convert
	 * @return CustomFieldValueHolder instance
	 */
	private CustomFieldValueHolder initCustomFieldValueHolderFromMap(EntityReferenceWrapper childEntityWrapper, Map<String, Object> values) {

		// Init CEI
		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setCetCode(childEntityWrapper.getClassnameCode());
		cei.setCode(childEntityWrapper.getCode());
		cei.setUuid((String) values.get("uuid"));
		cei.setDescription((String) values.get("description"));

		// Init fields of CEI
		Map<String, CustomFieldTemplate> fields = cache.getCustomFieldTemplates(CustomEntityTemplate.getAppliesTo(cei.getCetCode()));
		if (cei.getUuid() != null) {
			CustomEntityTemplate customEntityTemplate = customEntityTemplateService.findByCode(cei.getCetCode());
			try {
				values = crossStorageService.find(repositoryService.findDefaultRepository(), customEntityTemplate, cei.getUuid(), true);
				if (values != null) {
					values.remove("uuid");
				}
			} catch (EntityDoesNotExistsException e) {
				log.error(e.getMessage());
			}
		}
		CustomFieldValues customFieldValues = CETUtils.initCustomFieldValues(values, fields.values());
		cei.setCfValues(customFieldValues);

		// Init holder
		CustomFieldValueHolder entityFieldsValues = new CustomFieldValueHolder(fields, customFieldValues.getValuesByCode(), cei);
		return entityFieldsValues;
	}

	/**
	 * Save custom fields for a given entity
	 *
	 * @param entity Entity, the fields relate to
	 * @throws BusinessException
	 */
	public Map<CustomFieldTemplate, Object> loadCustomFieldsFromGUI(ICustomFieldEntity entity) throws BusinessException {
		Map<CustomFieldTemplate, Object> fieldMap = new HashMap<>();
		String uuid = entity.getUuid();
		CustomFieldValueHolder entityFieldsValues = getFieldValueHolderByUUID(uuid);
		GroupedCustomField groupedCustomFields = groupedFieldTemplates.get(uuid);
		if (groupedCustomFields != null) {
			for (CustomFieldTemplate cft : groupedCustomFields.getFields()) {
				for (CustomFieldValue cfValue : entityFieldsValues.getValues(cft)) {
					serializeFromGUI(cfValue, cft);
					if (CustomFieldTypeEnum.ENTITY.equals(cft.getFieldType())) {
						fieldMap.put(cft, cfValue.getEntityReferenceValueForGUI());
					} else {
						fieldMap.put(cft, cfValue.getValue());
					}
				}
			}
		}
		return fieldMap;
	}

	/**
	 * Get custom field values for a given entity - in case of versioned custom
	 * fields, retrieve the latest value
	 *
	 * @param entity Entity, the fields relate to
	 * @throws BusinessException
	 */
	public Map<CustomFieldTemplate, Object> getFieldValuesLatestValue(ICustomFieldEntity entity) throws BusinessException {
		Map<CustomFieldTemplate, Object> fieldMap = new HashMap<>();
		String uuid = entity.getUuid();
		CustomFieldValueHolder entityFieldsValues = getFieldValueHolderByUUID(uuid);
		GroupedCustomField groupedCustomFields = groupedFieldTemplates.get(uuid);
		if (groupedCustomFields != null) {
			for (CustomFieldTemplate cft : groupedCustomFields.getFields()) {

				for (CustomFieldValue cfValue : entityFieldsValues.getValues(cft)) {

					try {
						serializeFromGUI(cfValue, cft);
						fieldMap.put(cft, cfValue.getValue());

					} catch (Exception e) {
						log.error("Failed to convert custom field to product characteristic {} {}", cft.getCode(), cfValue);
					}
				}
			}
		}
		return fieldMap;
	}

	/**
	 * Set values of custom fields
	 *
	 * @param cfValues A map of custom field values with CFT as a key and CF value
	 *                 as a value
	 * @param entity   Entity custom field values apply to
	 */
	public void setCustomFieldValues(Map<CustomFieldTemplate, Object> cfValues, BusinessCFEntity entity) {

		if (entity == null) {
			return;
		}

		if (!groupedFieldTemplates.containsKey(entity.getUuid())) {
			initFields(entity);
		}

		CustomFieldValueHolder entityFieldsValues = getFieldValueHolderByUUID(entity.getUuid());

		for (Entry<CustomFieldTemplate, Object> cfValueInfo : cfValues.entrySet()) {
			CustomFieldValue cfValue = entityFieldsValues.getFirstValue(cfValueInfo.getKey().getCode());
			if (cfValue == null) {
				// log.error("AKK not CFI found in holder for {}",
				// cfValueInfo.getKey().getCode());
				continue;
			}
			cfValue.setValue(cfValueInfo.getValue());
			deserializeForGUI(cfValue, cfValueInfo.getKey());
		}
	}

	/**
	 * Get names of repeated custom field component forms and tabs ids
	 *
	 * @param prefix prefix to apply
	 * @param suffix suffix to apply
	 * @param length Number of repeated items
	 * @return A concatenated string of component ID values
	 */
	public static String getCFComponentIds(String prefix, String suffix, int length) {
		if (length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(prefix + i + (suffix != null ? suffix : "") + " ");
		}

		return sb.deleteCharAt(sb.length() - 1).toString();
	}

	public int getActiveGroupedFieldTemplatesCount(ICustomFieldEntity entity) {

		if (entity == null) {
			return 0;
		}
		if (!groupedFieldTemplates.containsKey(entity.getUuid())) {
			initFields(entity);
		}

		GroupedCustomField groupCF = groupedFieldTemplates.get(entity.getUuid());
		CustomFieldValueHolder cfValueHolder = getFieldValueHolderByUUID(entity.getUuid());
		int ctr = 0;
		for (GroupedCustomField groupCFChild : groupCF.getChildren()) {
			if (groupCFChild.hasVisibleCustomFields(entity, cfValueHolder)) {
				ctr++;
			}
		}

		return ctr;
	}

	/**
	 * Get currently active locale
	 *
	 * @return Currently active locale
	 */
	public Locale getCurrentLocale() {
		return FacesContext.getCurrentInstance().getViewRoot().getLocale();
	}

	/**
	 * Calculate a parent JSF component id based on a given component id
	 *
	 * @param componentId Component identifier
	 * @return A parent JSF component id
	 */
	public String getParentComponentId(String componentId) {

		int index = componentId.lastIndexOf(':');
		if (index > 0) {
			return componentId.substring(0, index);
		}
		return componentId;
	}

	public boolean isEmbeddedEntity(ICustomFieldEntity entity) {
		Map<String, CustomFieldTemplate> customFieldTemplates = customFieldTemplateService.findByAppliesTo(entity);
		for (CustomFieldTemplate cft : customFieldTemplates.values()) {
			if (cft.getFieldType() == CustomFieldTypeEnum.EMBEDDED_ENTITY) {
				return true;
			}
		}
		return false;
	}

	public String getSegmentTree(ICustomFieldEntity entity) {
		String segmentTreeValue = null;
		Map<String, CustomFieldTemplate> customFieldTemplates = customFieldTemplateService.findByAppliesTo(entity);
		for (CustomFieldTemplate cft : customFieldTemplates.values()) {
			if (cft.getFieldType() == CustomFieldTypeEnum.EMBEDDED_ENTITY) {
				var cfValue = customFieldInstanceService.getCFValue(entity, cft.getCode());
				if(cfValue instanceof String) {
					segmentTreeValue = (String) cfValue;
				} else {
					segmentTreeValue = JacksonUtil.toString(cfValue);
				}
				
				if (segmentTreeValue == null) {
					segmentTreeValue = "{}";
				}
				log.info("getSegmentTree segmentValue={}", segmentTreeValue);
			}
		}
		return segmentTreeValue;
	}

	public String getCustomEntityTemplateCode() {
		return customEntityTemplateCode;
	}

	public void setCustomEntityTemplateCode(String customEntityTemplateCode) {
		this.customEntityTemplateCode = customEntityTemplateCode;
	}

	public String getCustomEntityInstanceCode() {
		return customEntityInstanceCode;
	}

	public void setCustomEntityInstanceCode(String customEntityInstanceCode) {
		this.customEntityInstanceCode = customEntityInstanceCode;
	}

	public String getCustomEntityInstanceUuid() {
		return customEntityInstanceUuid;
	}

	public void setCustomEntityInstanceUuid(String customEntityInstanceUuid) {
		this.customEntityInstanceUuid = customEntityInstanceUuid;
	}

	public CustomEntityInstance getEntityInstance() {
		return entityInstance;
	}

	public void setEntityInstance(CustomEntityInstance entityInstance) {
		this.entityInstance = entityInstance;
	}

	public void initializeCustomEntityTemplateCode(String entityClazz) {
		customEntityTemplateCode = CustomFieldTemplate.retrieveCetCode(entityClazz);
		if (customEntityTemplate != null && customEntityTemplate.isStoreAsTable()) {
			customEntityInstanceUuid = null;
			entityInstanceTable = null;
			entityInstancesForTable = customTableService.list("default", customEntityTemplate);
		} else if (customEntityTemplate != null && !customEntityTemplate.isStoreAsTable()){
			customEntityInstanceCode = null;
			entityInstance = null;
			entityInstances = customEntityInstanceService.findByCode(customEntityTemplateCode, customEntityInstanceCode);
		}
	}

	public boolean isStoreTable(String entityClazz) {
		customEntityTemplateCode = CustomFieldTemplate.retrieveCetCode(entityClazz);
		customEntityTemplate = customEntityTemplateService.findByCode(customEntityTemplateCode);
		return customEntityTemplate.isStoreAsTable();
	}

	public String getFieldName() {

		if (customEntityTemplate != null) {
			Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(customEntityTemplate.getAppliesTo());
			if (cfts != null) {
				List<String> identifierFields = cfts.values().stream().filter(f -> f.isIdentifier()).map(CustomFieldTemplate::getDbFieldname).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(identifierFields)) {
					return fieldName = identifierFields.get(0);
				}
				List<String> requireFields = cfts.values().stream().filter(f -> f.isValueRequired()).map(CustomFieldTemplate::getDbFieldname).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(requireFields)) {
					return fieldName = requireFields.get(0);
				}
				List<String> summaryFields = cfts.values().stream().filter(f -> f.isSummary()).map(CustomFieldTemplate::getDbFieldname).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(summaryFields)) {
					return fieldName = summaryFields.get(0);
				}
			}
		}
		return fieldName = null;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public List<CustomEntityInstance> getEntityInstances() {
		return entityInstances;
	}

	public void setEntityInstances(List<CustomEntityInstance> entityInstances) {
		this.entityInstances = entityInstances;
	}

	public List<Map<String, Object>> getEntityInstancesForTable() {
		return entityInstancesForTable;
	}

	public void setEntityInstancesForTabe(List<Map<String, Object>> entityInstancesForTable) {
		this.entityInstancesForTable = entityInstancesForTable;
	}

	public Map<String, Object> getEntityInstanceTable() {
		return entityInstanceTable;
	}

	public void setEntityInstanceTable(Map<String, Object> entityInstanceTable) {
		this.entityInstanceTable = entityInstanceTable;
	}

	public void search() {
		if (!customEntityTemplate.isStoreAsTable()) {
			entityInstances = customEntityInstanceService.findByCode(customEntityTemplateCode, customEntityInstanceCode);
		} else {
			Map<String, Object> filters = new HashMap<>();
			filters.put("uuid", customEntityInstanceUuid);
			PaginationConfiguration paginationConfiguration = new PaginationConfiguration(0, 1000, filters, null, null, null, null, null);
			entityInstancesForTable = customTableService.searchAndFetch("default", customEntityTemplateCode, paginationConfiguration);
		}
	}

	public void clean() {
		if (!customEntityTemplate.isStoreAsTable()) {
			customEntityInstanceCode = null;
			entityInstances = customEntityInstanceService.findByCode(customEntityTemplateCode, customEntityInstanceCode);
		} else {
			entityInstancesForTable = customTableService.list("default", customEntityTemplate);
		}
	}

	/**
	 * @return the {@link #binaries}
	 */
	public Map<String, UploadedFile> getBinaries() {
		return binaries;
	}

	/**
	 * @param binaries the binaries to set
	 */
	public void setBinaries(Map<String, UploadedFile> binaries) {
		this.binaries = binaries;
	}

	public void handleFileUpload(FileUploadEvent event) throws BusinessException, IOException {

		log.debug("handleFileUpload {}", event.getFile().getFileName());
		String uuid = (String) event.getComponent().getAttributes().get("uuid");
		CustomFieldTemplate cft = (CustomFieldTemplate) event.getComponent().getAttributes().get("cft");

		var file = event.getFile();
		binaries.put(cft.getCode(),  event.getFile());
		
		if (repository != null) {
			repository = repositoryService.retrieveIfNotManaged(repository);
		}

		String rootPath = repository != null && repository.getBinaryStorageConfiguration() != null ? repository.getBinaryStorageConfiguration().getRootPath() : "";

		String cetCode = (String) event.getComponent().getAttributes().get("cetCode");
		CustomFieldValue cfv = (CustomFieldValue) event.getComponent().getAttributes().get("cfv");
		boolean isSingle = !cft.getStorageType().equals(CustomFieldStorageTypeEnum.LIST) && !cft.getStorageType().equals(CustomFieldStorageTypeEnum.MAP);

		BinaryStoragePathParam params = new BinaryStoragePathParam();
		params.setShowOnExplorer(cft.isSaveOnExplorer());
		params.setRootPath(rootPath);
		params.setCetCode(cetCode);
		params.setUuid(uuid);
		params.setCftCode(cft.getCode());
		params.setFilePath(cft.getFilePath());
		params.setContentType(file.getContentType());
		params.setFilename(file.getFileName());
		params.setInputStream(file.getInputstream());
		params.setFileSizeInBytes(file.getSize());
		params.setFileExtensions(cft.getFileExtensions());
		params.setContentTypes(cft.getContentTypes());
		params.setMaxFileSizeAllowedInKb(cft.getMaxFileSizeAllowedInKb());

		// FIXME: Maybe here he takes the existing values and somehow delete them
		rootPath = fileSystemService.persists(params, entity.getCfValuesAsValues());

		log.debug("binary path={}", rootPath);

		if (isSingle) {
			cfv.setStringValue(rootPath);

			initAfterUpload();

		} else {
			List<Map<String, Object>> mapValues = cfv.getMapValuesForGUI();
			Map<String, Object> mapValue = new HashMap<>();
			if (mapValues != null && !mapValues.isEmpty()) {
				mapValue.put(CustomFieldValue.MAP_VALUE, rootPath);
				mapValues.add(mapValue);

			} else {
				mapValues = new ArrayList<>();
				mapValue.put(CustomFieldValue.MAP_VALUE, rootPath);
				mapValues.add(mapValue);
			}
			cfv.setMapValuesForGUI(mapValues);
		}
	}

	private void initAfterUpload() {
		// inaries = new HashMap<>();
		// repository = null;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public StreamedContent downloadFile(String path) throws IOException {

		if (StringUtils.isBlank(path)) {
			return null;
		}

		File file = new File(path);
		InputStream stream = new FileInputStream(file);

		String filename = FilenameUtils.getName(path);
		String mimeType = Files.probeContentType(file.toPath());

		return new DefaultStreamedContent(stream, mimeType, filename);
	}

	@SuppressWarnings("unchecked")
	@ActionMethod
	public void onEntityReferenceSelected(SelectEvent event) {
		Map<String, Object> selectedEntityInPopup = (Map<String, Object>) event.getObject();
		String newId = (String) selectedEntityInPopup.get("uuid");
		listValues.add(newId);
	}

	public List<String> getFilesToDeleteOnExit() {
		return filesToDeleteOnExit;
	}

	public Object getValueObject(CustomFieldValueHolder childEntityValue, String fieldCode, String entityClazz) {
		Object value = null;
		if (childEntityValue != null && childEntityValue.getValuesByCode() != null && isStoreTable(entityClazz)) {
			CustomEntityInstance customEntityInstance = (CustomEntityInstance) childEntityValue.getEntity();
			if (customEntityInstance != null) {
				CustomEntityTemplate customEntityTemplate = customEntityTemplateService.findByCode(customEntityInstance.getCetCode());
				CustomFieldTemplate customFieldTemplate = customFieldTemplateService.findByCodeAndAppliesTo(fieldCode, customEntityTemplate.getAppliesTo());
				Map<String, Object> childEntity = customTableService.findById(SqlConfiguration.DEFAULT_SQL_CONNECTION, customEntityTemplate, customEntityInstance.getUuid());
				if (childEntity != null) {
					value = childEntity.get(fieldCode);
					if (customFieldTemplate.getFieldType() == CustomFieldTypeEnum.BOOLEAN && value instanceof Integer) {
						if ((Integer) value == 1) {
							return true;
						} else {
							return false;
						}
					}
				}
			}
		}
		return value;
	}
	
	/**
	 * @param cetCode code of the cet
	 * @return the identifier field for the cet, or null if there is not
	 */
	public CustomFieldTemplate getIdentifierField(String cetCode) {
		return customFieldTemplateService.findByAppliesTo(CustomEntityTemplate.getAppliesTo(cetCode))
				.values()
				.stream()
				.filter(CustomFieldTemplate::isIdentifier)
				.findFirst()
				.orElse(null);
	}
	
	/**
	 * @param cetCode code of the cet
	 * @return the first three summary fields (excluding the identifier field) for the given cet
	 */
	public Collection<CustomFieldTemplate> getSummaryFields(String cetCode) {
		return customFieldTemplateService.findByAppliesTo(CustomEntityTemplate.getAppliesTo(cetCode))
				.values()
				.stream()
				.filter(CustomFieldTemplate::isSummary)
				.filter(Predicate.not(CustomFieldTemplate::isIdentifier))
				.sorted((field1, field2) -> field1.getGUIFieldPosition() - field2.getGUIFieldPosition())
				.limit(3)
				.collect(Collectors.toList());
	}

}

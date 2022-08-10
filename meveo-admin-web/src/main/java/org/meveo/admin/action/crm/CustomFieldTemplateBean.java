package org.meveo.admin.action.crm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.UpdateMapTypeFieldBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ValidationException;
import org.meveo.admin.util.ResourceBundle;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.commons.utils.StringUtils;
import org.meveo.elresolver.ELException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldMapKeyEnum;
import org.meveo.model.crm.custom.CustomFieldMatrixColumn;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.catalog.impl.CalendarService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.crm.impl.SampleValueHelper;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.service.custom.CustomizedEntity;
import org.meveo.service.custom.CustomizedEntityFilter;
import org.meveo.service.custom.CustomizedEntityService;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.util.EntityCustomizationUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DualListModel;

/**
 * Bean for managing {@link CustomFieldTemplate}.
 *
 * @author clement.bareth
 * @version 6.0.9
 * @since 6.0.0
 */
@Named
@ViewScoped
public class CustomFieldTemplateBean extends UpdateMapTypeFieldBean<CustomFieldTemplate> {

    private static final long serialVersionUID = 9099292371182275568L;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    private CalendarService calendarService;

    @Inject
    private CustomizedEntityService customizedEntityService;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private ResourceBundle resourceMessages;
    
    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;

    @Inject
    private ScriptInstanceService scriptInstanceService;

    private DualListModel<CustomFieldMatrixColumn> childEntityFieldDM;

    private List<CustomFieldTypeEnum> identifierFieldTypes = Arrays.asList(CustomFieldTypeEnum.LONG, CustomFieldTypeEnum.STRING);

    private CustomFieldTypeEnum fieldType;

    private DualListModel<DBStorageType> storagesDM;

    private DualListModel<DBStorageType> cetStorageDM;
    
    private CustomRelationshipTemplate relationshipToCreate = new CustomRelationshipTemplate();

    /**
     * To what entity class CFT should be copied to - a appliesTo value
     */
    private String copyCftTo;

    private String appliesTo;

    /**
	 * Instantiates a new custom field template bean.
	 */
    public CustomFieldTemplateBean() {
        super(CustomFieldTemplate.class);
    }

    /**
	 * Creates a new entity linked an other entity pointed by the applies to query.
	 *
	 * @param appliesTo the applies to query
	 * @return the custom field template created
	 */
    public CustomFieldTemplate newEntity(String appliesTo) {
        storagesDM = new DualListModel<>();
        CustomFieldTemplate customFieldTemplate = super.newEntity();
        entity = customFieldTemplate;
        this.appliesTo = appliesTo;
        return customFieldTemplate;
    }

    @Override
    public CustomFieldTemplate initEntity() {
    	relationshipToCreate = new CustomRelationshipTemplate();
        CustomFieldTemplate customFieldTemplate = super.initEntity();

        if (customFieldTemplate != null) {
            extractMapTypeFieldFromEntity(customFieldTemplate.getListValuesSorted(), "listValues");
        }
        
        entity = customFieldTemplate;
        appliesTo = entity.getAppliesTo();
        storagesDM = null;
        return customFieldTemplate;
    }

    /**
	 * Gets the applies to.
	 *
	 * @return the applies to
	 */
    public String getAppliesTo() {
        return appliesTo;
    }

    /**
	 * Sets the applies to.
	 *
	 * @param appliesTo the new applies to
	 */
    public void setAppliesTo(String appliesTo) {
        this.appliesTo = appliesTo;
    }
    
	@Override
	public void delete(Long id) throws BusinessException {
		var cft = customFieldTemplateService.findById(id);
		customFieldTemplateService.remove(cft, true);
	}

	/**
	 * <p>In case of entity reference, 
	 * gets the available relationships according to the CET pointed by the applies to query and the referenced CET.</p>
	 * Will return null if: 
	 * <ul>
	 * 	<li>Field is not stored in neo4j</li>
	 * 	<li>Field does not applies to a custom entity template</li>
	 *  <li>Field does not refers to a custom entity template</li>
	 *</ul>
	 *  
	 * @see #getAppliesTo()
	 * @see CustomFieldTemplateBean#getEntityClazzCetCode()
	 * @see CustomRelationshipTemplateService#findBySourceOrTarget(String, String)
	 * @return the available relationships
	 */
	public List<CustomRelationshipTemplate> getAvailableRelationships() {
		if(!cetStorageDM.getTarget().contains(DBStorageType.NEO4J))
			return null;
		
		String cetCode = CustomEntityTemplate.getCodeFromAppliesTo(getAppliesTo());
		if(cetCode == null)
			return null;
		
		String targetCode = getEntity().getEntityClazzCetCode();
		if(targetCode == null) 
			return null;
		
		CustomEntityTemplate cet = customEntityTemplateService.findByCode(cetCode);
		List<CustomRelationshipTemplate> relations = customRelationshipTemplateService.findBySourceOrTarget(cetCode, targetCode);
		
		while(cet.getSuperTemplate() != null) {
			List<CustomRelationshipTemplate> crts = customRelationshipTemplateService.findBySourceOrTarget(cet.getSuperTemplate().getCode(), targetCode);
			relations.addAll(crts);
			cet = cet.getSuperTemplate();
		}

		return relations;
	}


    /**
	 * Gets the cet storage DM.
	 *
	 * @return the cet storage DM
	 */
    public DualListModel<DBStorageType> getCetStorageDM() {
		return cetStorageDM;
	}

	/**
	 * Sets the cet storage DM.
	 *
	 * @param cetStorageDM the new cet storage DM
	 */
	public void setCetStorageDM(DualListModel<DBStorageType> cetStorageDM) {
		this.cetStorageDM = cetStorageDM;
	}

	@Override
    @ActionMethod
    public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {

        if (entity.getFieldType() == CustomFieldTypeEnum.LIST) {
            entity.setListValues(new TreeMap<String, String>());
            updateMapTypeFieldInEntity(entity.getListValues(), "listValues");
        }

        CustomFieldTemplate cfDuplicate = customFieldTemplateService.findByCodeAndAppliesTo(entity.getCode(), entity.getAppliesTo());
        if (cfDuplicate != null && cfDuplicate.getId() != null && !cfDuplicate.getId().equals(entity.getId())) {
            messages.error(new BundleKey("messages", "customFieldTemplate.alreadyExists"));
            return null;
        }

        boolean invalid = false;
        if (getEntity().getFieldType()==CustomFieldTypeEnum.STRING) {
            Map<Integer, String> validateSamples = SampleValueHelper.validateStringType(getEntity().getSamples(), getEntity().getStorageType());
            if (!validateSamples.isEmpty()) {
                for (Map.Entry<Integer,String> validateSample : validateSamples.entrySet()) {
                    messages.error(new BundleKey("messages", validateSample.getValue()), validateSample.getKey());
                    invalid = true;
                }
            }
        }

        if (getEntity().getFieldType()==CustomFieldTypeEnum.LONG) {
            Map<Integer, String> validateSamples = SampleValueHelper.validateLongType(getEntity().getSamples(), getEntity().getStorageType());
            if (!validateSamples.isEmpty()) {
                for (Map.Entry<Integer,String> validateSample : validateSamples.entrySet()) {
                    messages.error(new BundleKey("messages", validateSample.getValue()), validateSample.getKey());
                    invalid = true;
                }
            }
        }

        if (getEntity().getFieldType()==CustomFieldTypeEnum.DOUBLE) {
            Map<Integer, String> validateSamples = SampleValueHelper.validateDoubleType(getEntity().getSamples(), getEntity().getStorageType());
            if (!validateSamples.isEmpty()) {
                for (Map.Entry<Integer,String> validateSample: validateSamples.entrySet()) {
                    messages.error(new BundleKey("messages", validateSample.getValue()), validateSample.getKey());
                    invalid = true;
                }
            }
        }

        if (getEntity().getFieldType()==CustomFieldTypeEnum.CHILD_ENTITY) {
            Map<String, CustomFieldTemplate> customFieldTemplates = customFieldTemplateService.findByAppliesTo(CustomEntityTemplate.getAppliesTo(getEntity().getEntityClazzCetCode()));
            Map<Integer, String> validateSamples = SampleValueHelper.validateChildEntityType(customFieldTemplates, getEntity().getSamples(), getEntity().getStorageType());
            if (!validateSamples.isEmpty()) {
                for (Map.Entry<Integer,String> validateSample : validateSamples.entrySet()) {
                    messages.error(new BundleKey("messages", validateSample.getValue()), validateSample.getKey());
                    invalid = true;
                }
            }
        }

        if (invalid) {
            return null;
        }

        // Update childEntityColumns
        if (getEntity().getFieldType() == CustomFieldTypeEnum.CHILD_ENTITY) {
            List<String> cheColumns = new ArrayList<>();
            for (CustomFieldMatrixColumn cheColumn : childEntityFieldDM.getTarget()) {
                cheColumns.add(cheColumn.getCode());
            }
            getEntity().setChildEntityFieldsAsList(cheColumns);
        } else {
            getEntity().setChildEntityFields(null);
        }

        if (entity.getCalendar() != null) {
            entity.setCalendar(calendarService.retrieveIfNotManaged(entity.getCalendar()));
        }

        if (CollectionUtils.isNotEmpty(getEntity().getStoragesNullSafe())) {
            getEntity().getStoragesNullSafe().clear();
            getEntity().getStoragesNullSafe().addAll(storagesDM.getTarget());
        } else {
            getEntity().setStorages(storagesDM.getTarget());
        }

        String message = entity.isTransient() ? "save.successful" : "update.successful";

        try {
            entity = saveOrUpdate(entity);
            messages.info(new BundleKey("messages", message));
            if (killConversation) {
                endConversation();
            }

        } catch (Exception e){
            if (e.getCause() instanceof IllegalArgumentException) {
                messages.error(new BundleKey("messages", "message.ontology.code.error"));
                return null;
            } else {
                messages.error("Entity can't be saved. Please retry.");
                log.error("Can't update entity", e);
            }
        }

        return back();
    }

    @Override
    protected IPersistenceService<CustomFieldTemplate> getPersistenceService() {
        return customFieldTemplateService;
    }

    @Override
    protected String getDefaultSort() {
        return "code";
    }

    /**
	 * Autocomplete method for selecting a class/custom entity template for entity reference type Custom field template.
	 *
	 * @param query Partial value entered
	 * @return A list of matching values
	 */
    public List<String> autocompleteClassNames(String query) {
        List<String> clazzNames = new ArrayList<String>();

        CustomizedEntityFilter filter = new CustomizedEntityFilter();
        filter.setEntityName(query);
        filter.setCustomEntityTemplatesOnly(false);
        filter.setIncludeNonManagedEntities(true);
        filter.setIncludeParentClassesOnly(false);
        
        List<CustomizedEntity> entities = customizedEntityService.getCustomizedEntities(filter);

        for (CustomizedEntity customizedEntity : entities) {
            clazzNames.add(customizedEntity.getClassnameToDisplay());
        }

        // add script classes


        return clazzNames;
    }

    /**
	 * Autocomplete method for selecting a custom entity template for child entity reference type Custom field template.
	 *
	 * @param query Partial value entered
	 * @return A list of matching values
	 */
    public List<String> autocompleteClassNamesCEIOnly(String query) {
        List<String> clazzNames = new ArrayList<String>();

        CustomizedEntityFilter filter = new CustomizedEntityFilter();
        filter.setEntityName(query);
        filter.setCustomEntityTemplatesOnly(true);
        filter.setIncludeNonManagedEntities(false);
        filter.setIncludeParentClassesOnly(false);
        
        List<CustomizedEntity> entities = customizedEntityService.getCustomizedEntities(filter);

        for (CustomizedEntity customizedEntity : entities) {
            clazzNames.add(customizedEntity.getClassnameToDisplay());
        }

        return clazzNames;
    }

    /**
     * Autocomplete method for selecting a class that implement ICustomFieldEntity. Return a human readable class name. Used in conjunction with n
     * 
     * @param query Partial class name to match
     * @return the matching class names
     */
    public List<String> autocompleteClassNamesHuman(String query) {
        List<String> clazzNames = new ArrayList<String>();

        CustomizedEntityFilter filter = new CustomizedEntityFilter();
        filter.setEntityName(query);
        filter.setCustomEntityTemplatesOnly(false);
        filter.setIncludeNonManagedEntities(true);
        filter.setIncludeParentClassesOnly(true);
        
        List<CustomizedEntity> entities = customizedEntityService.getCustomizedEntities(filter);

        for (CustomizedEntity customizedEntity : entities) {
            clazzNames.add(customizedEntity.getClassnameToDisplayHuman());
        }

        return clazzNames;
    }

    /**
	 * Update default values.
	 */
    public void updateDefaultValues() {

        if ((entity.getFieldType() == CustomFieldTypeEnum.STRING || entity.getFieldType() == CustomFieldTypeEnum.SECRET) && entity.getMaxValue() == null) {
            entity.setMaxValue(CustomFieldTemplate.DEFAULT_MAX_LENGTH_STRING);
        }
        if (entity.getFieldType() == CustomFieldTypeEnum.CHILD_ENTITY && entity.getStorageType() != CustomFieldStorageTypeEnum.LIST && entity.getStorageType() != CustomFieldStorageTypeEnum.SINGLE) {
			entity.setStorageType(CustomFieldStorageTypeEnum.LIST);
			entity.setVersionable(false);
        }
        if (entity.getStorageType() == CustomFieldStorageTypeEnum.MAP && entity.getMapKeyType() == null) {
            entity.setMapKeyType(CustomFieldMapKeyEnum.STRING);
        }
        if (entity.getFieldType() == CustomFieldTypeEnum.MULTI_VALUE) {
            entity.setStorageType(CustomFieldStorageTypeEnum.MATRIX);
        }
    }

    /**
	 * Reset child entity fields.
	 */
    public void resetChildEntityFields() {
        childEntityFieldDM = null;
    }

    /**
	 * Gets the child entity field list model.
	 *
	 * @return the child entity field list model
	 */
    public DualListModel<CustomFieldMatrixColumn> getChildEntityFieldListModel() {
        if (childEntityFieldDM == null && CustomFieldTemplate.retrieveCetCode(entity.getEntityClazz()) != null) {

            List<CustomFieldMatrixColumn> childEntityFieldsList = new ArrayList<>();

            Map<String, CustomFieldTemplate> cfts = customFieldTemplateService
                .findByAppliesTo(EntityCustomizationUtils.getAppliesTo(CustomEntityTemplate.class, CustomFieldTemplate.retrieveCetCode(entity.getEntityClazz())));

            for (CustomFieldTemplate cft : cfts.values()) {
                childEntityFieldsList.add(new CustomFieldMatrixColumn(cft.getCode(), cft.getDescription()));
            }

            // Custom field template stores selected fields as a comma separated string of field codes.
            List<CustomFieldMatrixColumn> perksTarget = new ArrayList<CustomFieldMatrixColumn>();
            if (getEntity().getChildEntityFields() != null) {
                for (String fieldCode : getEntity().getChildEntityFieldsAsList()) {
                    CustomFieldTemplate cft = cfts.get(fieldCode);
                    if(cft != null) {
                    	perksTarget.add(new CustomFieldMatrixColumn(cft.getCode(), cft.getDescription()));
                    }
                }
            }
            childEntityFieldsList.removeAll(perksTarget);
            childEntityFieldDM = new DualListModel<CustomFieldMatrixColumn>(childEntityFieldsList, perksTarget);
        }
        return childEntityFieldDM;
    }
    
    /**
	 * Gets the child entity field list.
	 *
	 * @return the child entity field list
	 */
    public List<CustomFieldMatrixColumn> getChildEntityFieldList(){
    	ArrayList<CustomFieldMatrixColumn> arrayList = new ArrayList<>(childEntityFieldDM.getSource());
    	arrayList.addAll(childEntityFieldDM.getTarget());
    	return arrayList;
    }

    /**
	 * Sets the child entity field list model.
	 *
	 * @param childEntityFieldDM the new child entity field list model
	 */
    public void setChildEntityFieldListModel(DualListModel<CustomFieldMatrixColumn> childEntityFieldDM) {
        this.childEntityFieldDM = childEntityFieldDM;
    }

    /**
	 * Validate matrix columns of a custom field template.
	 *
	 * @param cft Custom field template
	 */
    public void validateMatrixColumns(CustomFieldTemplate cft) {

        if (cft.getStorageType() != CustomFieldStorageTypeEnum.MATRIX) {
            return;
        }

        FacesContext fc = FacesContext.getCurrentInstance();
        boolean valid = true;

        if (cft.getMatrixColumns() == null || cft.getMatrixColumns().isEmpty()) {
            FacesMessage msg = new FacesMessage(resourceMessages.getString("customFieldTemplate.matrixColumn.error.atLeastOne"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fc.addMessage(null, msg);
            valid = false;
        } else {
            for (CustomFieldMatrixColumn column : cft.getMatrixColumns()) {
                if (StringUtils.isBlank(column.getCode()) || StringUtils.isBlank(column.getLabel()) || column.getKeyType() == null) {
                    FacesMessage msg = new FacesMessage(resourceMessages.getString("customFieldTemplate.matrixColumn.error.missingFields"));
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    fc.addMessage(null, msg);
                    valid = false;
                    break;
                }
            }
        }

        if (!valid) {

            fc.validationFailed();
            fc.renderResponse();
        }
    }

    /**
	 * Gets the to what entity class CFT should be copied to - a appliesTo value.
	 *
	 * @return the to what entity class CFT should be copied to - a appliesTo value
	 */
    public String getCopyCftTo() {
        return copyCftTo;
    }

    /**
	 * Sets the to what entity class CFT should be copied to - a appliesTo value.
	 *
	 * @param copyCftTo the new to what entity class CFT should be copied to - a appliesTo value
	 */
    public void setCopyCftTo(String copyCftTo) {
        this.copyCftTo = copyCftTo;
    }

    /**
	 * Copy and associate custom field template with another entity class.
	 *
	 * @throws BusinessException the business exception
	 */
    @ActionMethod
    public void copyCFT() throws BusinessException {

        if (copyCftTo == null) {
            throw new ValidationException("Not specified what class to copy CFT to", "customFieldTemplate.copyCFT.targetNotSpecified");
        }

        entity = customFieldTemplateService.refreshOrRetrieve(entity);
        customFieldTemplateService.copyCustomFieldTemplate(entity, copyCftTo);

        messages.info(new BundleKey("messages", "customFieldTemplate.copyCFT.ok"));
    }

    /**
	 * Gets the identifier field types.
	 *
	 * @return the identifier field types
	 */
    public List<CustomFieldTypeEnum> getIdentifierFieldTypes() {
        return identifierFieldTypes;
    }

    /**
	 * Gets the field type.
	 *
	 * @return the field type
	 */
    public CustomFieldTypeEnum getFieldType() {
        entity.setFieldType(fieldType);
        return fieldType;
    }

    /**
	 * Sets the field type.
	 *
	 * @param fieldType the new field type
	 */
    public void setFieldType(CustomFieldTypeEnum fieldType) {
        entity.setFieldType(fieldType);
        this.fieldType = fieldType;
    }

    /**
	 * Reset field type.
	 */
    public void resetFieldType() {
        this.fieldType = null;
    }

    /**
	 * The possible storages of a CFT are the available storages of the CET / CRT <br>
	 * If CFT is being created, the storage list has by default all the storages of its CET or CRT <br>
	 * If the CFT is being edited, the target list is filled with persisted data, and the remaining available storages are put in the source list.
	 *
	 * @return The dual list for storages of the CFT
	 */
    public DualListModel<DBStorageType> getStoragesDM() {
    	if(storagesDM == null) {
    		List<DBStorageType> perksSource = new ArrayList<>();
            List<DBStorageType> perksTarget = new ArrayList<>();

            // If the CFT has no id, then it's being created, otherwise it's being edited
        	if(getEntity().getId() == null) {
        		perksTarget.addAll(cetStorageDM.getTarget());
        	} else {
        		perksSource.addAll(cetStorageDM.getTarget());
	            if (getEntity().getStoragesNullSafe() != null) {
	                perksTarget.addAll(getEntity().getStoragesNullSafe());		// Persistent data
	                perksSource.removeAll(getEntity().getStoragesNullSafe());	// Display remaining available storages
	            }
        	}

            storagesDM = new DualListModel<DBStorageType>(perksSource, perksTarget);
    	}

        return storagesDM;
    }

    /**
	 * Sets the storages DM.
	 *
	 * @param storagesDM the new storages DM
	 */
    public void setStoragesDM(DualListModel<DBStorageType> storagesDM) {
        this.storagesDM = storagesDM;
    }

    /**
	 * Gets the storage types list.
	 *
	 * @return the storage types list
	 */
    public List<DBStorageType> getStorageTypesList(){
        ArrayList<DBStorageType> arrayList = new ArrayList<>(storagesDM.getSource());
        arrayList.addAll(storagesDM.getTarget());
        return arrayList;
    }

	/**
	 * Adds the content types.
	 */
	public void addContentTypes() {
		if (!StringUtils.isBlank(entity.getContentType())) {
			entity.addContentType(entity.getContentType());
		}
	}

	/**
	 * Adds the file extensions.
	 */
	public void addFileExtensions() {
		if (!StringUtils.isBlank(entity.getFileExtension())) {
			entity.addFileExtension(entity.getFileExtension());
		}
	}

	/**
	 * Clear content type.
	 */
	public void clearContentType() {
		entity.setContentType(null);
	}

	/**
	 * Clear file extension.
	 */
	public void clearFileExtension() {
		entity.setFileExtension(null);
	}

	/**
	 * Reinit content type.
	 *
	 * @return the string
	 */
	public String reinitContentType() {
		entity.setContentType(null);

        return null;
    }

	/**
	 * Reinit file extension.
	 *
	 * @return the string
	 */
	public String reinitFileExtension() {
		entity.setFileExtension(null);

        return null;
    }

    /**
	 * On change available storages.
	 */
    public void onChangeAvailableStorages() {
        if (CollectionUtils.isNotEmpty(getEntity().getStoragesNullSafe())) {
            getEntity().getStoragesNullSafe().clear();
            getEntity().getStoragesNullSafe().addAll(storagesDM.getTarget());
        } else {
            getEntity().setStorages(storagesDM.getTarget());
        }
    }

    /**
	 * Gets the cet id.
	 *
	 * @param entityClazz the entity clazz
	 * @return the cet id
	 */
    public Long getCetId(String entityClazz) {
        if (entityClazz.startsWith(CustomEntityTemplate.class.getName())) {
            CustomEntityTemplate cet = customEntityTemplateService.findByCode(CustomFieldTemplate.retrieveCetCode(entityClazz));
            if(cet == null) {
            	log.error("Can't rerieve cet for {}", entityClazz);
            	return null;
            }
            
            return cet.getId();
        }
        
        return null;
    }

	public CustomRelationshipTemplate getRelationshipToCreate() {
		return relationshipToCreate;
	}
	
	public void createRelationsip() {
		String sourceCetCode = CustomEntityTemplate.getCodeFromAppliesTo(getAppliesTo());
		CustomEntityTemplate source = customEntityTemplateService.findByCode(sourceCetCode);
		CustomEntityTemplate target = customEntityTemplateService.findByCode(entity.getEntityClazzCetCode());
		
		relationshipToCreate.setStartEntity(source);
		relationshipToCreate.setEndEntity(target);
		relationshipToCreate.setAvailableStorages(List.of(DBStorageType.NEO4J));
		
		try {
			customRelationshipTemplateService.create(relationshipToCreate);
			entity.setRelationship(relationshipToCreate);
		} catch (BusinessException e) {
			log.error("Can't create relationship", e);
			
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Can't create relationship", e.getLocalizedMessage());
			PrimeFaces.current().dialog().showMessageDynamic(message);
		}
	}

	public boolean showAuditedField() {
		return getEntity().getStoragesNullSafe().contains(DBStorageType.SQL);
	}
}
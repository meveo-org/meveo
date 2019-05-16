package org.meveo.admin.action.crm;

import java.util.*;

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
import org.meveo.model.persistence.DBStorageType;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.catalog.impl.CalendarService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomizedEntity;
import org.meveo.service.custom.CustomizedEntityService;
import org.meveo.util.EntityCustomizationUtils;
import org.primefaces.model.DualListModel;

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

    private DualListModel<CustomFieldMatrixColumn> childEntityFieldDM;

    private List<CustomFieldTypeEnum> identifierFieldTypes = Arrays.asList(CustomFieldTypeEnum.LONG, CustomFieldTypeEnum.STRING);

    private CustomFieldTypeEnum fieldType;

    private DualListModel<DBStorageType> storagesDM;

    /**
     * To what entity class CFT should be copied to - a appliesTo value
     */
    private String copyCftTo;

    private String appliesTo;

    public CustomFieldTemplateBean() {
        super(CustomFieldTemplate.class);
    }

    public CustomFieldTemplate newEntity(String appliesTo) {
        CustomFieldTemplate customFieldTemplate = super.newEntity();
        entity = customFieldTemplate;
        storagesDM = null;
        this.appliesTo = appliesTo;
        return customFieldTemplate;
    }

    @Override
    public CustomFieldTemplate initEntity() {
        CustomFieldTemplate customFieldTemplate = super.initEntity();

        if (customFieldTemplate != null) {
            extractMapTypeFieldFromEntity(customFieldTemplate.getListValuesSorted(), "listValues");
        }
        
        entity = customFieldTemplate;
        appliesTo = entity.getAppliesTo();
        storagesDM = null;
        return customFieldTemplate;
    }

    public String getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = appliesTo;
    }

    @Override
    @ActionMethod
    public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {

        if (entity.getFieldType() == CustomFieldTypeEnum.LIST) {
            entity.setListValues(new TreeMap<String, String>());
            updateMapTypeFieldInEntity(entity.getListValues(), "listValues");
        }

        CustomFieldTemplate cfDuplicate = customFieldTemplateService.findByCodeAndAppliesTo(entity.getCode(), entity.getAppliesTo());
        if (cfDuplicate != null && !cfDuplicate.getId().equals(entity.getId())) {
            messages.error(new BundleKey("messages", "customFieldTemplate.alreadyExists"));
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

        if (CollectionUtils.isNotEmpty(getEntity().getStorages())) {
            getEntity().getStorages().clear();
            getEntity().getStorages().addAll(storagesDM.getTarget());
        } else {
            getEntity().setStorages(storagesDM.getTarget());
        }
        return super.saveOrUpdate(killConversation);
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
     * Autocomplete method for selecting a class/custom entity template for entity reference type Custom field template
     * 
     * @param query Partial value entered
     * @return A list of matching values
     */
    public List<String> autocompleteClassNames(String query) {
        List<String> clazzNames = new ArrayList<String>();

        List<CustomizedEntity> entities = customizedEntityService.getCustomizedEntities(query, false, true, false, null, null);

        for (CustomizedEntity customizedEntity : entities) {
            clazzNames.add(customizedEntity.getClassnameToDisplay());
        }

        return clazzNames;
    }

    /**
     * Autocomplete method for selecting a custom entity template for child entity reference type Custom field template
     * 
     * @param query Partial value entered
     * @return A list of matching values
     */
    public List<String> autocompleteClassNamesCEIOnly(String query) {
        List<String> clazzNames = new ArrayList<String>();

        List<CustomizedEntity> entities = customizedEntityService.getCustomizedEntities(query, true, false, false, null, null);

        for (CustomizedEntity customizedEntity : entities) {
            clazzNames.add(customizedEntity.getClassnameToDisplay());
        }

        return clazzNames;
    }

    /**
     * Autocomplete method for selecting a class that implement ICustomFieldEntity. Return a human readable class name. Used in conjunction with n
     * 
     * @param query Partial class name to match
     * @return
     */
    public List<String> autocompleteClassNamesHuman(String query) {
        List<String> clazzNames = new ArrayList<String>();

        List<CustomizedEntity> entities = customizedEntityService.getCustomizedEntities(query, false, true, true, null, null);

        for (CustomizedEntity customizedEntity : entities) {
            clazzNames.add(customizedEntity.getClassnameToDisplayHuman());
        }

        return clazzNames;
    }

    public void updateDefaultValues() {

        if (entity.getFieldType() == CustomFieldTypeEnum.STRING && entity.getMaxValue() == null) {
            entity.setMaxValue(CustomFieldTemplate.DEFAULT_MAX_LENGTH_STRING);
        }
        if (entity.getFieldType() == CustomFieldTypeEnum.CHILD_ENTITY) {
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

    public void resetChildEntityFields() {
        childEntityFieldDM = null;
    }

    public DualListModel<CustomFieldMatrixColumn> getChildEntityFieldListModel() {
        if (childEntityFieldDM == null && CustomFieldTemplate.retrieveCetCode(entity.getEntityClazz()) != null) {

            List<CustomFieldMatrixColumn> childEntityFieldsList = new ArrayList<>();
            childEntityFieldsList.add(new CustomFieldMatrixColumn("code", "Code"));
            childEntityFieldsList.add(new CustomFieldMatrixColumn("description", "Description"));

            Map<String, CustomFieldTemplate> cfts = customFieldTemplateService
                .findByAppliesTo(EntityCustomizationUtils.getAppliesTo(CustomEntityTemplate.class, CustomFieldTemplate.retrieveCetCode(entity.getEntityClazz())));

            for (CustomFieldTemplate cft : cfts.values()) {
                childEntityFieldsList.add(new CustomFieldMatrixColumn(cft.getCode(), cft.getDescription()));
            }

            // Custom field template stores selected fields as a comma separated string of field codes.
            List<CustomFieldMatrixColumn> perksTarget = new ArrayList<CustomFieldMatrixColumn>();
            if (getEntity().getChildEntityFields() != null) {
                for (String fieldCode : getEntity().getChildEntityFieldsAsList()) {
                    if (fieldCode.equals("code")) {
                        perksTarget.add(new CustomFieldMatrixColumn("code", "Code"));
                    } else if (fieldCode.equals("description")) {
                        perksTarget.add(new CustomFieldMatrixColumn("description", "Description"));
                    } else if (cfts.containsKey(fieldCode)) {
                        CustomFieldTemplate cft = cfts.get(fieldCode);
                        perksTarget.add(new CustomFieldMatrixColumn(cft.getCode(), cft.getDescription()));
                    }
                }
            }
            childEntityFieldsList.removeAll(perksTarget);
            childEntityFieldDM = new DualListModel<CustomFieldMatrixColumn>(childEntityFieldsList, perksTarget);
        }
        return childEntityFieldDM;
    }
    
    public List<CustomFieldMatrixColumn> getChildEntityFieldList(){
    	ArrayList<CustomFieldMatrixColumn> arrayList = new ArrayList<>(childEntityFieldDM.getSource());
    	arrayList.addAll(childEntityFieldDM.getTarget());
    	return arrayList;
    }

    public void setChildEntityFieldListModel(DualListModel<CustomFieldMatrixColumn> childEntityFieldDM) {
        this.childEntityFieldDM = childEntityFieldDM;
    }

    /**
     * Validate matrix columns of a custom field template
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

    public String getCopyCftTo() {
        return copyCftTo;
    }

    public void setCopyCftTo(String copyCftTo) {
        this.copyCftTo = copyCftTo;
    }

    /**
     * Copy and associate custom field template with another entity class
     * 
     * @throws org.meveo.admin.exception.BusinessException
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

    public List<CustomFieldTypeEnum> getIdentifierFieldTypes() {
        return identifierFieldTypes;
    }

    public CustomFieldTypeEnum getFieldType() {
        entity.setFieldType(fieldType);
        return fieldType;
    }

    public void setFieldType(CustomFieldTypeEnum fieldType) {
        entity.setFieldType(fieldType);
        this.fieldType = fieldType;
    }

    public void resetFieldType() {
        this.fieldType = null;
    }

    public DualListModel<DBStorageType> getStoragesDM() {
        if (storagesDM == null) {
            List<DBStorageType> perksSource = new ArrayList<>();
            if (!StringUtils.isBlank(appliesTo)) {
                String cetCode = appliesTo;
                if (appliesTo.startsWith(CustomEntityTemplate.CFT_PREFIX)) {
                    cetCode = cetCode.substring(3);
                }
                CustomEntityTemplate customEntityTemplate = customEntityTemplateService.findByCode(cetCode);
                if (customEntityTemplate != null && CollectionUtils.isNotEmpty(customEntityTemplate.getAvailableStorages())) {
                    perksSource.addAll(customEntityTemplate.getAvailableStorages());
                }
            }
            List<DBStorageType> perksTarget = new ArrayList<DBStorageType>();
            if (getEntity().getStorages() != null) {
                perksTarget.addAll(getEntity().getStorages());
            }
            perksSource.removeAll(perksTarget);
            storagesDM = new DualListModel<DBStorageType>(perksSource, perksTarget);

        }
        return storagesDM;
    }

    public void setStoragesDM(DualListModel<DBStorageType> storagesDM) {
        this.storagesDM = storagesDM;
    }

    public List<DBStorageType> getStorageTypesList(){
        ArrayList<DBStorageType> arrayList = new ArrayList<>(storagesDM.getSource());
        arrayList.addAll(storagesDM.getTarget());
        return arrayList;
    }
}
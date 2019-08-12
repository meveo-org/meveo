package org.meveo.admin.action.admin.custom;

import java.io.File;
import java.util.ArrayList;
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
import org.meveo.admin.action.crm.CustomFieldTemplateBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.elresolver.ELException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValue;
import org.meveo.model.crm.custom.CustomFieldValueHolder;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomizedEntity;
import org.meveo.service.custom.CustomizedEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@ViewScoped
public class CustomEntityInstanceBean extends CustomFieldBean<CustomEntityInstance> {

    Logger log = LoggerFactory.getLogger(CustomEntityInstanceBean.class);

    private static final long serialVersionUID = -459772193950603406L;

    private String customEntityTemplateCode;

    private CustomEntityTemplate customEntityTemplate;

    public CustomEntityInstanceBean() {
        super(CustomEntityInstance.class);
    }
    
    @Inject
    private CustomizedEntityService customizedEntityService;

    @Inject
    private CustomEntityInstanceService customEntityInstanceService;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private CustomFieldInstanceService customFieldInstanceService;
    
    @Override
    protected IPersistenceService<CustomEntityInstance> getPersistenceService() {
        return customEntityInstanceService;
    }

    public void setCustomEntityTemplateCode(String customEntityTemplateCode) {
        this.customEntityTemplateCode = customEntityTemplateCode;
    }

    public String getCustomEntityTemplateCode() {
        return customEntityTemplateCode;
    }

    @Override
    public CustomEntityInstance initEntity() {

        CustomEntityInstance initResult = super.initEntity();

        // If it is a new entity and does not have yet the CET code set yet, set it from request parameter and initialize custom fields
        if (initResult.getCetCode() == null && customEntityTemplateCode != null) {
            initResult.setCetCode(customEntityTemplateCode);
        }
        
        if (customEntityTemplateCode==null && !initResult.isTransient()){
            customEntityTemplateCode = initResult.getCetCode();
        }
        
        return initResult;
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
        String listViewName =  super.saveOrUpdate(killConversation);
        ceiSameCode = customEntityInstanceService.findByCodeByCet(entity.getCetCode(), entity.getCode());
        Map<String, Object> fieldValues = new HashMap<>();
        Map<String, CustomFieldTemplate> customFieldTemplates = customFieldTemplateService.findByAppliesTo(ceiSameCode);
        for (Iterator<CustomFieldTemplate> iterator = customFieldTemplates.values().iterator(); iterator.hasNext(); ) {
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
        for(String fileToDelete : customFieldDataEntryBean.getFilesToDeleteOnExit()) {
        	File file = new File(fileToDelete);
        	if(file.exists()) {
        		file.delete();
        	}
        }

        return listViewName;
    }
    
    public void updateInBaseBean() {
    	Map<String, List<CustomFieldValue>> newValuesByCode = new HashMap<>();

        CustomFieldValueHolder entityFieldsValues = customFieldDataEntryBean.getFieldValueHolderByUUID(entity.getUuid());
        GroupedCustomField groupedCustomFields = customFieldDataEntryBean.groupedFieldTemplates.get(entity.getUuid());
//        if (groupedCustomFields != null) {
//            for (CustomFieldTemplate cft : groupedCustomFields.getFields()) {
//
//                // Do not update existing CF value if it is not updatable
//                if (!isNewEntity && !cft.isAllowEdit()) {
//
//                    if (entity != null && entity.getCfValues() != null) {
//                        List<CustomFieldValue> previousCfValues = entity.getCfValues().getValuesByCode().get(cft.getCode());
//                        if (previousCfValues != null && !previousCfValues.isEmpty()) {
//                            newValuesByCode.put(cft.getCode(), previousCfValues);
//                        }
//                    }
//                    continue;
//                }
//
//                for (CustomFieldValue cfValue : entityFieldsValues.getValues(cft)) {
//
//
//                    // if (duplicateCFI) {
//                    // if (removedOriginalCFI) {
//                    // List<CustomFieldInstance> cfisToBeRemove = customFieldInstanceService.getCustomFieldInstances(entity, cfValue.getCode());
//                    // if (cfisToBeRemove != null) {
//                    // for (CustomFieldInstance cfiToBeRemove : cfisToBeRemove) {
//                    // customFieldInstanceService.remove(cfiToBeRemove);
//                    // }
//                    // }
//                    // }
//                    //
//                    // customFieldInstanceService.detach(cfValue);
//                    // cfValue.setId(null);
//                    // cfValue.setAppliesToEntity(entity.getUuid());
//                    // }
//
//                    // Not saving empty values unless template has a default value or is versionable (to prevent that for SINGLE type CFT with a default value, value is
//                    // instantiates automatically)
//                    // Also don't save if CFT does not apply in a given entity lifecycle or because cft.applicableOnEL evaluates to false
//                    if ((cfValue.isValueEmptyForGui() && (cft.getDefaultValue() == null || cft.getStorageType() != CustomFieldStorageTypeEnum.SINGLE) && !cft.isVersionable())
//                            || ((isNewEntity && cft.isHideOnNew())
//                                    || (entity != null && !MeveoValueExpressionWrapper.evaluateToBooleanOneVariable(cft.getApplicableOnEl(), "entity", entity)))) {
//                        log.trace("Will ommit from saving cfi {}", cfValue);
//
//                        // Existing value update
//                    } else {
//                        serializeFromGUI(cfValue, cft);
//
//                        if (!newValuesByCode.containsKey(cft.getCode())) {
//                            newValuesByCode.put(cft.getCode(), new ArrayList<>());
//                        }
//                        newValuesByCode.get(cft.getCode()).add(cfValue);
//
//                        saveChildEntities(entity, cfValue, cft);
//                    }
//                }
//            }
//        }
//        // Update entity custom values field
//
//        if (entity != null) {
//            if (newValuesByCode.isEmpty()) {
//                entity.clearCfValues();
//            } else {
//                entity.getCfValuesNullSafe().setValuesByCode(newValuesByCode);
//            }
//        }

//        return newValuesByCode
    }

    @Override
    public String getEditViewName() {
        return "customEntity";
    }

    @Override
    public String getListViewName() {
        return "customEntities";
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
    
    public List<String> autocompleteClassNamesHuman(String input){
        return customizedEntityService.getCustomizedEntities(input, true, true, true, "code", "ASC", false)
        		.stream()
        		.map(CustomizedEntity::getEntityCode)
        		.collect(Collectors.toList());
    }
}
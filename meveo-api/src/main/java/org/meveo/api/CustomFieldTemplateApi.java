package org.meveo.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.CustomFieldMatrixColumnDto;
import org.meveo.api.dto.CustomFieldTemplateDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.model.catalog.Calendar;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldMapKeyEnum;
import org.meveo.model.crm.custom.CustomFieldMatrixColumn;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.catalog.impl.CalendarService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.crm.impl.SampleValueHelper;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.service.custom.CustomizedEntity;
import org.meveo.service.custom.CustomizedEntityFilter;
import org.meveo.service.custom.CustomizedEntityService;
import org.meveo.util.EntityCustomizationUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.8.0
 **/
@Stateless
public class CustomFieldTemplateApi extends BaseCrudApi<CustomFieldTemplate, CustomFieldTemplateDto> {
	
	@Inject
    private CalendarService calendarService;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    private CustomizedEntityService customizedEntityService;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;
    
    @Inject
    private CustomFieldsCacheContainerProvider cache;
    
    private String displayFormat;

    /**
     * Super base API service
     * 
     * @param jpaClass
     * @param dtoClass
     */
    public CustomFieldTemplateApi() {
		super(CustomFieldTemplate.class, CustomFieldTemplateDto.class);
	}
    
    public List<CustomFieldTemplateDto> readCfts(File directory) {
    	List<CustomFieldTemplateDto> cftDtos = new ArrayList<>();
    	
    	for (File cetOrCrtDir : directory.listFiles()) {
    		if(!cetOrCrtDir.isDirectory()) {
    			continue;
    		}
    		
    		for (File cftFile : cetOrCrtDir.listFiles()) {
    			try {
					cftDtos.add(JacksonUtil.read(cftFile, CustomFieldTemplateDto.class));
				} catch (IOException e) {
					log.error("Failed to read cft file", e);
					return null;
				}
    		}
    	}
    	
    	return cftDtos;
    }

    /**
	 * Creates a new CustomFieldTemplate using the given data.
	 *
	 * @param postData  the post data
	 * @param appliesTo the applies to query
	 * @throws MeveoApiException if api exception occurs
	 * @throws BusinessException if business exception occurs
	 */
    public void create(CustomFieldTemplateDto postData, String appliesTo) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(postData.getDescription())) {
            missingParameters.add("description");
        }
        if (appliesTo == null && StringUtils.isBlank(postData.getAccountLevel()) && StringUtils.isBlank(postData.getAppliesTo())) {
            missingParameters.add("appliesTo");
        }
        if (postData.getFieldType() == null) {
            missingParameters.add("fieldType");
        }
        if (postData.getStorageType() == null) {
            missingParameters.add("storageType");
        }
        if (postData.getStorageType() == CustomFieldStorageTypeEnum.MATRIX && (postData.getMatrixColumns() == null || postData.getMatrixColumns().isEmpty())) {
            missingParameters.add("matrixColumns");
            
        if(postData.getFieldType() == CustomFieldTypeEnum.ENTITY && postData.getStorages().contains(DBStorageType.NEO4J) && postData.getRelationshipName() == null){
        	 missingParameters.add("relationshipName");
        }
        
        } else if (postData.getStorageType() == CustomFieldStorageTypeEnum.MATRIX) {
            for (CustomFieldMatrixColumnDto columnDto : postData.getMatrixColumns()) {
                if (StringUtils.isBlank(columnDto.getCode())) {
                    missingParameters.add("matrixColumns/code");
                }
                if (StringUtils.isBlank(columnDto.getLabel())) {
                    missingParameters.add("matrixColumns/label");
                }
                if (columnDto.getKeyType() == null) {
                    missingParameters.add("matrixColumns/keyType");
                }
            }
        }

        handleMissingParameters();

        if (appliesTo != null) {
            postData.setAppliesTo(appliesTo);

        } else {
            // Support for old API
            if (postData.getAppliesTo() == null && postData.getAccountLevel() != null) {
                appliesTo = postData.getAccountLevel();
            } else {
                appliesTo = postData.getAppliesTo();
            }
        }

        if (!checkAppliesToExisted(appliesTo)) {
            throw new InvalidParameterException("appliesTo", appliesTo);
        }

        if (customFieldTemplateService.exists(postData.getCode(), appliesTo)) {
            throw new EntityAlreadyExistsException(CustomFieldTemplate.class, postData.getCode());
        }

        CustomFieldTemplate cft = fromDTO(postData, appliesTo, null);
        
        validateSamples(cft);
        customFieldTemplateService.create(cft);

    }

    private boolean checkAppliesToExisted (String appliesTo) {
    	if(appliesTo.startsWith(CustomEntityTemplate.CFT_PREFIX)) {
    		return customEntityTemplateService.findByCode(CustomEntityTemplate.getCodeFromAppliesTo(appliesTo)) != null;
    	} else if (appliesTo.startsWith(CustomRelationshipTemplate.CRT_PREFIX)) {
    		return customRelationshipTemplateService.findByCode(CustomRelationshipTemplate.getCodeFromAppliesTo(appliesTo)) != null;
    	} else {
            CustomizedEntityFilter filter = new CustomizedEntityFilter();
            filter.setCustomEntityTemplatesOnly(false);
            filter.setIncludeNonManagedEntities(true);
            filter.setIncludeParentClassesOnly(true);
            
            List<CustomizedEntity> entities = customizedEntityService.getCustomizedEntities(filter);
            for (CustomizedEntity customizedEntity : entities) {
            	String entityApliesTo = EntityCustomizationUtils.getAppliesTo(customizedEntity.getEntityClass(), customizedEntity.getEntityCode());
                if (entityApliesTo.equals(appliesTo)) {
                	return true;
                }
            }
    	}
    	
        return false;
    }

    /**
	 * Update the corresponding custom field template.
	 *
	 * @param postData  the post data
	 * @param appliesTo the applies to query
	 * @throws MeveoApiException if api exception occurs
	 * @throws BusinessException if business exception occurs
	 */
    public void update(CustomFieldTemplateDto postData, String appliesTo) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        if (appliesTo == null && StringUtils.isBlank(postData.getAccountLevel()) && StringUtils.isBlank(postData.getAppliesTo())) {
            missingParameters.add("appliesTo");
        }
        
        if(postData.getFieldType() == CustomFieldTypeEnum.ENTITY && postData.getStorages() != null && postData.getStorages().contains(DBStorageType.NEO4J) && postData.getRelationshipName() == null){
       	 	missingParameters.add("relationshipName");
        }

        if (postData.getMatrixColumns() != null) {
            for (CustomFieldMatrixColumnDto columnDto : postData.getMatrixColumns()) {
                if (StringUtils.isBlank(columnDto.getCode())) {
                    missingParameters.add("matrixColumns/code");
                }
                if (StringUtils.isBlank(columnDto.getLabel())) {
                    missingParameters.add("matrixColumns/label");
                }
                if (columnDto.getKeyType() == null) {
                    missingParameters.add("matrixColumns/keyType");
                }
            }
        }

        handleMissingParameters();

        if (appliesTo != null) {
            postData.setAppliesTo(appliesTo);

        } else {
            // Support for old API
            if (postData.getAppliesTo() == null && postData.getAccountLevel() != null) {
                appliesTo = postData.getAccountLevel();
            } else {
                appliesTo = postData.getAppliesTo();
            }
        }

        if (!checkAppliesToExisted(appliesTo)) {
            throw new InvalidParameterException("appliesTo", appliesTo);
        }

        CustomFieldTemplate cft = customFieldTemplateService.findByCodeAndAppliesToNoCache(postData.getCode(), appliesTo);
        if (cft == null) {
            throw new EntityDoesNotExistsException(CustomFieldTemplate.class, postData.getCode());
        }

        cft = fromDTO(postData, appliesTo, cft);

        validateSamples(cft);

        customFieldTemplateService.update(cft);

    }

    /**
	 * Removes the corresponding CustomFieldTemplate.
	 *
	 * @param code      the code of the field
	 * @param appliesTo the applies to query
	 * @throws MeveoApiException if api exception occurs
	 * @throws BusinessException if business exception occurs
	 */
    public void remove(String code, String appliesTo) throws MeveoApiException, BusinessException {
        if (StringUtils.isBlank(code)) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(appliesTo)) {
            missingParameters.add("appliesTo");
        }

        handleMissingParameters();

        if (!checkAppliesToExisted(appliesTo)) {
        	return;
        }

        CustomFieldTemplate cft = customFieldTemplateService.findByCodeAndAppliesToNoCache(code, appliesTo);
        if (cft != null) {
            customFieldTemplateService.remove(cft.getId());
        }
    }

    /**
     * Find Custom Field Template by its code and appliesTo attributes.
     * 
     * @param code Custom Field Template code
     * @param appliesTo Applies to
     * @return DTO
     * @throws EntityDoesNotExistsException Custom Field Template was not found
     * @throws InvalidParameterException AppliesTo value is incorrect
     * @throws MissingParameterException A parameter, necessary to find an Custom Field Template, was not provided
     */
    public CustomFieldTemplateDto find(String code, String appliesTo) throws EntityDoesNotExistsException, MissingParameterException, InvalidParameterException {
        if (StringUtils.isBlank(code)) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(appliesTo)) {
            missingParameters.add("appliesTo");
        }

        handleMissingParameters();

        if (!checkAppliesToExisted(appliesTo)) {
            throw new InvalidParameterException("appliesTo", appliesTo);
        }

        CustomFieldTemplate cft = customFieldTemplateService.findByCodeAndAppliesToNoCache(code, appliesTo);

        if (cft == null) {
            throw new EntityDoesNotExistsException(CustomFieldTemplate.class, code + "/" + appliesTo);
        }
        return new CustomFieldTemplateDto(cft);
    }
    
    /**
     * Same as find method, only ignore EntityDoesNotExistException exception and return Null instead.
     * 
     * @param code Custom Field Template code
     * @param appliesTo Applies to
     * @return DTO or Null if not found
     * @throws InvalidParameterException AppliesTo value is incorrect
     * @throws MissingParameterException A parameter, necessary to find an Custom Field Template, was not provided
     */
    public CustomFieldTemplateDto findIgnoreNotFound(String code, String appliesTo) throws MissingParameterException, InvalidParameterException {
        try {
            return find(code, appliesTo);
        } catch (EntityDoesNotExistsException e) {
            return null;
        }
    }

    /**
	 * Same a {@link #createOrUpdate(CustomFieldTemplateDto, String)} but in a new transaction.
	 *
	 * @param postData  the post data
	 * @param appliesTo the applies to
	 * @throws MeveoApiException if api exception occurs
	 * @throws BusinessException if business exception occurs
	 */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void createOrUpdateInNewTransaction(CustomFieldTemplateDto postData, String appliesTo) throws MeveoApiException, BusinessException {
    	createOrUpdate(postData, appliesTo);
    }
    
    /**
	 * Update the corresponding custom field template if exists, or create it otherwise.
	 *
	 * @param postData  the post data
	 * @param appliesTo the applies to
	 * @see #create(CustomFieldTemplateDto, String)
	 * @see #update(CustomFieldTemplateDto, String)
	 * @throws MeveoApiException if api exception occurs
	 * @throws BusinessException if business exception occurs
	 */
    public void createOrUpdate(CustomFieldTemplateDto postData, String appliesTo) throws MeveoApiException, BusinessException {
        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }

        if (appliesTo == null && StringUtils.isBlank(postData.getAccountLevel()) && StringUtils.isBlank(postData.getAppliesTo())) {
            missingParameters.add("appliesTo");
        }

        handleMissingParameters();

        if (appliesTo != null) {
            postData.setAppliesTo(appliesTo);

        } else {
            // Support for old API
            if (postData.getAppliesTo() == null && postData.getAccountLevel() != null) {
                appliesTo = postData.getAccountLevel();
            } else {
                appliesTo = postData.getAppliesTo();
            }
        }

        if (!customFieldTemplateService.exists(postData.getCode(), appliesTo)) {
            create(postData, appliesTo);
        } else {
            update(postData, appliesTo);
        }
    }
    
    /**
	 * Gets the display format.
	 *
	 * @return the display format
	 */
    public String getDisplayFormat() {
        return displayFormat;
    }

    /**
	 * Sets the display format.
	 *
	 * @param displayFormat the new display format
	 */
    public void setDisplayFormat(String displayFormat) {
        this.displayFormat = displayFormat;
    }

    protected CustomFieldTemplate fromDTO(CustomFieldTemplateDto dto, String appliesTo, CustomFieldTemplate cftToUpdate) throws InvalidParameterException {

        // Set default values
        if (dto.getFieldType() == CustomFieldTypeEnum.STRING && dto.getMaxValue() == null) {
            dto.setMaxValue(CustomFieldTemplate.DEFAULT_MAX_LENGTH_STRING);
        }

        CustomFieldTemplate cft = cftToUpdate;
        if (cftToUpdate == null) {
            cft = new CustomFieldTemplate();
            cft.setCode(dto.getCode());
            cft.setFieldType(dto.getFieldType());
            cft.setStorageType(dto.getStorageType());
            if (appliesTo == null) {

                // Support for old API
                if (dto.getAccountLevel() != null) {
                    appliesTo = dto.getAccountLevel();
                } else {
                    appliesTo = dto.getAppliesTo();
                }
            }
            cft.setAppliesTo(appliesTo);
        }
        
        cft.setInDraft(dto.isInDraft());

        if (dto.getDescription() != null) {
            cft.setDescription(dto.getDescription());
        }
        
        if(dto.getRelationship() != null) {
        	CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(dto.getRelationship());
			cft.setRelationship(crt);

        } else if(dto.getRelationshipName() != null && dto.getEntityClazzCetCode() != null) {
            // Old api support - find relationship that has same source / target than entity_clazz and same name, or create one
        	String cetCode = CustomEntityTemplate.getCodeFromAppliesTo(appliesTo);
        	CustomRelationshipTemplate crt;
        	if(cetCode != null) {
		        try {
					crt = findOrCreateRelationship(dto.getRelationshipName(), cetCode, dto.getEntityClazzCetCode());
				} catch (BusinessException e) {
					throw new RuntimeException(e);
				}
		        
				cft.setRelationship(crt);
        	}
        }
        
        if (StringUtils.isNotBlank(dto.getEntityClazz())) {
        	if (customEntityTemplateService.findByCode(dto.getEntityClazzCetCode()) == null) {
        		try {
        			Class.forName(dto.getEntityClazz());
        		} catch (ClassNotFoundException e) {
        			throw new IllegalArgumentException("Entity " + dto.getEntityClazz() + " does not exists");
        		}
        	}
        }
        
        if(dto.getRelationshipName() != null && dto.getFieldType() == CustomFieldTypeEnum.BINARY) {
        	cft.setRelationshipName(dto.getRelationshipName());
        }

        if (dto.getDefaultValue() != null) {
            cft.setDefaultValue(dto.getDefaultValue());
        } else if (StringUtils.isBlank(dto.getDefaultValue()) && dto.getFieldType() == CustomFieldTypeEnum.BOOLEAN) {
            cft.setDefaultValue("false");
        }
        if (dto.isUseInheritedAsDefaultValue() != null) {
            cft.setUseInheritedAsDefaultValue(dto.isUseInheritedAsDefaultValue());
        }
        if (dto.isValueRequired() != null) {
            cft.setValueRequired(dto.isValueRequired());
        }
        if (dto.isVersionable() != null) {
            cft.setVersionable(dto.isVersionable());
        }
        if (dto.isTriggerEndPeriodEvent() != null) {
            cft.setTriggerEndPeriodEvent(dto.isTriggerEndPeriodEvent());
        }
        if (dto.getEntityClazz() != null) {
            cft.setEntityClazz(StringUtils.trimToNull(dto.getEntityClazz()));
        }
        if (dto.isAllowEdit() != null) {
            cft.setAllowEdit(dto.isAllowEdit());
        }
        if (dto.isHideOnNew() != null) {
            cft.setHideOnNew(dto.isHideOnNew());
        }
        if (dto.getMinValue() != null) {
            cft.setMinValue(dto.getMinValue());
        }
        if (dto.getMaxValue() != null) {
            cft.setMaxValue(dto.getMaxValue());
        }
        if (dto.getRegExp() != null) {
            cft.setRegExp(dto.getRegExp());
        }
        if (dto.getGuiPosition() != null) {
            cft.setGuiPosition(dto.getGuiPosition());
        }
        if (dto.getApplicableOnEl() != null) {
            cft.setApplicableOnEl(dto.getApplicableOnEl());
        }

        if (cft.getFieldType() == CustomFieldTypeEnum.LIST && dto.getListValues() != null) {
            cft.setListValues(dto.getListValues());
        }

        if (dto.getMapKeyType() != null) {
            cft.setMapKeyType(dto.getMapKeyType());
        }
        if (dto.getIndexType() != null) {
            cft.setIndexType(dto.getIndexType());
        }
        if (dto.getTags() != null) {
            cft.setTags(dto.getTags());
        }
        if (cft.getStorageType() == CustomFieldStorageTypeEnum.MAP && cft.getMapKeyType() == null) {
            cft.setMapKeyType(CustomFieldMapKeyEnum.STRING);
        }

        if (cft.getStorageType() == CustomFieldStorageTypeEnum.MATRIX && dto.getMatrixColumns() != null) {
            if (cft.getMatrixColumns() == null) {
                cft.setMatrixColumns(new ArrayList<CustomFieldMatrixColumn>());
            } else {
                cft.getMatrixColumns().clear();
            }

            for (CustomFieldMatrixColumnDto columnDto : dto.getMatrixColumns()) {
                cft.getMatrixColumns().add(CustomFieldMatrixColumnDto.fromDto(columnDto));
            }
        }

        if (cft.getFieldType() == CustomFieldTypeEnum.CHILD_ENTITY) {
            cft.setVersionable(false);
            cft.setStorageType(dto.getStorageType());
            if (dto.getChildEntityFieldsForSummary() != null) {
                cft.setChildEntityFieldsAsList(dto.getChildEntityFieldsForSummary());
            }
        }

        if (dto.getCalendar() != null) {
            if (StringUtils.isBlank(dto.getCalendar())) {
                cft.setCalendar(null);
            } else {
                Calendar calendar = calendarService.findByCode(dto.getCalendar());
                if (calendar != null) {
                    cft.setCalendar(calendar);
                } else {
                    cft.setCalendar(null);
                }
            }
        }

        if (dto.getLanguageDescriptions() != null) {
            cft.setDescriptionI18n(convertMultiLanguageToMapOfValues(dto.getLanguageDescriptions(), cft.getDescriptionI18n()));
        }

        cft.setUnique(dto.isUnique());

        cft.setIdentifier(dto.isIdentifier());
        
        cft.setFilter(dto.isFilter());
        
        cft.setHasReferenceJpaEntity(dto.hasReferenceJpaEntity());

        // A cft can't be stored in a db that is not available for its cet
        List<DBStorageType> storageTypes = null;
        if(cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
            String cetCode = CustomEntityTemplate.getCodeFromAppliesTo(cft.getAppliesTo());
            CustomEntityTemplate cet = customEntityTemplateService.findByCode(cetCode);
            if(cet == null) {
            	var message = String.format("CET %s referenced from cft %s does not exists", cetCode, cft.getCode());
            	throw new InvalidParameterException(message);
            }
            storageTypes = cet.getAvailableStorages();
        } else if(cft.getAppliesTo().startsWith(CustomRelationshipTemplate.CRT_PREFIX)) {
            String crtCode = EntityCustomizationUtils.getEntityCode(cft.getAppliesTo());
            CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(crtCode);
            storageTypes = crt.getAvailableStorages();
        }

        if(dto.getStorages() != null) {
            for (DBStorageType storageType : dto.getStorages()) {
                if (storageTypes == null || !storageTypes.contains(storageType)) {
                    String message = "Custom field %s can't be stored to %s as the CET / CRT with code %s is not configure to be stored in this database";
                    throw new InvalidParameterException(String.format(message, cft.getCode(), storageType, EntityCustomizationUtils.getEntityCode(cft.getAppliesTo())));
                }
            }
        }
        if (dto.getDisplayFormat() != null) {
            cft.setDisplayFormat(dto.getDisplayFormat());
        }

        if (dto.getSamples() != null) {
            cft.setSamples(dto.getSamples());
        }

        cft.setStorages(dto.getStorages());
        cft.setSummary(dto.isSummary());

        cft.setFileExtensions(dto.getFileExtensions());
        cft.setContentTypes(dto.getContentTypes());
        cft.setMaxFileSizeAllowedInKb(dto.getMaxFileSizeAllowedInKb());
        cft.setFilePath(dto.getFilePath());
        cft.setSaveOnExplorer(dto.isSaveOnExplorer());
        cft.setAudited(dto.isAudited());
        cft.setPersisted(dto.isPersisted());

        return cft;
    }

	/**
	 * Find a relationship with same name and with given source or target, or create a default one.
	 *
	 * @param relationshipName the relationship name
	 * @param sourceCet        the source cet
	 * @param targetCet        the target cet
	 * @return a relationship with the given name and that has the sourceCet or targetCet either as source or as target
	 * @throws BusinessException if an exception occurs during the creation of the {@link CustomRelationshipTemplate}
	 */
	public CustomRelationshipTemplate findOrCreateRelationship(String relationshipName, String sourceCet, String targetCet) throws BusinessException {
		CustomRelationshipTemplate crt;
		List<CustomRelationshipTemplate> availableCrts = customRelationshipTemplateService.findByNameAndSourceOrTarget(sourceCet, targetCet, relationshipName);
		if(!availableCrts.isEmpty()) {
			crt = availableCrts.get(0);
		} else {
			crt = new CustomRelationshipTemplate();
			crt.setCode(sourceCet + "_" + relationshipName + "_" + targetCet);
			crt.setName(relationshipName);
			crt.setStartNode(customEntityTemplateService.findByCode(sourceCet));
			crt.setEndNode(customEntityTemplateService.findByCode(targetCet));
			customRelationshipTemplateService.create(crt);
		}
		return crt;
	}

	@SuppressWarnings("unused")
	private void validateSamples(CustomFieldTemplate template) {
		
		if(template.getSamples() == null || template.getSamples().isEmpty()) {
			return;
		}

		if (template.getFieldType() == CustomFieldTypeEnum.STRING) {
			Map<Integer, String> validateSamples = SampleValueHelper.validateStringType(template.getSamples(), template.getStorageType());
			if (!validateSamples.isEmpty()) {
				for (Map.Entry<Integer, String> validateSample : validateSamples.entrySet()) {
					throw new IllegalArgumentException();
				}
			}
		}

		if (template.getFieldType() == CustomFieldTypeEnum.LONG) {
			Map<Integer, String> validateSamples = SampleValueHelper.validateLongType(template.getSamples(), template.getStorageType());
			if (!validateSamples.isEmpty()) {
				for (Map.Entry<Integer, String> validateSample : validateSamples.entrySet()) {
					throw new IllegalArgumentException();
				}
			}
		}

		if (template.getFieldType() == CustomFieldTypeEnum.DOUBLE) {
			Map<Integer, String> validateSamples = SampleValueHelper.validateDoubleType(template.getSamples(), template.getStorageType());
			if (!validateSamples.isEmpty()) {
				for (Map.Entry<Integer, String> validateSample : validateSamples.entrySet()) {
					throw new IllegalArgumentException();
				}
			}
		}

		if (template.getFieldType() == CustomFieldTypeEnum.CHILD_ENTITY) {
			Map<String, CustomFieldTemplate> customFieldTemplates = customFieldTemplateService.findByAppliesTo(CustomEntityTemplate.getAppliesTo(template.getEntityClazzCetCode()));
			Map<Integer, String> validateSamples = SampleValueHelper.validateChildEntityType(customFieldTemplates, template.getSamples(), template.getStorageType());
			if (!validateSamples.isEmpty()) {
				for (Map.Entry<Integer, String> validateSample : validateSamples.entrySet()) {
					throw new IllegalArgumentException();
				}
			}
		}

	}

    /* (non-Javadoc)
     * @see org.meveo.api.ApiService#find(java.lang.String)
     */
	@Override
	public CustomFieldTemplateDto find(String code) throws EntityDoesNotExistsException, MissingParameterException,
			InvalidParameterException, MeveoApiException, org.meveo.exceptions.EntityDoesNotExistsException {
		throw new UnsupportedOperationException("Use find(code, appliesTo) instead");
	}

    /* (non-Javadoc)
     * @see org.meveo.api.ApiService#createOrUpdate(java.lang.String)
     */
	@Override
	public CustomFieldTemplate createOrUpdate(CustomFieldTemplateDto dtoData)
			throws MeveoApiException, BusinessException {
		CustomFieldTemplate fieldTemplate = this.customFieldTemplateService.findByCodeAndAppliesTo(dtoData.getCode(), dtoData.getAppliesTo());
		if (fieldTemplate == null) {
			this.create(dtoData, dtoData.getAppliesTo());
		} else {
			this.update(dtoData, dtoData.getAppliesTo());
		}
		return fieldTemplate;
	}

	@Override
	public CustomFieldTemplateDto toDto(CustomFieldTemplate entity) throws MeveoApiException {
		CustomFieldTemplateDto dto = new CustomFieldTemplateDto(entity);
		
		return dto;
	}

	@Override
	public IPersistenceService<CustomFieldTemplate> getPersistenceService() {
		return this.customFieldTemplateService;
	}
}

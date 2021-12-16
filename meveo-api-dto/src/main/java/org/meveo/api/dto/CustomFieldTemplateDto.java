package org.meveo.api.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldIndexTypeEnum;
import org.meveo.model.crm.custom.CustomFieldMapKeyEnum;
import org.meveo.model.crm.custom.CustomFieldMatrixColumn;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.persistence.DBStorageType;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The Class CustomFieldTemplateDto.
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@XmlRootElement(name = "CustomFieldTemplate")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel
public class CustomFieldTemplateDto extends BaseEntityDto {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Field code. */
    @XmlAttribute(required = true)
    @ApiModelProperty(required = true, value = "Code of the custom field template")
    protected String code;

    /** Field label. */
    @XmlAttribute(required = true)
    @ApiModelProperty(required = true, value = "Description of the custom field template")
    protected String description;

    /** The language descriptions. */
    @ApiModelProperty("List of language descriptions information")
    private List<LanguageDescriptionDto> languageDescriptions;

    /** Value type. */
    @XmlElement(required = true)
    @ApiModelProperty(required = true, value = "Value type")
    protected CustomFieldTypeEnum fieldType;

    /** The account level. */
    @XmlElement(required = false)
    @ApiModelProperty(required = false, value = "The account level")
    protected String accountLevel;

    /**
     * PROVIDER - Provider, SELLER - Seller, CUST - customer, CA - Customer account, BA - Billing Account, UA - User account, SUB - subscription, ACC - access, CHARGE - charge
     * template, SERVICE - service template or service instance, OFFER_CATEGORY - Offer template category, OFFER - Offer template, JOB_XX - Job instance, CE_ - Custom entity
     * instance.
     */
    @XmlElement(required = false)
    @ApiModelProperty(required = false, value = "Applies to")
    protected String appliesTo;

    /** Default value. */
    @XmlElement
    @ApiModelProperty("Default value")
    protected String defaultValue;

    /** Shall inherited value be used as default value instead if available. */
    @XmlElement
    @ApiModelProperty("Whether to inherit value be used as default value instead if available")
    protected Boolean useInheritedAsDefaultValue;

    /** Value storage type. */
    @XmlElement(required = true)
    @ApiModelProperty(required = true, value = "Value storage type")
    protected CustomFieldStorageTypeEnum storageType;

    /** Is value required. */
    @XmlElement
    @ApiModelProperty("Whether to require value")
    protected Boolean valueRequired;

    /** Is value versionable. */
    @XmlElement
    @ApiModelProperty("Whether to versionable value")
    protected Boolean versionable;

    /** Should Period end event be fired when value period is over. */
    @XmlElement
    @ApiModelProperty("Whether to fire Period end event when value period is over")
    protected Boolean triggerEndPeriodEvent;

    /** Calendar associated to value versioning periods. */
    @XmlElement
    @ApiModelProperty("Calendar associated to value versioning periods")
    protected String calendar;

    /**
     * How long versionable values be cached past the period end date. As of v.4.7 not used anymore
     */
    @XmlElement
    @Deprecated
    @ApiModelProperty("Cache value time period")
    protected Integer cacheValueTimeperiod;

    /** Entity class and CET code for a reference to entity or child entity type fields. */
    @XmlElement
    @ApiModelProperty("Entity class and CET code for a reference to entity or child entity type fields")
    protected String entityClazz;

    /** List of values for LIST type field. */
    @XmlElement
    @ApiModelProperty("List of values for LIST type field")
    protected Map<String, String> listValues;

    /** Can value be changed when editing a previously saved entity. */
    @XmlElement
    @ApiModelProperty("Whether to change value when editing a previously saved entity")
    protected Boolean allowEdit = true;

    /** Do not show/apply field on new entity creation. */
    @XmlElement
    @ApiModelProperty("Whether to do not show/apply field on new entity creation")
    protected Boolean hideOnNew;

    /** Maximum value to validate long and double values OR maximum length of string value. */
    @XmlElement
    @ApiModelProperty("Maximum value to validate long and double values")
    protected Long maxValue;

    /** Minimum value to validate long and double values. */
    @XmlElement
    @ApiModelProperty("Minimum value to validate long and double values")
    protected Long minValue;

    /**
     * Regular expression to validate string values.
     */
    @XmlElement
    @ApiModelProperty("Regular expression to validate string values")
    protected String regExp;

    /**
     * Should value be cached. As of v.4.7 not used anymore
     */
    @XmlElement
    @Deprecated
    @ApiModelProperty("Whether to cache value")
    protected Boolean cacheValue;

    /** The content types. */
    @XmlElement
    @ApiModelProperty("List of content types")
    protected List<String> contentTypes = new ArrayList<String>();

    /** The file extensions. */
    @XmlElement
    @ApiModelProperty("List of file extensions")
    protected List<String> fileExtensions = new ArrayList<String>();

    /** The max file size allowed in kb. */
    @XmlElement
    @ApiModelProperty("Max files size allowed in kb")
    protected Long maxFileSizeAllowedInKb;

    /** The file path. */
    @XmlElement
    @ApiModelProperty("File path")
    protected String filePath;

    /** The save on explorer. */
    @ApiModelProperty("Whether to save on explorer")
    protected boolean saveOnExplorer;

    /**
     * Where field should be displayed. Format: tab:&lt;tab name&gt;:&lt;tab relative position&gt;;fieldGroup:&lt;fieldgroup name&gt;:&lt;fieldgroup relative
     * position&gt;;field:&lt;field relative position in fieldgroup
     * 
     * 
     * Tab and field group names support translation in the following format: &lt;default value&gt;|&lt;language3 letter key=translated value&gt;
     * 
     * e.g. tab:Tab default title|FRA=Title in french|ENG=Title in english:0;fieldGroup:Field group default label|FRA=Field group label in french|ENG=Field group label in
     * english:0;field:0 OR tab:Second tab:1;field:1
     */
    @XmlElement
    @ApiModelProperty("Gui position")
    protected String guiPosition;
    
    /** The is unique. */
    @XmlElement
    @ApiModelProperty("Whether to unique")
    @JsonProperty("unique")
    @JsonAlias("isUnique")
    protected boolean isUnique = false;
    
    /** The is filter. */
    @XmlElement
    @ApiModelProperty("Whether to filter")
    @JsonProperty("filter")
    @JsonAlias("isFilter")
    protected boolean isFilter;
    
    @XmlElement
    @ApiModelProperty("Name of relationship")
    private String relationshipName;
    
    @XmlElement
    @ApiModelProperty("Code of relationship")
    private String relationship;
    
    /** The expression separator. */
    @XmlElement
    @ApiModelProperty("Expression separator")
    protected String expressionSeparator;

    /**
     * Whether we can use this field as the identifier of the entity
     */
    @XmlElement
    @ApiModelProperty("Whether we can use this field as the identifier of the entity")
    private boolean identifier;

    /**
     * Key format of a map for map type fields.
     */
    @XmlElement()
    @ApiModelProperty("Key format of a map for map type fields")
    protected CustomFieldMapKeyEnum mapKeyType;

    /**
     * EL expression (including #{}) to evaluate when field is applicable.
     */
    @XmlElement()
    @ApiModelProperty("EL expression to evaluate when field is applicable")
    protected String applicableOnEl;

    /**
     * A list of columns matrix consists of.
     */
    @XmlElementWrapper(name = "matrixColumns")
    @XmlElement(name = "matrixColumn")
    @JsonProperty("matrixColumns")
    @ApiModelProperty("A list of columns matrix consists of")
    private List<CustomFieldMatrixColumnDto> matrixColumns;

    /**
     * A list of child entity fields to be displayed in a summary table of child entities.
     */
    @XmlElementWrapper(name = "childEntityFieldsForSummary")
    @XmlElement(name = "fieldCode")
    @JsonProperty("childEntityFieldsForSummary")
    @ApiModelProperty("A list of child entity fields to be displayed in a summary table of child entities")
    private List<String> childEntityFieldsForSummary;

    @XmlElement()
    @ApiModelProperty("List of storage types")
    private List<DBStorageType> storages;

    @XmlElement
    @ApiModelProperty("List of samples")
    private List<String> samples;

    @XmlElement
    @ApiModelProperty("Whether to summary")
    private boolean summary;

    /**
     * If and how custom field values should be indexed in Elastic Search.
     */
    @ApiModelProperty("If and how custom field values should be indexed in Elastic Search")
    private CustomFieldIndexTypeEnum indexType;

    /**
     * Tags assigned to custom field template.
     */
    @ApiModelProperty("Tags assigned to custom field template")
    private String tags;

    @XmlElement()
    @ApiModelProperty("Display format")
    private String displayFormat;
    
    @JsonIgnore
	private boolean hasReferenceJpaEntity;
    
    @JsonIgnore
	private boolean inDraft = false;
	
	private boolean audited = false;
	
	private boolean persisted = true;
	

    /**
     * Instantiates a new custom field template dto.
     */
    public CustomFieldTemplateDto() {
    	super();
    }

    /**
     * Instantiates a new custom field template dto.
     *
     * @param cf the cf
     */
    public CustomFieldTemplateDto(CustomFieldTemplate cf) {
    	audited = cf.isAudited();
        code = cf.getCode();
        storages = cf.getStoragesNullSafe();
        description = cf.getDescription();
        languageDescriptions = LanguageDescriptionDto.convertMultiLanguageFromMapOfValues(cf.getDescriptionI18n());
        fieldType = cf.getFieldType();
        accountLevel = cf.getAppliesTo();
        appliesTo = cf.getAppliesTo();
        defaultValue = cf.getDefaultValue();
        useInheritedAsDefaultValue = cf.isUseInheritedAsDefaultValue();
        storageType = cf.getStorageType();
        valueRequired = cf.isValueRequired();
        versionable = cf.isVersionable();
        triggerEndPeriodEvent = cf.isTriggerEndPeriodEvent();
        entityClazz = cf.getEntityClazz();
        if (cf.getCalendar() != null) {
            calendar = cf.getCalendar().getCode();
        }
        allowEdit = cf.isAllowEdit();
        hideOnNew = cf.isHideOnNew();
        minValue = cf.getMinValue();
        maxValue = cf.getMaxValue();
        regExp = cf.getRegExp();
        
        if(cf.getRelationship() != null) {
        	relationship = cf.getRelationship().getCode();
        } else {
            relationshipName = cf.getRelationshipName();
        }
        
        // cacheValue = cf.isCacheValue();
        // cacheValueTimeperiod = cf.getCacheValueTimeperiod();
        guiPosition = cf.getGuiPosition();
        if (cf.getFieldType() == CustomFieldTypeEnum.LIST) {
            listValues = cf.getListValuesSorted();
        }
        applicableOnEl = cf.getApplicableOnEl();
        mapKeyType = cf.getMapKeyType();
        indexType = cf.getIndexType();
        tags = cf.getTags();

        if (cf.getStorageType() == CustomFieldStorageTypeEnum.MATRIX && cf.getMatrixColumns() != null) {
            matrixColumns = new ArrayList<>();
            for (CustomFieldMatrixColumn column : cf.getMatrixColumns()) {
                matrixColumns.add(new CustomFieldMatrixColumnDto(column));
            }
        }

        if (cf.getFieldType() == CustomFieldTypeEnum.CHILD_ENTITY && cf.getChildEntityFields() != null) {
            childEntityFieldsForSummary = Arrays.asList(cf.getChildEntityFieldsAsList());
        }

        identifier = cf.isIdentifier();
        isFilter = cf.isFilter();
        isUnique = cf.isUnique();
        storages = cf.getStoragesNullSafe();
        summary=cf.isSummary();

        saveOnExplorer = cf.isSaveOnExplorer();
        fileExtensions = cf.getFileExtensions();
        contentTypes = cf.getContentTypes();
        maxFileSizeAllowedInKb = cf.getMaxFileSizeAllowedInKb();
        filePath = cf.getFilePath();
        samples = cf.getSamples();
        audited = cf.isAudited();
        persisted = cf.isPersisted();
    }

    /**
	 * Checks if is whether we can use this field as the identifier of the entity.
	 *
	 * @return the whether we can use this field as the identifier of the entity
	 */
    public boolean isIdentifier() {
        return identifier;
    }

    /**
	 * Sets the whether we can use this field as the identifier of the entity.
	 *
	 * @param identifier the new whether we can use this field as the identifier of the entity
	 */
    public void setIdentifier(boolean identifier) {
        this.identifier = identifier;
    }

    /**
	 * Gets the relationship name.
	 *
	 * @return the relationship name
	 */
    public String getRelationshipName() {
		return relationshipName;
	}

    /**
	 * Sets the relationship name.
	 *
	 * @param relationshipName the new relationship name
	 */
	public void setRelationshipName(String relationshipName) {
		this.relationshipName = relationshipName;
	}

    /**
	 * Gets the storages.
	 *
	 * @return the storages
	 */
    public List<DBStorageType> getStorages() {
        return storages;
    }

    /**
	 * Sets the storages.
	 *
	 * @param storages the new storages
	 */
    public void setStorages(List<DBStorageType> storages) {
        this.storages = storages;
    }

    /**
     * Gets the code.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the code.
     *
     * @param code the new code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the field type.
     *
     * @return the field type
     */
    public CustomFieldTypeEnum getFieldType() {
        return fieldType;
    }

    /**
     * Sets the field type.
     *
     * @param fieldType the new field type
     */
    public void setFieldType(CustomFieldTypeEnum fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * Gets the account level.
     *
     * @return the account level
     */
    public String getAccountLevel() {
        return accountLevel;
    }

    /**
     * Sets the account level.
     *
     * @param accountLevel the new account level
     */
    @Deprecated
    public void setAccountLevel(String accountLevel) {
        this.accountLevel = accountLevel;
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

    /**
     * Gets the default value.
     *
     * @return the default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value.
     *
     * @param defaultValue the new default value
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Checks if is use inherited as default value.
     *
     * @return the boolean
     */
    public Boolean isUseInheritedAsDefaultValue() {
        return useInheritedAsDefaultValue;
    }

    /**
     * Sets the use inherited as default value.
     *
     * @param useInheritedAsDefaultValue the new use inherited as default value
     */
    public void setUseInheritedAsDefaultValue(Boolean useInheritedAsDefaultValue) {
        this.useInheritedAsDefaultValue = useInheritedAsDefaultValue;
    }

    /**
     * Gets the storage type.
     *
     * @return the storage type
     */
    public CustomFieldStorageTypeEnum getStorageType() {
        return storageType;
    }

    /**
     * Sets the storage type.
     *
     * @param storageType the new storage type
     */
    public void setStorageType(CustomFieldStorageTypeEnum storageType) {
        this.storageType = storageType;
    }

    /**
     * Checks if is versionable.
     *
     * @return the boolean
     */
    public Boolean isVersionable() {
        return versionable;
    }

    /**
     * Sets the versionable.
     *
     * @param versionable the new versionable
     */
    public void setVersionable(Boolean versionable) {
        this.versionable = versionable;
    }

    /**
     * Checks if is trigger end period event.
     *
     * @return the boolean
     */
    public Boolean isTriggerEndPeriodEvent() {
        return triggerEndPeriodEvent;
    }

    /**
     * Sets the trigger end period event.
     *
     * @param triggerEndPeriodEvent the new trigger end period event
     */
    public void setTriggerEndPeriodEvent(Boolean triggerEndPeriodEvent) {
        this.triggerEndPeriodEvent = triggerEndPeriodEvent;
    }

    /**
     * Gets the calendar.
     *
     * @return the calendar
     */
    public String getCalendar() {
        return calendar;
    }

    /**
     * Sets the calendar.
     *
     * @param calendar the new calendar
     */
    public void setCalendar(String calendar) {
        this.calendar = calendar;
    }

    /**
     * Checks if is value required.
     *
     * @return the boolean
     */
    public Boolean isValueRequired() {
        return valueRequired;
    }

    /**
     * Sets the value required.
     *
     * @param valueRequired the new value required
     */
    public void setValueRequired(Boolean valueRequired) {
        this.valueRequired = valueRequired;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CustomFieldTemplateDto [code=" + code + ", description=" + description + ", fieldType=" + fieldType + ", accountLevel=" + accountLevel + ", appliesTo=" + appliesTo
                + ", defaultValue=" + defaultValue + ", storageType=" + storageType + ", mapKeyType=" + mapKeyType + ", valueRequired=" + valueRequired + ", versionable="
                + versionable + ", triggerEndPeriodEvent=" + triggerEndPeriodEvent + ", calendar=" + calendar + ", entityClazz=" + entityClazz + ", indexType=" + indexType + ", displayFormat=" + displayFormat + ", samples=" + samples + "]";
    }

    /**
     * Gets the entity clazz.
     *
     * @return the entity clazz
     */
    public String getEntityClazz() {
        return entityClazz;
    }

    /**
     * Sets the entity clazz.
     *
     * @param entityClazz the new entity clazz
     */
    public void setEntityClazz(String entityClazz) {
        this.entityClazz = entityClazz;
    }

    /**
     * Gets the list values.
     *
     * @return the listValues
     */
    public Map<String, String> getListValues() {
        if (listValues == null) {
            listValues = new HashMap<String, String>();
        }
        return listValues;
    }

    /**
     * Sets the list values.
     *
     * @param listValues the listValues to set
     */
    public void setListValues(Map<String, String> listValues) {
        this.listValues = listValues;
    }

    /**
     * Checks if is allow edit.
     *
     * @return the boolean
     */
    public Boolean isAllowEdit() {
        return allowEdit;
    }

    /**
     * Sets the allow edit.
     *
     * @param allowEdit the new allow edit
     */
    public void setAllowEdit(Boolean allowEdit) {
        this.allowEdit = allowEdit;
    }

    /**
     * Checks if is hide on new.
     *
     * @return the boolean
     */
    public Boolean isHideOnNew() {
        return hideOnNew;
    }

    /**
     * Sets the hide on new.
     *
     * @param hideOnNew the new hide on new
     */
    public void setHideOnNew(Boolean hideOnNew) {
        this.hideOnNew = hideOnNew;
    }

    /**
     * Gets the min value.
     *
     * @return the min value
     */
    public Long getMinValue() {
        return minValue;
    }

    /**
     * Sets the min value.
     *
     * @param minValue the new min value
     */
    public void setMinValue(Long minValue) {
        this.minValue = minValue;
    }

    /**
     * Gets the max value.
     *
     * @return the max value
     */
    public Long getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the max value.
     *
     * @param maxValue the new max value
     */
    public void setMaxValue(Long maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * Gets the reg exp.
     *
     * @return the reg exp
     */
    public String getRegExp() {
        return regExp;
    }

    /**
     * Sets the reg exp.
     *
     * @param regExp the new reg exp
     */
    public void setRegExp(String regExp) {
        this.regExp = regExp;
    }

    /**
     * Checks if is cache value.
     *
     * @return the boolean
     */
    public Boolean isCacheValue() {
        return cacheValue;
    }

    /**
     * Sets the cache value.
     *
     * @param cacheValue the new cache value
     */
    public void setCacheValue(Boolean cacheValue) {
        this.cacheValue = cacheValue;
    }

    /**
     * Gets the cache value timeperiod.
     *
     * @return the cache value timeperiod
     */
    public Integer getCacheValueTimeperiod() {
        return cacheValueTimeperiod;
    }

    /**
     * Sets the cache value timeperiod.
     *
     * @param cacheValueTimeperiod the new cache value timeperiod
     */
    public void setCacheValueTimeperiod(Integer cacheValueTimeperiod) {
        this.cacheValueTimeperiod = cacheValueTimeperiod;
    }

    /**
     * Gets the gui position.
     *
     * @return the gui position
     */
    public String getGuiPosition() {
        return guiPosition;
    }

    /**
     * Sets the gui position.
     *
     * @param guiPosition the new gui position
     */
    public void setGuiPosition(String guiPosition) {
        this.guiPosition = guiPosition;
    }

    /**
     * Gets the map key type.
     *
     * @return the map key type
     */
    public CustomFieldMapKeyEnum getMapKeyType() {
        return mapKeyType;
    }

    /**
     * Sets the map key type.
     *
     * @param mapKeyType the new map key type
     */
    public void setMapKeyType(CustomFieldMapKeyEnum mapKeyType) {
        this.mapKeyType = mapKeyType;
    }

    /**
     * Gets the applicable on el.
     *
     * @return the applicable on el
     */
    public String getApplicableOnEl() {
        return applicableOnEl;
    }

    /**
     * Sets the applicable on el.
     *
     * @param applicableOnEl the new applicable on el
     */
    public void setApplicableOnEl(String applicableOnEl) {
        this.applicableOnEl = applicableOnEl;
    }

    /**
     * Gets the matrix columns.
     *
     * @return the matrix columns
     */
    public List<CustomFieldMatrixColumnDto> getMatrixColumns() {
        if (matrixColumns == null) {
            matrixColumns = new ArrayList<CustomFieldMatrixColumnDto>();
        }
        return matrixColumns;
    }

    /**
     * Sets the matrix columns.
     *
     * @param matrixColumns the new matrix columns
     */
    public void setMatrixColumns(List<CustomFieldMatrixColumnDto> matrixColumns) {
        this.matrixColumns = matrixColumns;
    }

    /**
     * Gets the child entity fields for summary.
     *
     * @return the child entity fields for summary
     */
    public List<String> getChildEntityFieldsForSummary() {
        return childEntityFieldsForSummary;
    }

    /**
     * Sets the child entity fields for summary.
     *
     * @param childEntityFieldsForSummary the new child entity fields for summary
     */
    public void setChildEntityFieldsForSummary(List<String> childEntityFieldsForSummary) {
        this.childEntityFieldsForSummary = childEntityFieldsForSummary;
    }

    /**
     * Gets the index type.
     *
     * @return the index type
     */
    public CustomFieldIndexTypeEnum getIndexType() {
        return indexType;
    }

    /**
     * Sets the index type.
     *
     * @param indexType the new index type
     */
    public void setIndexType(CustomFieldIndexTypeEnum indexType) {
        this.indexType = indexType;
    }

    /**
     * Gets the tags.
     *
     * @return the tags
     */
    public String getTags() {
        return tags;
    }

    /**
     * Sets the tags.
     *
     * @param tags the new tags
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * Gets the language descriptions.
     *
     * @return the language descriptions
     */
    public List<LanguageDescriptionDto> getLanguageDescriptions() {
        return languageDescriptions;
    }

    /**
     * Sets the language descriptions.
     *
     * @param languageDescriptions the new language descriptions
     */
    public void setLanguageDescriptions(List<LanguageDescriptionDto> languageDescriptions) {
        this.languageDescriptions = languageDescriptions;
    }

	/**
	 * Checks if is unique.
	 *
	 * @return true, if is unique
	 */
	public boolean isUnique() {
		return isUnique;
	}

	/**
	 * Sets the unique.
	 *
	 * @param isUnique the new unique
	 */
	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

	/**
	 * Checks if is filter.
	 *
	 * @return true, if is filter
	 */
	public boolean isFilter() {
		return isFilter;
	}

	/**
	 * Sets the filter.
	 *
	 * @param isFilter the new filter
	 */
	public void setFilter(boolean isFilter) {
		this.isFilter = isFilter;
	}

	/**
	 * Gets the expression separator.
	 *
	 * @return the expression separator
	 */
	public String getExpressionSeparator() {
		return expressionSeparator;
	}

	/**
	 * Sets the expression separator.
	 *
	 * @param expressionSeparator the new expression separator
	 */
	public void setExpressionSeparator(String expressionSeparator) {
		this.expressionSeparator = expressionSeparator;
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

	/**
	 * Checks if is summary.
	 *
	 * @return true, if is summary
	 */
	public boolean isSummary() {
		return summary;
	}

	/**
	 * Sets the summary.
	 *
	 * @param summary the new summary
	 */
	public void setSummary(boolean summary) {
		this.summary = summary;
	}

    /**
	 * Gets the content types.
	 *
	 * @return the content types
	 */
    public List<String> getContentTypes() {
        return contentTypes;
    }

    /**
	 * Sets the content types.
	 *
	 * @param contentTypes the new content types
	 */
    public void setContentTypes(List<String> contentTypes) {
        this.contentTypes = contentTypes;
    }

    /**
	 * Gets the file extensions.
	 *
	 * @return the file extensions
	 */
    public List<String> getFileExtensions() {
        return fileExtensions;
    }

    /**
	 * Sets the file extensions.
	 *
	 * @param fileExtensions the new file extensions
	 */
    public void setFileExtensions(List<String> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    /**
	 * Gets the max file size allowed in kb.
	 *
	 * @return the max file size allowed in kb
	 */
    public Long getMaxFileSizeAllowedInKb() {
        return maxFileSizeAllowedInKb;
    }

    /**
	 * Sets the max file size allowed in kb.
	 *
	 * @param maxFileSizeAllowedInKb the new max file size allowed in kb
	 */
    public void setMaxFileSizeAllowedInKb(Long maxFileSizeAllowedInKb) {
        this.maxFileSizeAllowedInKb = maxFileSizeAllowedInKb;
    }

    /**
	 * Gets the file path.
	 *
	 * @return the file path
	 */
    public String getFilePath() {
        return filePath;
    }

    /**
	 * Sets the file path.
	 *
	 * @param filePath the new file path
	 */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
	 * Checks if is save on explorer.
	 *
	 * @return true, if is save on explorer
	 */
    public boolean isSaveOnExplorer() {
        return saveOnExplorer;
    }

    /**
	 * Sets the save on explorer.
	 *
	 * @param saveOnExplorer the new save on explorer
	 */
    public void setSaveOnExplorer(boolean saveOnExplorer) {
        this.saveOnExplorer = saveOnExplorer;
    }

    /**
	 * Gets the samples.
	 *
	 * @return the samples
	 */
    public List<String> getSamples() {
        return samples;
    }

    /**
	 * Sets the samples.
	 *
	 * @param samples the new samples
	 */
    public void setSamples(List<String> samples) {
        this.samples = samples;
    }
    
    /**
	 * Gets the entity clazz cet code.
	 *
	 * @return the entity clazz cet code
	 */
    public String getEntityClazzCetCode() {
        return CustomFieldTemplate.retrieveCetCode(entityClazz);
    }

	public boolean isInDraft() {
		return inDraft;
	}

	public void setInDraft(boolean inDraft) {
		this.inDraft = inDraft;
	}

	/**
	 * Checks for reference jpa entity.
	 *
	 * @return true, if successful
	 */
	public boolean hasReferenceJpaEntity() {
		return hasReferenceJpaEntity;
	}

	/**
	 * Sets the checks for reference jpa entity.
	 *
	 * @param hasReferenceJpaEntity the new checks for reference jpa entity
	 */
	public void setHasReferenceJpaEntity(boolean hasReferenceJpaEntity) {
		this.hasReferenceJpaEntity = hasReferenceJpaEntity;
	}

	/**
	 * Gets the relationship.
	 *
	 * @return the relationship
	 */
	public String getRelationship() {
		return relationship;
	}

	/**
	 * Sets the relationship.
	 *
	 * @param relationship the new relationship
	 */
	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public boolean isAudited() {
		return audited;
	}

	public void setAudited(boolean audited) {
		this.audited = audited;
	}

	public boolean isPersisted() {
		return persisted;
	}

	public void setPersisted(boolean persisted) {
		this.persisted = persisted;
	}
	
}
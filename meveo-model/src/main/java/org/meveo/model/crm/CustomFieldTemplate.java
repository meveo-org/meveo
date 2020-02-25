/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.model.crm;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.*;
import org.meveo.model.annotation.ImportOrder;
import org.meveo.model.catalog.Calendar;
import org.meveo.model.converter.StringListConverter;
import org.meveo.model.crm.custom.*;
import org.meveo.model.crm.custom.CustomFieldMatrixColumn.CustomFieldColumnUseEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.shared.DateUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author clement.bareth
 * @author akadid abdelmounaim
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @lastModifiedVersion 6.8.0
 **/
@Entity
@ModuleItem("CustomFieldTemplate")
@Cacheable
@ExportIdentifier({ "code", "appliesTo" })
@ObservableEntity
@ImportOrder(3)
@Table(name = "crm_custom_field_tmpl", uniqueConstraints = @UniqueConstraint(columnNames = { "code", "applies_to" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "crm_custom_fld_tmp_seq"), })
@NamedQueries({
        @NamedQuery(name = "CustomFieldTemplate.getCFTForCache", query = "SELECT cft from CustomFieldTemplate cft left join fetch cft.calendar where cft.disabled=false order by cft.appliesTo"),
        @NamedQuery(name = "CustomFieldTemplate.getCFTForIndex", query = "SELECT cft from CustomFieldTemplate cft where cft.disabled=false and cft.indexType is not null "),
        @NamedQuery(name = "CustomFieldTemplate.getCFTByCodeAndAppliesTo", query = "SELECT cft from CustomFieldTemplate cft where cft.code=:code and cft.appliesTo=:appliesTo", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "true") }),
        @NamedQuery(name = "CustomFieldTemplate.getCFTByAppliesTo", query = "SELECT cft from CustomFieldTemplate cft where cft.appliesTo=:appliesTo order by cft.code", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "true") }) })
public class CustomFieldTemplate extends BusinessEntity implements Comparable<CustomFieldTemplate> {

    private static final long serialVersionUID = -1403961759495272885L;

    public static long DEFAULT_MAX_LENGTH_STRING = 255L;

    public static String ENTITY_REFERENCE_CLASSNAME_CETCODE_SEPARATOR = " - ";

    public enum GroupedCustomFieldTreeItemType {

        root(null), tab("tab"), fieldGroup("fieldGroup"), field("field"), action("action");

        public String positionTag;

        GroupedCustomFieldTreeItemType(String tag) {
            this.positionTag = tag;
        }
    }

    @Column(name = "field_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private CustomFieldTypeEnum fieldType;

    @Column(name = "applies_to", nullable = false, length = 100)
    @Size(max = 100)
    @NotNull
    private String appliesTo;

    @Type(type = "numeric_boolean")
    @Column(name = "value_required")
    private boolean valueRequired;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "crm_custom_field_tmpl_val")
    private Map<String, String> listValues;

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderBy("columnUse ASC, position ASC")
    @CollectionTable(name = "crm_custom_field_tmpl_mcols", joinColumns = { @JoinColumn(name = "cft_id") })
    @AttributeOverrides({ @AttributeOverride(name = "code", column = @Column(name = "code", nullable = false, length = 20)),
            @AttributeOverride(name = "label", column = @Column(name = "label", nullable = false, length = 50)),
            @AttributeOverride(name = "keyType", column = @Column(name = "key_type", nullable = false, length = 10)),
            @AttributeOverride(name = "columnUse", column = @Column(name = "column_use", nullable = false)) })
    private List<CustomFieldMatrixColumn> matrixColumns = new ArrayList<>();

    @Transient
    private List<CustomFieldMatrixColumn> matrixKeyColumns;

    @Transient
    private List<CustomFieldMatrixColumn> matrixValueColumns;

    @Type(type = "numeric_boolean")
    @Column(name = "versionable")
    private boolean versionable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id")
    private Calendar calendar;

    // @Column(name = "cache_value_for")
    // private Integer cacheValueTimeperiod;

    @Column(name = "default_value", length = 250)
    @Size(max = 250)
    private String defaultValue;

    @Type(type = "numeric_boolean")
    @Column(name = "inh_as_def_value")
    private boolean useInheritedAsDefaultValue;

    /**
     * Reference to an entity. A classname. In case of CustomEntityTemplate, classname consist of "CustomEntityTemplate - &lt;CustomEntityTemplate code&gt;"
     */
    @Column(name = "entity_clazz")
    @Size(max = 255)
    private String entityClazz;

    @Column(name = "storage_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private CustomFieldStorageTypeEnum storageType = CustomFieldStorageTypeEnum.SINGLE;

    @Column(name = "mapkey_type")
    @Enumerated(EnumType.STRING)
    private CustomFieldMapKeyEnum mapKeyType;

    @Type(type = "numeric_boolean")
    @Column(name = "trigger_end_period_event", nullable = false)
    private boolean triggerEndPeriodEvent;

    @Transient
    private PrimitiveTypeEnum primitiveType;

    /**
     * Where field should be displayed. Format: tab:&lt;tab name&gt;:&lt;tab relative position&gt;;fieldGroup:&lt;fieldgroup name&gt;:&lt;fieldgroup relative
     * position&gt;;field:&lt;field relative position in fieldgroup/tab&gt;
     * 
     * Tab and field group names support translation in the following format: &lt;default value&gt;|&lt;language3 letter key=translated value&gt;
     * 
     * e.g. tab:Tab default title|FRA=Title in french|ENG=Title in english:0;fieldGroup:Field group default label|FRA=Field group label in french|ENG=Field group label in
     * english:0;field:0 OR tab:Second tab:1;field:1
     */
    @Column(name = "gui_position", length = 2000)
    @Size(max = 2000)
    private String guiPosition;

    @Type(type = "numeric_boolean")
    @Column(name = "allow_edit")
    @NotNull
    private boolean allowEdit = true;

    @Type(type = "numeric_boolean")
    @Column(name = "hide_on_new")
    @NotNull
    private boolean hideOnNew;

    @Column(name = "max_value")
    private Long maxValue;

    @Column(name = "min_value")
    private Long minValue;

    @Column(name = "reg_exp", length = 80)
    @Size(max = 80)
    private String regExp;

    @Column(name = "applicable_on_el", length = 2000)
    @Size(max = 2000)
    private String applicableOnEl;
    
    @Column(name = "relationship_name", updatable = false)
    private String relationshipName;

    /**
     * Child entity fields to display as summary. Field names are separated by a comma.
     */
    @Column(name = "che_fields", length = 500)
    @Size(max = 500)
    private String childEntityFields;

    /**
     * If and how custom field value should be indexed in Elastic Search
     */
    @Column(name = "index_type", length = 10)
    @Enumerated(EnumType.STRING)
    private CustomFieldIndexTypeEnum indexType;

    /**
     * Tags assigned to custom field template
     */
    @Column(name = "tags", length = 2000)
    @Size(max = 2000)
    private String tags;

    @Type(type = "json")
    @Column(name = "description_i18n", columnDefinition = "text")
    private Map<String, String> descriptionI18n;
    
    @Type(type="numeric_boolean") @ColumnDefault("0")
    @Column(name = "IS_UNIQUE")
    @NotNull
    private boolean unique;

    
    @Type(type="numeric_boolean") @ColumnDefault("1")
    @Column(name = "IS_FILTER")
    @NotNull
    private boolean filter = true;

    /**
     * Whether the field will appear in the listing of custom table
     */
    @Type(type="numeric_boolean") @ColumnDefault("0")
    @Column(name = "IS_SUMMARY")
    @NotNull
    private boolean summary = false;
    
    @Column(name = "EXPRESSION_SEPARATOR", length = 2)
    @Size(max = 2)
    private String expressionSeparator;

    /**
     * Whether we can use this field as the identifier of the entity
     */
    @Column(name = "identifier")
    @Type(type = "numeric_boolean")
    @ColumnDefault("0")
    private boolean identifier;

    /**
     * Storage where the cft value will be stored
     */
    @Column(name = "storages", columnDefinition = "TEXT")
    @Type(type = "jsonList")
    List<DBStorageType> storages = new ArrayList<>();

    /**
     * Display format for Date type only
     */
    @Column(name = "display_format", length = 20)
    @Size(max = 20)
    private String displayFormat;

	/**
	 * List of content types
	 */
	@Column(name = "content_types", length = 2000)
	@Convert(converter = StringListConverter.class)
	private List<String> contentTypes = new ArrayList<>();

	/**
	 * List of file extensions
	 */
	@Column(name = "file_extensions", length = 2000)
	@Convert(converter = StringListConverter.class)
	private List<String> fileExtensions = new ArrayList<>();

	/**
	 * Maximum size in kb.
	 */
	@Column(name = "max_file_size_allowed_kb")
	private Long maxFileSizeAllowedInKb;

	/**
	 * Supports EL variables.
	 */
	@Column(name = "file_path")
	private String filePath;

    /**
     * Whether the binaries will be accessible through the file explorer
     */
	@Column(name = "save_on_explorer")
    @Type(type = "numeric_boolean")
    @ColumnDefault("0")
    private boolean saveOnExplorer;

    @Column(name = "samples", columnDefinition = "TEXT")
    @Type(type = "jsonList")
    private List<String> samples = new ArrayList<>();

    /**
     * Database field name - derived from code
     */
    @Transient
    private String dbFieldname;

    @Transient
    private String contentType;

    @Transient
    private String fileExtension;

    public boolean isSaveOnExplorer() {
        return saveOnExplorer;
    }

    public void setSaveOnExplorer(boolean saveOnExplorer) {
        this.saveOnExplorer = saveOnExplorer;
    }

    public List<DBStorageType> getStorages() {
        return storages == null ? new ArrayList<>() : storages;
    }

    public void setStorages(List<DBStorageType> storages) {
        this.storages = storages;
    }

    /**
     * Get a database field name derived from a code value. Lowercase and spaces replaced by "_".
     *
     * @return Database field name
     */
    public String getDbFieldname() {
        if (dbFieldname == null && code != null) {
            dbFieldname = CustomFieldTemplate.getDbFieldname(code);
        }
        return dbFieldname;
    }


    /**
     * Get a database field name derived from a code value. Lowercase and spaces replaced by "_".
     *
     * @param code Field code
     * @return Database field name
     */
    public static String getDbFieldname(String code) {
        return BaseEntity.cleanUpAndLowercaseCodeOrId(code);
    }

    /**
     * Get GUI 'field' position value in a GUIPosition value as in e.g. "tab:Configuration:0;fieldGroup:Purge counter periods:1;field:0"
     *
     * @return GUI 'field' position value
     */
    public int getGUIFieldPosition() {
        if (guiPosition != null) {
            String position = getGuiPositionParsed().get(GroupedCustomFieldTreeItemType.field.positionTag + "_pos");
            if (position != null) {
                try {
                    return Integer.parseInt(position);
                } catch (NumberFormatException ignored) {

                }
            }
        }
        return 0;
    }

    public boolean isSummary() {
        return summary;
    }

    public void setSummary(boolean summary) {
        this.summary = summary;
    }

    public boolean isIdentifier() {
        return identifier;
    }

    public void setIdentifier(boolean identifier) {
        this.identifier = identifier;
    }

    public String getRelationshipName() {
		return relationshipName;
	}

	public void setRelationshipName(String relationshipName) {
		this.relationshipName = relationshipName;
	}

	public CustomFieldTypeEnum getFieldType() {
        return fieldType;
    }

    public void setFieldType(CustomFieldTypeEnum fieldType) {
        this.fieldType = fieldType;
    }
    
    public String getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = appliesTo;
    }

    public boolean isValueRequired() {
        return valueRequired;
    }

    public void setValueRequired(boolean valueRequired) {
        this.valueRequired = valueRequired;
    }

    public Map<String, String> getListValues() {
        return listValues;
    }

    public Map<String, String> getListValuesSorted() {
        if (listValues != null && !listValues.isEmpty()) {
            Comparator<String> dropdownListComparator = (s1, s2) -> {
                try {
                    return Integer.valueOf(s1).compareTo(Integer.valueOf(s2));
                } catch (NumberFormatException e) {
                    return s1.compareTo(s2);
                }
            };

            Map<String, String> newList = new TreeMap<>(dropdownListComparator);
            newList.putAll(listValues);
            return newList;
        }

        return listValues;
    }

    public void setListValues(Map<String, String> listValues) {
        this.listValues = listValues;
    }

    public List<CustomFieldMatrixColumn> getMatrixColumns() {
        return matrixColumns;
    }

    public void setMatrixColumns(List<CustomFieldMatrixColumn> matrixColumns) {
        this.matrixColumns = matrixColumns;
    }

    public void addMatrixKeyColumn() {
        CustomFieldMatrixColumn column = new CustomFieldMatrixColumn();
        column.setColumnUse(CustomFieldColumnUseEnum.USE_KEY);
        column.setPosition(getMatrixKeyColumns().size() + 1);
        this.matrixColumns.add(column);
        matrixKeyColumns = null;
    }

    public void addMatrixValueColumn() {
        CustomFieldMatrixColumn column = new CustomFieldMatrixColumn();
        column.setColumnUse(CustomFieldColumnUseEnum.USE_VALUE);
        column.setPosition(getMatrixValueColumns().size() + 1);
        this.matrixColumns.add(column);
        matrixValueColumns = null;
    }

    public void removeMatrixColumn(CustomFieldMatrixColumn columnToRemove) {
        this.matrixColumns.remove(columnToRemove);
        matrixKeyColumns = null;
        matrixValueColumns = null;

        // Reorder position
        int i = 0;
        for (CustomFieldMatrixColumn column : getMatrixKeyColumns()) {
            i++;
            column.setPosition(i);
        }
        i = 0;
        for (CustomFieldMatrixColumn column : getMatrixValueColumns()) {
            i++;
            column.setPosition(i);
        }
    }

    /**
     * Find a corresponding matrix column by its index (position). Note: result might differ if matrix column was added and value was not updated
     * 
     * @param index Index to return the column for
     * @return Matched matrix column
     */
    public CustomFieldMatrixColumn getMatrixColumnByIndex(int index) {
        if (index >= matrixColumns.size()) {
            return null;
        }
        return matrixColumns.get(index);
    }

    /**
     * Extract codes of matrix columns into a sorted list by column index.
     * 
     * @return A list of matrix column codes
     */
    public List<String> getMatrixColumnCodes() {

        List<String> matrixColumnNames = null;
        if (storageType == CustomFieldStorageTypeEnum.MATRIX) {
            matrixColumnNames = new ArrayList<>();
            for (CustomFieldMatrixColumn column : matrixColumns) {
                matrixColumnNames.add(column.getCode());
            }
        }
        return matrixColumnNames;
    }

    public void setVersionable(boolean versionable) {
        this.versionable = versionable;
    }

    public boolean isVersionable() {
        return versionable;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isUseInheritedAsDefaultValue() {
        return useInheritedAsDefaultValue;
    }

    public void setUseInheritedAsDefaultValue(boolean useInheritedAsDefaultValue) {
        this.useInheritedAsDefaultValue = useInheritedAsDefaultValue;
    }

    public String getEntityClazz() {
        return entityClazz;
    }

    public void setEntityClazz(String entityClazz) {
        this.entityClazz = entityClazz;
    }

    public String getEntityClazzCetCode() {
        return CustomFieldTemplate.retrieveCetCode(entityClazz);
    }

    public PrimitiveTypeEnum getPrimitiveType() {
        return primitiveType;
    }

    public void setPrimitiveType(PrimitiveTypeEnum primitiveType) {
        this.primitiveType = primitiveType;
    }

    public String getReferenceEntityClazzCetCode() {
        return "Reference to "+ CustomFieldTemplate.retrieveCetCode(entityClazz);
    }

    public String getReferenceEntityClassName() {
        return CustomFieldTemplate.retrieveClassName(entityClazz);
    }

    /**
     * Retrieve a class name from classname and code as it is stored in entityClazz field.
     *
     * @param entityClazz entity class
     * @return className
     */
    public static String retrieveClassName(String entityClazz) {
        if (entityClazz == null) {
            return null;
        }
        if (entityClazz.startsWith(CustomEntityTemplate.class.getName())) {
            return entityClazz.substring(0, entityClazz.indexOf(ENTITY_REFERENCE_CLASSNAME_CETCODE_SEPARATOR) + ENTITY_REFERENCE_CLASSNAME_CETCODE_SEPARATOR.length() - 3);
        }
        return entityClazz;
    }

    /**
     * Retrieve a cet code from classname and code as it is stored in entityClazz field.
     * 
     * @param entityClazz entity class
     * @return code
     */
    public static String retrieveCetCode(String entityClazz) {
        if (entityClazz == null) {
            return null;
        }
        if (entityClazz.startsWith(CustomEntityTemplate.class.getName())) {
            return entityClazz.substring(entityClazz.indexOf(ENTITY_REFERENCE_CLASSNAME_CETCODE_SEPARATOR) + ENTITY_REFERENCE_CLASSNAME_CETCODE_SEPARATOR.length());
        }
        return entityClazz;
    }

    public Object getDefaultValueConverted() {
        if (defaultValue == null) {
        	return null;
        }
        try {
            if (fieldType == CustomFieldTypeEnum.DOUBLE) {
                return Double.parseDouble(defaultValue);
            } else if (fieldType == CustomFieldTypeEnum.LONG) {
                return Long.parseLong(defaultValue);
            } else if (fieldType == CustomFieldTypeEnum.STRING || fieldType == CustomFieldTypeEnum.LIST || fieldType == CustomFieldTypeEnum.TEXT_AREA) {
                return defaultValue;
            } else if (fieldType == CustomFieldTypeEnum.DATE) {
                return DateUtils.parseDateWithPattern(defaultValue, DateUtils.DATE_TIME_PATTERN);
            }else if (fieldType == CustomFieldTypeEnum.BOOLEAN) {
                return Boolean.parseBoolean(defaultValue);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public CustomFieldStorageTypeEnum getStorageType() {
        return storageType;
    }

    public void setStorageType(CustomFieldStorageTypeEnum storageType) {
        this.storageType = storageType;
    }

    public CustomFieldMapKeyEnum getMapKeyType() {
        return mapKeyType;
    }

    public void setMapKeyType(CustomFieldMapKeyEnum mapKeyType) {
        this.mapKeyType = mapKeyType;
    }

    public boolean isTriggerEndPeriodEvent() {
        return triggerEndPeriodEvent;
    }

    public void setTriggerEndPeriodEvent(boolean triggerEndPeriodEvent) {
        this.triggerEndPeriodEvent = triggerEndPeriodEvent;
    }

    // public Integer getCacheValueTimeperiod() {
    // return cacheValueTimeperiod;
    // }
    //
    // public void setCacheValueTimeperiod(Integer cacheValueTimeperiod) {
    // this.cacheValueTimeperiod = cacheValueTimeperiod;
    // }

    public String getGuiPosition() {
        return guiPosition;
    }

    public void setGuiPosition(String guiPosition) {
        this.guiPosition = guiPosition;
    }

    public Map<String, String> getGuiPositionParsed() {

        if (guiPosition == null) {
            return null;
        }

        Map<String, String> parsedInfo = new HashMap<>();

        String[] positions = guiPosition.split(";");

        for (String position : positions) {
            String[] positionDetails = position.split(":");
            if (!positionDetails[0].equals(GroupedCustomFieldTreeItemType.field.positionTag)) {
                parsedInfo.put(positionDetails[0] + "_name", positionDetails[1]);
                if (positionDetails.length == 3) {
                    parsedInfo.put(positionDetails[0] + "_pos", positionDetails[2]);
                }
            } else if (positionDetails.length == 2) {
                parsedInfo.put(positionDetails[0] + "_pos", positionDetails[1]);
            }
        }

        return parsedInfo;
    }

    public boolean isAllowEdit() {
        return allowEdit;
    }

    public void setAllowEdit(boolean allowEdit) {
        this.allowEdit = allowEdit;
    }

    public boolean isHideOnNew() {
        return hideOnNew;
    }

    public void setHideOnNew(boolean hideOnNew) {
        this.hideOnNew = hideOnNew;
    }

    public Long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Long maxValue) {
        this.maxValue = maxValue;
    }

    public Long getMinValue() {
        return minValue;
    }

    public void setMinValue(Long minValue) {
        this.minValue = minValue;
    }

    public String getRegExp() {
        return regExp;
    }

    public void setRegExp(String regExp) {
        this.regExp = regExp;
    }

    // public boolean isCacheValue() {
    // return cacheValue;
    // }
    //
    // public void setCacheValue(boolean cacheValue) {
    // this.cacheValue = cacheValue;
    // }

    public String getApplicableOnEl() {
        return applicableOnEl;
    }

    public void setApplicableOnEl(String applicableOnEl) {
        this.applicableOnEl = applicableOnEl;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof CustomFieldTemplate)) {
            return false;
        }

        CustomFieldTemplate other = (CustomFieldTemplate) obj;

        if (getId() != null && other.getId() != null && getId().equals(other.getId())) {
             return true;
        }

        if (code == null && other.getCode() != null) {
            return false;
        } else if (!code.equals(other.getCode())) {
            return false;
        } else if (appliesTo == null && other.getAppliesTo() != null) {
            return false;
        } else return appliesTo.equals(other.getAppliesTo());
    }

    public String getChildEntityFields() {
        return childEntityFields;
    }

    public String[] getChildEntityFieldsAsList() {
        if (childEntityFields != null) {
            return childEntityFields.split("\\|");
        }
        return new String[0];
    }

    public void setChildEntityFields(String childEntityFields) {
        this.childEntityFields = childEntityFields;
    }

    public void setChildEntityFieldsAsList(List<String> cheFields) {
        this.childEntityFields = StringUtils.concatenate("|", cheFields);
    }

    public CustomFieldIndexTypeEnum getIndexType() {
        return indexType;
    }

    public void setIndexType(CustomFieldIndexTypeEnum indexType) {
        this.indexType = indexType;
    }
    
    public boolean isVisibleOnUI() {
        return allowEdit;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    
    
    public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public boolean isFilter() {
		return filter;
	}

	public void setFilter(boolean filter) {
		this.filter = filter;
	}

	public String getExpressionSeparator() {
		return expressionSeparator;
	}

	public void setExpressionSeparator(String expressionSeparator) {
		this.expressionSeparator = expressionSeparator;
	}

	@Override
    public String toString() {
        return String.format("CustomFieldTemplate [id=%s, appliesTo=%s, code=%s]", id, appliesTo, code);
    }

    @Override
    public int compareTo(CustomFieldTemplate o) {
        return o.getCode().compareTo(getCode());
    }

    /**
     * Instantiate a CustomFieldValue from a template, setting a default value if applicable.
     *
     * @return CustomFieldValue object
     */
    public CustomFieldValue toDefaultCFValue() {
        CustomFieldValue cfValue = new CustomFieldValue();

        // Set a default value
        if (getStorageType() == CustomFieldStorageTypeEnum.SINGLE) {
            cfValue.setValue(getDefaultValueConverted());
        }

        return cfValue;
    }

    /**
     * Get a date period for a given date. Applies only to CFT versionable by a calendar.
     * 
     * @param date Date
     * @return Date period matching calendar's dates
     */
    public DatePeriod getDatePeriod(Date date) {
        if (isVersionable() && getCalendar() != null) {
            return new DatePeriod(getCalendar().previousCalendarDate(date), getCalendar().nextCalendarDate(date));
        }
        return null;
    }

    public Map<String, String> getDescriptionI18n() {
        return descriptionI18n;
    }

    public void setDescriptionI18n(Map<String, String> descriptionI18n) {
        this.descriptionI18n = descriptionI18n;
    }

    /**
     * Instantiate descriptionI18n field if it is null. NOTE: do not use this method unless you have an intention to modify it's value, as entity will be marked dirty and record
     * will be updated in DB
     * 
     * @return descriptionI18n value or instantiated descriptionI18n field value
     */
    public Map<String, String> getDescriptionI18nNullSafe() {
        if (descriptionI18n == null) {
            descriptionI18n = new HashMap<>();
        }
        return descriptionI18n;
    }

    /**
     * Get description in a given language. Will return default description if not found for the language
     * 
     * @param language language code
     * @return descriptionI18n value or instantiated descriptionI18n field value
     * @author akadid abdelmounaim
     * @lastModifiedVersion 5.0
     */
    public String getDescription(String language) {

        if (language == null || descriptionI18n == null || descriptionI18n.isEmpty()) {
            return description;
        }

        language = language.toUpperCase();
        if (StringUtils.isBlank(descriptionI18n.get(language))) {
            return description;
        } else {
            return descriptionI18n.get(language);
        }
    }

    /**
     * Get a list of matrix columns used as key columns
     * 
     * @return A list of matrix columns where isKeyColumn = true
     */
    public List<CustomFieldMatrixColumn> getMatrixKeyColumns() {

        if (matrixKeyColumns != null) {
            return matrixKeyColumns;
        }
        matrixKeyColumns = matrixColumns.stream().filter(CustomFieldMatrixColumn::isColumnForKey).collect(Collectors.toList());
        return matrixKeyColumns;
    }

    /**
     * Extract codes of matrix columns used as key columns into a sorted list by column index
     * 
     * @return A list of matrix column codes
     */
    public List<String> getMatrixKeyColumnCodes() {

        List<String> matrixColumnNames = null;
        if (storageType == CustomFieldStorageTypeEnum.MATRIX) {
            matrixColumnNames = new ArrayList<>();
            for (CustomFieldMatrixColumn column : getMatrixKeyColumns()) {
                matrixColumnNames.add(column.getCode());
            }
        }
        return matrixColumnNames;
    }

    /**
     * Get a list of matrix columns used as value columns
     * 
     * @return A list of matrix columns where isKeyColumn = false
     */
    public List<CustomFieldMatrixColumn> getMatrixValueColumns() {

        if (matrixValueColumns != null) {
            return matrixValueColumns;
        }

        matrixValueColumns = matrixColumns.stream().filter(elem -> !elem.isColumnForKey()).collect(Collectors.toList());

        return matrixValueColumns;
    }

    /**
     * Extract codes of matrix columns used as value columns into a sorted list by column index
     * 
     * @return A list of matrix column codes
     */
    public List<String> getMatrixValueColumnCodes() {

        List<String> matrixColumnNames = null;
        if (storageType == CustomFieldStorageTypeEnum.MATRIX) {
            matrixColumnNames = new ArrayList<>();
            for (CustomFieldMatrixColumn column : getMatrixValueColumns()) {
                matrixColumnNames.add(column.getCode());
            }
        }
        return matrixColumnNames;
    }

    /**
     * Parse multi-value value from string to a map of values
     * 
     * @param multiValue Multi-value value as string
     * @param appendToMap Map to append values to. If not provided a new map will be instantiated.
     * @return Map of values (or same as appendToMap if provided)
     */
    public Map<String, Object> deserializeMultiValue(String multiValue, Map<String, Object> appendToMap) {

        // DO NOT REMOVE - Initialize matrixValueColumns field
        getMatrixValueColumns();

        Map<String, Object> values = appendToMap;
        if (values == null) {
            values = new HashMap<>();
        }

        // Multi-value values are concatenated when stored - split them and set as separate map key/values
        String[] splitValues = multiValue.split("\\" + CustomFieldValue.MATRIX_KEY_SEPARATOR);
        for (int i = 0; i < splitValues.length; i++) {
            CustomFieldMapKeyEnum dataType = matrixValueColumns.get(i).getKeyType();
            if (dataType == CustomFieldMapKeyEnum.STRING || dataType == CustomFieldMapKeyEnum.TEXT_AREA) {
                values.put(matrixValueColumns.get(i).getCode(), splitValues[i]);

            } else if (!StringUtils.isBlank(splitValues[i])) {
                try {
                    if (dataType == CustomFieldMapKeyEnum.LONG) {
                        values.put(matrixValueColumns.get(i).getCode(), Long.parseLong(splitValues[i]));
                    } else if (dataType == CustomFieldMapKeyEnum.DOUBLE) {
                        values.put(matrixValueColumns.get(i).getCode(), Double.parseDouble(splitValues[i]));
                    }
                } catch (Exception e) {
                    // Was not a number - ignore
                }
            }
        }

        return values;
    }

    /**
     * Serialize multi-value from a map of values to a string
     * 
     * @param mapValues Map of values
     * @return A string with concatenated values
     */
    public String serializeMultiValue(Map<String, Object> mapValues) {

        // DO NOT REMOVE - Initialize matrixValueColumns field
        getMatrixValueColumns();

        boolean valueSet = false;
        StringBuilder valBuilder = new StringBuilder();
        for (CustomFieldMatrixColumn column : matrixValueColumns) {
            valBuilder.append(valBuilder.length() == 0 ? "" : CustomFieldValue.MATRIX_KEY_SEPARATOR);
            Object columnValue = mapValues.get(column.getCode());
            if (StringUtils.isBlank(columnValue)) {
                continue;
            }
            valueSet = true;
            CustomFieldMapKeyEnum dataType = column.getKeyType();
            if (dataType == CustomFieldMapKeyEnum.STRING || dataType == CustomFieldMapKeyEnum.TEXT_AREA) {
                valBuilder.append((String) columnValue);
            } else if (dataType == CustomFieldMapKeyEnum.LONG || dataType == CustomFieldMapKeyEnum.DOUBLE) {
                valBuilder.append(columnValue.toString());
            }
        }

        if (!valueSet) {
            return null;
        }

        return valBuilder.toString();
    }

    public String getDisplayFormat() {
        return displayFormat;
    }

    public void setDisplayFormat(String displayFormat) {
        this.displayFormat = displayFormat;
    }

	public List<String> getContentTypes() {
		return contentTypes;
	}

	public void setContentTypes(List<String> contentTypes) {
		this.contentTypes = contentTypes;
	}

	public List<String> getFileExtensions() {
		return fileExtensions;
	}

	public void setFileExtensions(List<String> fileExtensions) {
		this.fileExtensions = fileExtensions;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public void addContentType(String ct) {
		if (getContentTypes() == null) {
			contentTypes = new ArrayList<>();
		}

		contentTypes.add(ct);
	}

	public void addFileExtension(String fe) {
		if (getFileExtensions() == null) {
			fileExtensions = new ArrayList<>();
		}

		fileExtensions.add(fe);
	}

	public Long getMaxFileSizeAllowedInKb() {
		return maxFileSizeAllowedInKb;
	}

	public void setMaxFileSizeAllowedInKb(Long maxFileSizeAllowedInKb) {
		this.maxFileSizeAllowedInKb = maxFileSizeAllowedInKb;
	}

	public Long getMaxFileSizeAllowedInBytes() {
		return maxFileSizeAllowedInKb != null ? maxFileSizeAllowedInKb * 1000 : 0L;
	}

    public boolean isSqlStorage() {
        return storages != null && storages.contains(DBStorageType.SQL);
    }

    public List<String> getSamples() {
    	if(samples == null) {
    		this.samples = new ArrayList<>();
    	}
    	
        return samples;
    }

    public void setSamples(List<String> samples) {
    	if(samples == null && this.samples == null) {
    		samples = new ArrayList<>();
    	} else if (samples == null && this.samples != null) { 
    		this.samples.clear();
    	} else {
	        this.samples = samples;
    	}
    }

    public List<?> getNewListValue() {
		if (storageType != CustomFieldStorageTypeEnum.LIST) {
			return null;
		}

		switch (fieldType) {
		case BOOLEAN:
			return new ArrayList<Boolean>();
		case DATE:
			return new ArrayList<Date>();
		case DOUBLE:
			return new ArrayList<Double>();
		case EXPRESSION:
		case TEXT_AREA:
		case LIST:
		case STRING:
			return new ArrayList<String>();
		case LONG:
			return new ArrayList<Long>();
		default:
			return new ArrayList<>();
		}
	}
}

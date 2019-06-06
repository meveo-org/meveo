package org.meveo.api.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.persistence.Neo4JStorageConfigurationDto;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.sql.Neo4JStorageConfiguration;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;


/**
 * The Class CustomEntityTemplateDto.
 *
 * @author Andrius Karpavicius
 */

@XmlRootElement(name = "CustomEntityTemplate")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomEntityTemplateDto extends BaseDto {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6633504145323452803L;

    /** The code. */
    @XmlAttribute(required = true)
    private String code;

    @XmlElement()
	private String superTemplate;

    /** The name. */
    @XmlAttribute(required = true)
    private String name;

    /** The description. */
    @XmlAttribute()
    private String description;

    /** The fields. */
    @XmlElementWrapper(name = "fields")
    @XmlElement(name = "field")
    private List<CustomFieldTemplateDto> fields;

    /** The actions. */
    @XmlElementWrapper(name = "actions")
    @XmlElement(name = "action")
    private List<EntityCustomActionDto> actions;
    
    /** The pre-persist script instance code. */
    @XmlAttribute()
    private String prePersistScripCode;

    /** Category the CET belgongs to */
    @XmlAttribute()
    private String customEntityCategoryCode;

    /**
     * Storage where cfts can be stored
     */
    @XmlElement()
    private List<DBStorageType> availableStorages;
    
    @XmlElement
    private Neo4JStorageConfigurationDto neo4jStorageConfiguration;

    @XmlElement()
    private SQLStorageConfiguration sqlStorageConfiguration = new SQLStorageConfiguration();

    public List<DBStorageType> getAvailableStorages() {
        return availableStorages;
    }

    public void setAvailableStorages(List<DBStorageType> availableStorages) {
        this.availableStorages = availableStorages;
    }
    
    public Neo4JStorageConfigurationDto getNeo4jStorageConfiguration() {
		return neo4jStorageConfiguration;
	}

	public void setNeo4jStorageConfiguration(Neo4JStorageConfigurationDto neo4jStorageConfiguration) {
		this.neo4jStorageConfiguration = neo4jStorageConfiguration;
	}

	public SQLStorageConfiguration getSqlStorageConfiguration() {
		return sqlStorageConfiguration;
    }

    public void setSqlStorageConfiguration(SQLStorageConfiguration sqlStorageConfiguration) {
        this.sqlStorageConfiguration = sqlStorageConfiguration;
    }

    /**
     * Instantiates a new custom entity template dto.
     */
    public CustomEntityTemplateDto() {

    }
    
    public String getPrePersistScripCode() {
		return prePersistScripCode;
	}

	public void setPrePersistScripCode(String prePersistScripCode) {
		this.prePersistScripCode = prePersistScripCode;
	}

    /**
     * Gets the code.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    public String getSuperTemplate() {
        return superTemplate;
    }

    public void setSuperTemplate(String superTemplate) {
        this.superTemplate = superTemplate;
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
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
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
     * Gets the fields.
     *
     * @return the fields
     */
    public List<CustomFieldTemplateDto> getFields() {
        return fields;
    }

    /**
     * Sets the fields.
     *
     * @param fields the new fields
     */
    public void setFields(List<CustomFieldTemplateDto> fields) {
        this.fields = fields;
    }

    /**
     * Gets the actions.
     *
     * @return the actions
     */
    public List<EntityCustomActionDto> getActions() {
        return actions;
    }

    /**
     * Sets the actions.
     *
     * @param actions the new actions
     */
    public void setActions(List<EntityCustomActionDto> actions) {
        this.actions = actions;
    }

    public String getCustomEntityCategoryCode() {
        return customEntityCategoryCode;
    }

    public void setCustomEntityCategoryCode(String customEntityCategoryCode) {
        this.customEntityCategoryCode = customEntityCategoryCode;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CustomEntityTemplateDto [code=" + code + ", name=" + name + ", description=" + description + ", fields=" + fields + "]";
    }

}

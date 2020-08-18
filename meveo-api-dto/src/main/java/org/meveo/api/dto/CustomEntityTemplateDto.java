package org.meveo.api.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.persistence.Neo4JStorageConfigurationDto;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * The Class CustomEntityTemplateDto.
 *
 * @author Andrius Karpavicius
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@XmlRootElement(name = "CustomEntityTemplate")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel
public class CustomEntityTemplateDto extends BaseEntityDto implements Comparable<CustomEntityTemplateDto> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6633504145323452803L;

    /** The code. */
    @XmlAttribute(required = true)
    @ApiModelProperty(required = true, value = "Code of custom entity template")
    private String code;

    @XmlElement()
    @ApiModelProperty("Super template")
	private String superTemplate;

    /** The name. */
    @XmlAttribute(required = true)
    @ApiModelProperty(required = true, value = "Name of custom entity template")
    private String name;

    /** The description. */
    @XmlAttribute()
    @ApiModelProperty("Description of custom entity template")
    private String description;

    /** The fields. */
    @XmlElementWrapper(name = "fields")
    @XmlElement(name = "field")
    @JsonProperty("fields")
    @ApiModelProperty("List of custom field templates information")
    private List<CustomFieldTemplateDto> fields;

    /** The actions. */
    @JsonProperty("actions")
    @JsonAlias("action")
    @XmlElementWrapper(name = "actions")
    @XmlElement(name = "action")
    @ApiModelProperty("List of entity custom actions information")
    private List<EntityCustomActionDto> actions;
    
    /** The pre-persist script instance code. */
    @XmlAttribute()
    @ApiModelProperty("Code of the pre-persist script instance")
    private String prePersistScripCode;

    /** Category the CET belgongs to */
    @XmlAttribute()
    @ApiModelProperty("Code of the custom entity category")
    private String customEntityCategoryCode;

    /**
     * Storage where cfts can be stored
     */
    @XmlElement()
    @ApiModelProperty("List of storages where cfts can be stored")
    private List<DBStorageType> availableStorages;
    
    @XmlElement
    @ApiModelProperty("Neo4J storage configuration information")
    private Neo4JStorageConfigurationDto neo4jStorageConfiguration;

    @XmlElement()
    @ApiModelProperty("SQL storage configuration")
    private SQLStorageConfiguration sqlStorageConfiguration = new SQLStorageConfiguration();
    
    @ApiModelProperty("List of samples")
    private List<String> samples = new ArrayList<>();
    
    @XmlAttribute()
    @ApiModelProperty("Whether a table that will audit the changes will be created.")
	private boolean audited = false;
    
    /**
     * Instantiates a new custom entity template dto.
     */
    public CustomEntityTemplateDto() {
    	super();
    }

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

	public List<String> getSamples() {
		return samples;
	}

	public void setSamples(List<String> samples) {
		this.samples = samples;
	}

	public boolean isAudited() {
		return audited;
	}

	public void setAudited(boolean audited) {
		this.audited = audited;
	}

	@Override
	public int compareTo(CustomEntityTemplateDto o) {
		// If one CET is a super template of the other, it should be placed before
		if(this.getSuperTemplate() != null && this.getSuperTemplate().equals(o.getCode())) {
			return 1;
		} else if(o.getSuperTemplate() != null && o.getSuperTemplate().equals(this.code)) {
			return -1;
		}
		
		// CET with super templates should be placed after CET that doesn't have super templates
		if(this.superTemplate != null && o.superTemplate == null) {
			return 1;
		} else if(this.superTemplate == null && o.superTemplate != null) {
			return -1;
		}	
		
		return 0;
	}

}

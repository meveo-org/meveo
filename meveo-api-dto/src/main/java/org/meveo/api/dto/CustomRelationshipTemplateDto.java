package org.meveo.api.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.model.billing.RelationshipDirectionEnum;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.DBStorageType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Rachid AITYAAZZA
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel
public class CustomRelationshipTemplateDto extends BaseEntityDto {

    private static final long serialVersionUID = -6633504145323452803L;

    @XmlAttribute(required = true)
    @ApiModelProperty(required = true, value = "Code of the custom relationship template")
    private String code;

    @XmlAttribute()
    @ApiModelProperty("Name of the custom relationship template")
    private String name;

    @XmlAttribute()
    @ApiModelProperty("Whether to unique value")
    private boolean isUnique = false;

    @XmlAttribute()
    @ApiModelProperty("Description of the custom relationship template")
    private String description;

    @XmlElementWrapper(name = "fields")
    @XmlElement(name = "field")
    @JsonProperty("fields")
    @ApiModelProperty("List of custom fields information")
    private List<CustomFieldTemplateDto> fields = new ArrayList<>();

    @ApiModelProperty(required = true, value = "Start node code")
    private String startNodeCode;

    @ApiModelProperty(required = true, value = "End node code")
    private String endNodeCode;

    @XmlAttribute()
    @ApiModelProperty("Relationship direction type")
    private RelationshipDirectionEnum direction;

    @XmlAttribute()
    @ApiModelProperty("List of start node keys")
    private List<String> startNodeKeys = new ArrayList<>();

    @XmlAttribute()
    @ApiModelProperty("List of end node keys")
    private List<String> endNodeKeys = new ArrayList<>();
    
    /**
     * Storage where the custom relationship instances should be stored.
     * If SQL is included, will create a dedicated table
     */
    @XmlAttribute()
    @ApiModelProperty("List of available storages where the custom relationship instances should be stored")
    private List<DBStorageType> availableStorages = new ArrayList<>();

    /**
     * Name of the field that will be added to the source entity to refer the most recent target entity
     */
    @XmlAttribute
    @ApiModelProperty("Name of the field that will be added to the source entity to refer the most recent target entity")
    private String sourceNameSingular;

    /**
     * Name of the field that will be added to the source entity to refer every target entities
     */
    @XmlAttribute
    @ApiModelProperty("Name of the field that will be added to the source entity to refer every target entities")
    private String sourceNamePlural;


    /*
     * Name of the field that will be added to the target entity to refer the most recent source entity
     */
    @XmlAttribute
    @ApiModelProperty("Name of the field that will be added to the target entity to refer the most recent source entity")
    private String targetNameSingular;

    /**
     * Name of the field that will be added to the target entity to refer every source entities
     */
    @XmlAttribute
    @ApiModelProperty("Name of the field that will be added to the target entity to refer every source entities")
    private String targetNamePlural;

    @XmlAttribute
    @ApiModelProperty("Name of graphQl type")
    private String graphQlTypeName;
    
    /**
     * Whether this entity is audited or not
     */
	private boolean audited = false;
	
    @JsonIgnore
    private List<String> repositories;
    
    public CustomRelationshipTemplateDto() {
    	super();
    }

    // ------------------------------------------------- Setters and Getters ------------------------------------------

    /**
	 * @return the {@link #repositories}
	 */
	public List<String> getRepositories() {
		return repositories;
	}

	/**
	 * @param repositories the repositories to set
	 */
	public void setRepositories(List<String> repositories) {
		this.repositories = repositories;
	}
	
    public String getGraphQlTypeName() {
        return graphQlTypeName;
    }

    public List<DBStorageType> getAvailableStorages() {
		return availableStorages;
	}

	public void setAvailableStorages(List<DBStorageType> availableStorages) {
		this.availableStorages = availableStorages;
	}

	public void setGraphQlTypeName(String graphQlTypeName) {
        this.graphQlTypeName = graphQlTypeName;
    }

    public String getTargetNameSingular() {
        return targetNameSingular;
    }

    public void setTargetNameSingular(String targetNameSingular) {
        this.targetNameSingular = targetNameSingular;
    }

    public String getTargetNamePlural() {
        return targetNamePlural;
    }

    public void setTargetNamePlural(String targetNamePlural) {
        this.targetNamePlural = targetNamePlural;
    }

    public String getSourceNameSingular() {
        return sourceNameSingular;
    }

    public void setSourceNameSingular(String sourceNameSingular) {
        this.sourceNameSingular = sourceNameSingular;
    }

    public String getSourceNamePlural() {
        return sourceNamePlural;
    }

    public void setSourceNamePlural(String sourceNamePlural) {
        this.sourceNamePlural = sourceNamePlural;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public String getName() {
        return name != null ? name : code;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description != null ? description : code;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public List<CustomFieldTemplateDto> getFields() {
        return fields;
    }


    public void setFields(List<CustomFieldTemplateDto> fields) {
        this.fields = fields;
    }


    public RelationshipDirectionEnum getDirection() {
        return direction;
    }


    public void setDirection(RelationshipDirectionEnum direction) {
        this.direction = direction;
    }


    public String getStartNodeCode() {
        return startNodeCode;
    }


    public void setStartNodeCode(String startNodeCode) {
        this.startNodeCode = startNodeCode;
    }


    public String getEndNodeCode() {
        return endNodeCode;
    }


    public void setEndNodeCode(String endNodeCode) {
        this.endNodeCode = endNodeCode;
    }


    public List<String> getStartNodeKeys() {
        return startNodeKeys;
    }


    public void setStartNodeKeys(List<String> startNodeKeys) {
        this.startNodeKeys = startNodeKeys;
    }


    public List<String> getEndNodeKeys() {
        return endNodeKeys;
    }


    public void setEndNodeKeys(List<String> endNodeKeys) {
        this.endNodeKeys = endNodeKeys;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public void setUnique(boolean unique) {
        isUnique = unique;
    }

    /**
     * Convert CustomRelationshipTemplate instance to CustomRelationshipTemplateDto object including the fields and actions
     *
     * @param crt       CustomRelationshipTemplate object to convert
     * @param crtFields Fields (CustomFieldTemplate) that are part of CustomRelationshipTemplate
     * @return A CustomRelationshipTemplateDto object with fields set
     */
    public static CustomRelationshipTemplateDto toDTO(CustomRelationshipTemplate crt, Collection<CustomFieldTemplate> crtFields) {
        CustomRelationshipTemplateDto dto = new CustomRelationshipTemplateDto();
        dto.setCode(crt.getCode());
        dto.setName(crt.getName());
        dto.setDescription(crt.getDescription());
        dto.setUnique(crt.isUnique());
        dto.setSourceNamePlural(crt.getSourceNamePlural());
        dto.setSourceNameSingular(crt.getSourceNameSingular());
        dto.setTargetNamePlural(crt.getTargetNamePlural());
        dto.setTargetNameSingular(crt.getTargetNameSingular());
        dto.setGraphQlTypeName(crt.getGraphQlTypeName());
        if (crtFields != null) {
            List<CustomFieldTemplateDto> fields = new ArrayList<>();
            for (CustomFieldTemplate cft : crtFields) {
                fields.add(new CustomFieldTemplateDto(cft));
            }
            dto.setFields(fields);
        }
        dto.setAvailableStorages(crt.getAvailableStorages());
        dto.setStartNodeCode(crt.getStartNode().getCode());
        dto.setEndNodeCode(crt.getEndNode().getCode());
        dto.setAudited(crt.isAudited());
        return dto;
    }

    /**
     * Convert CustomRelationshipTemplateDto to a CustomRelationshipTemplate instance. Note: does not convert custom fields that are part of DTO
     *
     * @param dto         CustomRelationshipTemplateDto object to convert
     * @param crtToUpdate CustomRelationshipTemplate to update with values from dto, or if null create a new one
     * @return A new or updated CustomRelationshipTemplate instance
     */
    public static CustomRelationshipTemplate fromDTO(CustomRelationshipTemplateDto dto, CustomRelationshipTemplate crtToUpdate) {
        CustomRelationshipTemplate crt = new CustomRelationshipTemplate();
        if (crtToUpdate != null) {
            crt = crtToUpdate;
        }
        
        if(dto.getStartNodeCode() == null) {
        	throw new IllegalArgumentException("Start node can't be null");
        }
        
        if(dto.getEndNodeCode() == null) {
        	throw new IllegalArgumentException("End node can't be null");
        }
        
        crt.setCode(dto.getCode());
        crt.setName(dto.getName());
        crt.setDescription(dto.getDescription());
        crt.setDirection(dto.getDirection());
        crt.setUnique(dto.isUnique());
        crt.setSourceNamePlural(dto.getSourceNamePlural());
        crt.setSourceNameSingular(dto.getSourceNameSingular());
        crt.setTargetNamePlural(dto.getTargetNamePlural());
        crt.setTargetNameSingular(dto.getTargetNameSingular());
        crt.setGraphQlTypeName(dto.getGraphQlTypeName());
        crt.setAvailableStorages(dto.getAvailableStorages());
        return crt;
    }

    @Override
    public String toString() {
        return "CustomRelationshipTemplateDto [code=" + code + ", name=" + name + ", description=" + description + ", fields=" + fields + "]";
    }

	public boolean isAudited() {
		return audited;
	}

	public void setAudited(boolean audited) {
		this.audited = audited;
	}

}

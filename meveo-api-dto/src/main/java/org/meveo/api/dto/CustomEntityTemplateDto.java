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

import org.meveo.model.crm.CustomEntityTemplateUniqueConstraint;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.PrimitiveTypeEnum;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.customEntities.CustomEntityTemplate;


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

    /** Additional labels */
    @XmlAttribute()
    private List<String> labels;

    @XmlElement()
	private String superTemplate;

    /** The name. */
    @XmlAttribute(required = true)
    private String name;

    /** The description. */
    @XmlAttribute()
    private String description;

    /** Whether the CET is an primitiveEntity. */
    @XmlAttribute()
    private boolean primitiveEntity = false;

    /** Type of the primitiveEntity */
    @XmlAttribute(required = false)
    private PrimitiveTypeEnum primitiveType;

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

    /** The actions. */
    @XmlElementWrapper(name = "uniqueConstraints")
    @XmlElement(name = "uniqueConstraint")
    private List<CustomEntityTemplateUniqueConstraintDto> uniqueConstraints;

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

	public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public boolean isPrimitiveEntity() {
        return primitiveEntity;
    }

    public void setPrimitiveEntity(boolean primitiveEntity) {
        this.primitiveEntity = primitiveEntity;
    }

    public PrimitiveTypeEnum getPrimitiveType() {
        return primitiveType;
    }

    public void setPrimitiveType(PrimitiveTypeEnum primitiveType) {
        this.primitiveType = primitiveType;
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

    /**
     * Gets the unique constraints.
     *
     * @return the unique constraints
     */
    public List<CustomEntityTemplateUniqueConstraintDto> getUniqueConstraints() {
        return uniqueConstraints;
    }

    /**
     * Sets the unique constraints.
     *
     * @param uniqueConstraints the new unique constraints
     */
    public void setUniqueConstraints(List<CustomEntityTemplateUniqueConstraintDto> uniqueConstraints) {
        this.uniqueConstraints = uniqueConstraints;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CustomEntityTemplateDto [code=" + code + ", name=" + name + ", description=" + description + ", fields=" + fields + "]";
    }

}

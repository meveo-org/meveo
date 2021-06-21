package org.meveo.api.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.meveo.model.crm.EntityReferenceWrapper;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a custom field value type - reference to an Meveo entity
 * identified by a classname and code. In case a class is a generic Custom
 * Entity Template a classnameCode is required to identify a concrete custom
 * entity template by its code
 * 
 * @author Andrius Karpavicius
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.8.0
 **/
@XmlRootElement(name = "EntityReference")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class EntityReferenceDto implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4639754869992269238L;

	/** Classname of an entity. */
	@XmlAttribute(required = true)
	@ApiModelProperty(required = true, value = "Classname of this entity")
	private String classname;

	/**
	 * Custom entity template code - applicable and required when reference is to
	 * Custom Entity Template type.
	 */
	@XmlAttribute(required = false)
	@ApiModelProperty("Classname code of this entity")
	private String classnameCode;

	/** Entity code. */
	@XmlAttribute(required = true)
	@ApiModelProperty(required = true, value = "Code of this entity")
	private String code;
	
	/**
	 * Entity id
	 */
	@XmlAttribute(required = false)
	@ApiModelProperty(required = true, value = "Id of this entity")
	private Long id;

	/**
	 * Instantiates a new entity reference dto.
	 */
	public EntityReferenceDto() {

	}

	/**
	 * Instantiates a new entity reference dto.
	 *
	 * @param entityReferenceWrapper the EntityReferenceWrapper
	 */
	public EntityReferenceDto(EntityReferenceWrapper entityReferenceWrapper) {
		classname = entityReferenceWrapper.getClassname();
		classnameCode = entityReferenceWrapper.getClassnameCode();
		code = entityReferenceWrapper.getCode();
		id = entityReferenceWrapper.getId();
	}

	/**
	 * From DTO.
	 *
	 * @return the entity reference wrapper
	 */
	public EntityReferenceWrapper fromDTO() {
		if (isEmpty()) {
			return null;
		}
		var wrapper = new EntityReferenceWrapper(classname, classnameCode, code, id);
		wrapper.setUuid(code);
		return wrapper;
	}

	/**
	 * Gets the classname.
	 *
	 * @return the classname
	 */
	public String getClassname() {
		return classname;
	}

	/**
	 * Sets the classname.
	 *
	 * @param classname the new classname
	 */
	public void setClassname(String classname) {
		this.classname = classname;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("EntityReferenceDto [classname=%s, classnameCode=%s, code=%s]", classname, classnameCode, code);
	}

	/**
	 * Is value empty.
	 *
	 * @return True if classname or code are empty
	 */
	public boolean isEmpty() {
		return (StringUtils.isBlank(classname) && StringUtils.isBlank(classnameCode)) || StringUtils.isBlank(code);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the {@link #classnameCode}
	 */
	public String getClassnameCode() {
		return classnameCode;
	}

	/**
	 * @param classnameCode the classnameCode to set
	 */
	public void setClassnameCode(String classnameCode) {
		this.classnameCode = classnameCode;
	}
	
}
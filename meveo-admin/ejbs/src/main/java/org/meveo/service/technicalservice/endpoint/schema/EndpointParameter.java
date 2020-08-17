package org.meveo.service.technicalservice.endpoint.schema;

import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.util.ClassUtils;

/**
 * Represents an endpoint parameter. It can be the parameter of the get request
 * or body.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.9.0
 * @version 6.9.0
 */
public class EndpointParameter {

	/**
	 * Id of the parameter.
	 */
	private String id;

	/**
	 * Name of the parameter. This is use as the node value.
	 */
	private String name;

	/**
	 * Type of the parameter. Check {@link ClassUtils}.
	 */
	private String type;

	/**
	 * Description of this parameter.
	 */
	private String description;

	/**
	 * Whether this parameter is required.
	 */
	private boolean required;

	/**
	 * Default value to be use.
	 */
	private String defaultValue;

	/**
	 * If this parameter is mapped to a cet then this variable is set.
	 * 
	 * @see {@link CustomEntityTemplate}.
	 */
	private CustomEntityTemplate cet;

	/**
	 * Retrieves the name of this parameter.
	 * 
	 * @return parameter name
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retrieves the type of this parameter.
	 * 
	 * @return parameter type
	 */
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Retrieves the description of this parameter.
	 * 
	 * @return parameter description
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Checks whether this parameter is required.
	 * 
	 * @return true if required, otherwise false
	 */
	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * Retrieves the default value of this parameter.
	 * 
	 * @return parameter default value
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Retrieves the id of this parameter.
	 * 
	 * @return parameter id
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * If this parameter is an object of type cet, then return the object.
	 * 
	 * @return the custom entity template
	 * @see {@linkplain CustomEntityTemplate}
	 */
	public CustomEntityTemplate getCet() {
		return cet;
	}

	public void setCet(CustomEntityTemplate cet) {
		this.cet = cet;
	}
}

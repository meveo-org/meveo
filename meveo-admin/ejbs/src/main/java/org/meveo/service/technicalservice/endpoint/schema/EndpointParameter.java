package org.meveo.service.technicalservice.endpoint.schema;

import org.meveo.model.customEntities.CustomEntityTemplate;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.9.0
 * @version 6.9.0
 */
public class EndpointParameter {

	private String id;
	private String name;
	private String type;
	private String description;
	private boolean required;
	private String defaultValue;
	private CustomEntityTemplate cet;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CustomEntityTemplate getCet() {
		return cet;
	}

	public void setCet(CustomEntityTemplate cet) {
		this.cet = cet;
	}
}

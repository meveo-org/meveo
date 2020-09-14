package org.meveo.api.dto;

import javax.validation.constraints.NotNull;

import org.meveo.api.dto.technicalservice.InputOutputDescription;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * The process entity description dto.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
public class ProcessEntityDescription extends InputOutputDescription {

	public static final String ENTITY_DESCRIPTION = "EntityDescription";

	/**
	 * The name of this process entity
	 */
	@JsonProperty(required = true)
	@NotNull
	@ApiModelProperty(required = true, value = "The name of this process entity")
	private String name;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}

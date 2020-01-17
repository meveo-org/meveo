package org.meveo.api.dto;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Dto use when persisting a list of custom entity instance.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel
public class PersistenceDto {

	/**
	 * Type of the object to persist : 'entity' or 'relation'.
	 */
	@ApiModelProperty("Type of the object to persist")
	private String discriminator = "entity";

	/**
	 * Source of relation. Null if the object is an entity.
	 */
	@ApiModelProperty("Source of relation")
	private String source;

	/**
	 * Target of relation. Null if the object is an entity.
	 */
	@ApiModelProperty("Target of relation")
	private String target;

	/**
	 * Type of the relation or entity. References the CET code for an entity and the
	 * CRT code for a relation.
	 */
	@ApiModelProperty("Type of the relation or entity")
	private String type;

	/**
	 * Unique name of the entity in the given context. Used to build the persistence
	 * graph. Null if object is a relation.
	 */
	@ApiModelProperty("Unique name of the entity in the given context")
	private String name;

	/**
	 * Properties to attach to the object, key references the CFT code. Can be
	 * single-valuated or multi-valuated (defined in ontology).
	 */
	@ApiModelProperty("Map of properties to attach to the object, key references the CFT code")
	private Map<String, Object> properties = new HashMap<>();

	public String getDiscriminator() {
		return discriminator;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
		this.discriminator = "relation";
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
		this.discriminator = "relation";
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
}

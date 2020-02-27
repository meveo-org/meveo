package org.meveo.api.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.model.crm.CustomEntityTemplateUniqueConstraint;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "CustomEntityTemplateUniqueConstraint")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomEntityTemplateUniqueConstraintDto {

	/**
	 * The code of this dto
	 */
	@XmlAttribute(required = true)
	@ApiModelProperty(required = true, value = "The code of this dto")
	private String code;

	/**
	 * Applies this template to this entity's code
	 */
	@XmlAttribute()
	@ApiModelProperty("Applies this template to this entity's code")
	protected String appliesTo;

	/**
	 * Desscription of this dto
	 */
	@XmlAttribute()
	@ApiModelProperty(required = true, value = "Desscription of this dto")
	private String description;

	/**
	 * Cypher query for this entity
	 */
	@XmlAttribute(required = true)
	@ApiModelProperty(value = "Cypher query for this entity", required = true)
	private String cypherQuery;

	/**
	 * Trust score for this entity
	 */
	@XmlAttribute()
	@ApiModelProperty("Trust store for this entity")
	private Integer trustScore;

	/**
	 * Applies this unique constraint using this EL expression.
	 */
	@XmlElement()
	@ApiModelProperty("Applies this unique constraint using this EL expression.")
	private String applicableOnEl;

	/**
	 * Order value use when sorting a list of this entity
	 */
	@XmlAttribute(required = true)
	@ApiModelProperty(value = "Order value use when sorting a list of this entity", required = true)
	private Integer order;

	public CustomEntityTemplateUniqueConstraintDto() {
		super();
	}

	public CustomEntityTemplateUniqueConstraintDto(CustomEntityTemplateUniqueConstraint cetUniqueConstraint) {
		super();

		this.code = cetUniqueConstraint.getCode();
		this.description = cetUniqueConstraint.getDescription();
		this.cypherQuery = cetUniqueConstraint.getCypherQuery();
		this.trustScore = cetUniqueConstraint.getTrustScore();
		this.applicableOnEl = cetUniqueConstraint.getApplicableOnEl();
		this.order = cetUniqueConstraint.getPosition();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getAppliesTo() {
		return appliesTo;
	}

	public void setAppliesTo(String appliesTo) {
		this.appliesTo = appliesTo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCypherQuery() {
		return cypherQuery;
	}

	public void setCypherQuery(String cypherQuery) {
		this.cypherQuery = cypherQuery;
	}

	public Integer getTrustScore() {
		return trustScore;
	}

	public void setTrustScore(Integer trustScore) {
		this.trustScore = trustScore;
	}

	public String getApplicableOnEl() {
		return applicableOnEl;
	}

	public void setApplicableOnEl(String applicableOnEl) {
		this.applicableOnEl = applicableOnEl;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}
}

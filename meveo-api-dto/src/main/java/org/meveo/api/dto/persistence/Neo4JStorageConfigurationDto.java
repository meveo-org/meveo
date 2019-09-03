package org.meveo.api.dto.persistence;

import java.util.ArrayList;
import java.util.List;

import org.meveo.api.dto.CustomEntityTemplateUniqueConstraintDto;
import org.meveo.model.crm.custom.PrimitiveTypeEnum;
import org.meveo.model.customEntities.GraphQLQueryField;

public class Neo4JStorageConfigurationDto {

	/**
	 * Labels to apply to the template.
	 */
	private List<String> labels = new ArrayList<>();

	/**
	 * Whether the CET is primitive. A primitive entity is an entity containing only
	 * one property named "value"
	 */
	private boolean primitiveEntity = false;

	/**
	 * The primitive type, if entity is primitive.
	 */
	private PrimitiveTypeEnum primitiveType;

	/**
	 * Additionnal fields that can be retrieved using graphql engine
	 */
	private List<GraphQLQueryField> graphqlQueryFields;

	/**
	 * Unique constraint to be applied when persisiting custom entities
	 */
	private List<CustomEntityTemplateUniqueConstraintDto> uniqueConstraints = new ArrayList<>();

	public List<CustomEntityTemplateUniqueConstraintDto> getUniqueConstraints() {
		return uniqueConstraints;
	}

	public void setUniqueConstraints(List<CustomEntityTemplateUniqueConstraintDto> uniqueConstraints) {
		if (uniqueConstraints == null) {
			this.uniqueConstraints.clear();
		} else {
			this.uniqueConstraints = uniqueConstraints;
		}
	}

	public List<String> getLabels() {
		return labels;
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

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public List<GraphQLQueryField> getGraphqlQueryFields() {
		return graphqlQueryFields;
	}

	public void setGraphqlQueryFields(List<GraphQLQueryField> graphqlQueryFields) {
		this.graphqlQueryFields = graphqlQueryFields;
	}

}

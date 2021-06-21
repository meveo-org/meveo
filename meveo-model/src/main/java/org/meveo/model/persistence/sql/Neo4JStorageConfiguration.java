package org.meveo.model.persistence.sql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

import org.hibernate.annotations.Type;
import org.meveo.model.crm.CustomEntityTemplateUniqueConstraint;
import org.meveo.model.crm.custom.PrimitiveTypeEnum;
import org.meveo.model.customEntities.GraphQLQueryField;
import org.meveo.model.customEntities.Mutation;
import org.meveo.model.persistence.JsonTypes;

@Embeddable
public class Neo4JStorageConfiguration implements Serializable {

	private static final long serialVersionUID = 7692492509071553409L;

	/**
	 * Labels to apply to the template.
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "cet_labels", joinColumns = { @JoinColumn(name = "cet_id") })
	@Column(name = "label")
	private List<String> labels = new ArrayList<>();
	
	/**
	 * Whether the CET is primitive. A primitive entity is an entity containing only
	 * one property named "value"
	 */
	@Column(name = "primitive_entity")
	@Type(type = "numeric_boolean")
	private boolean primitiveEntity;

	/**
	 * The primitive type, if entity is primitive.
	 */
	@Column(name = "primitive_type")
	@Enumerated(EnumType.STRING)
	private PrimitiveTypeEnum primitiveType;
	
	/**
	 * Additionnal fields that can be retrieved using graphql engine
	 */
	@Column(name = "graphql_query_fields", columnDefinition = "TEXT")
	@Type(type = JsonTypes.JSON_LIST)
	private List<GraphQLQueryField> graphqlQueryFields;

	@Column(name = "mutations", columnDefinition = "TEXT")
	@Type(type = JsonTypes.JSON_LIST)
	private List<Mutation> mutations;

	@Column(name = "max_value")
	private Long maxValue;

	/**
	 * Unique constraint to be applied when persisiting custom entities
	 */
    @OneToMany(mappedBy = "customEntityTemplate", fetch = FetchType.EAGER, orphanRemoval=true, cascade = CascadeType.ALL)
	@OrderColumn(name = "position")
	private List<CustomEntityTemplateUniqueConstraint> uniqueConstraints = new ArrayList<>();
	
	public List<CustomEntityTemplateUniqueConstraint> getUniqueConstraints() {
		return uniqueConstraints;
	}

	public void setUniqueConstraints(List<CustomEntityTemplateUniqueConstraint> uniqueConstraints) {
		if(uniqueConstraints == null){
			this.uniqueConstraints.clear();
		}else{
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

	public List<Mutation> getMutations() {
		return mutations;
	}

	public void setMutations(List<Mutation> mutations) {
		this.mutations = mutations;
	}

	public Long getMaxValue() { return maxValue; }

	public void setMaxValue(Long maxValue) { this.maxValue = maxValue; }
}

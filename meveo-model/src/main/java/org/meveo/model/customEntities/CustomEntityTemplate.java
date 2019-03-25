package org.meveo.model.customEntities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.ModuleItem;
import org.meveo.model.crm.CustomEntityTemplateUniqueConstraint;
import org.meveo.model.crm.custom.PrimitiveTypeEnum;
import org.meveo.model.scripts.ScriptInstance;

@Entity
@ModuleItem
@Cacheable
@ExportIdentifier({ "code" })
@Table(name = "cust_cet", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "cust_cet_seq"), })
@NamedQueries({
		@NamedQuery(name = "CustomEntityTemplate.getCETForCache", query = "SELECT cet from CustomEntityTemplate cet where cet.disabled=false order by cet.name "),
		@NamedQuery(name = "CustomEntityTemplate.getCETForConfiguration", query = "SELECT DISTINCT cet from CustomEntityTemplate cet join fetch cet.entityReference left join fetch cet.subTemplates where cet.disabled=false order by cet.name"),
		@NamedQuery(name = "CustomEntityTemplate.PrimitiveType", query = "SELECT cet.primitiveType FROM CustomEntityTemplate cet WHERE code = :code")
})
public class CustomEntityTemplate extends BusinessEntity implements Comparable<CustomEntityTemplate> {

	private static final long serialVersionUID = 8281478284763353310L;

	public static String CFT_PREFIX = "CE";

	@Column(name = "name", length = 100, nullable = false)
	@Size(max = 100)
	@NotNull
	private String name;

	/**
	 * Labels to apply to the template.
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "cet_labels", joinColumns = { @JoinColumn(name = "cet_id") })
	@Column(name = "label")
	private List<String> labels = new ArrayList<>();

	/**
	 * Template that current template inherits from
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "super_template_id")
	private CustomEntityTemplate superTemplate;

	@OneToMany(mappedBy = "superTemplate", fetch = FetchType.LAZY)
	private List<CustomEntityTemplate> subTemplates;

	/**
	 * Unique constraint to be applied when persisiting custom entities
	 */
    @OneToMany(mappedBy = "customEntityTemplate", fetch = FetchType.EAGER, orphanRemoval=true, cascade = CascadeType.ALL)
	@OrderColumn(name = "position")
	private List<CustomEntityTemplateUniqueConstraint> uniqueConstraints = new ArrayList<>();

	@OneToOne(mappedBy = "customEntityTemplate", fetch = FetchType.LAZY)
	private CustomEntityReference entityReference;

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
	 * Script to execute before persisting the entity
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pre_persist_script")
	private ScriptInstance prePersistScript;

	/**
	 * Custom Entity Category
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "custom_entity_category")
	private CustomEntityCategory customEntityCategory;

	/**
	 * Additionnal fields that can be retrieved using graphql engine
	 */
	@Column(name = "graphql_query_fields", columnDefinition = "TEXT")
	@Type(type = "jsonList")
	private List<GraphQLQueryField> graphqlQueryFields;

	public ScriptInstance getPrePersistScript() {
		return prePersistScript;
	}

	public void setPrePersistScript(ScriptInstance prePersistScript) {
		this.prePersistScript = prePersistScript;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAppliesTo() {
		return CFT_PREFIX + "_" + getCode();
	}

	public static String getAppliesTo(String code) {
		return CFT_PREFIX + "_" + code;
	}

	public String getReadPermission() {
		return CustomEntityTemplate.getReadPermission(code);
	}

	public String getModifyPermission() {
		return CustomEntityTemplate.getModifyPermission(code);
	}

	@Override
	public int compareTo(CustomEntityTemplate cet1) {
		return StringUtils.compare(name, cet1.getName());
	}

	public static String getReadPermission(String code) {
		return "CE_" + code + "-read";
	}

	public static String getModifyPermission(String code) {
		return "CE_" + code + "-modify";
	}

	public static String getCodeFromAppliesTo(String appliesTo) {
		return appliesTo.substring(3);
	}

	public CustomEntityTemplate getSuperTemplate() {
		return superTemplate;
	}

	public void setSuperTemplate(CustomEntityTemplate superTemplate) {
		this.superTemplate = superTemplate;
	}

	public List<CustomEntityTemplate> getSubTemplates() {
		return subTemplates;
	}

	public void setSubTemplates(List<CustomEntityTemplate> subTemplates) {
		this.subTemplates = subTemplates;
	}

	public CustomEntityReference getEntityReference() {
		return entityReference;
	}

	public void setEntityReference(CustomEntityReference entityReference) {
		this.entityReference = entityReference;
	}

	public CustomEntityCategory getCustomEntityCategory() {
		return customEntityCategory;
	}

	public void setCustomEntityCategory(CustomEntityCategory customEntityCategory) {
		this.customEntityCategory = customEntityCategory;
	}

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

	public List<GraphQLQueryField> getGraphqlQueryFields() {
		return graphqlQueryFields;
	}

	public void setGraphqlQueryFields(List<GraphQLQueryField> graphqlQueryFields) {
		this.graphqlQueryFields = graphqlQueryFields;
	}
}

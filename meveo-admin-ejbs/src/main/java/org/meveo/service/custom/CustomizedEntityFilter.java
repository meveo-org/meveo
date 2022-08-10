package org.meveo.service.custom;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.4.0
 */
public class CustomizedEntityFilter {

	private String entityName;
	private boolean customEntityTemplatesOnly;
	private boolean includeNonManagedEntities;
	private boolean includeParentClassesOnly;
	private String sortBy;
	private String sortOrder;
	private boolean includeRelationships;
	
	/**
	 * 0 - default
	 * 1 - true
	 * 2 - no
	 */
	private String primitiveEntity = "0";
	private Long cecId;

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public boolean isCustomEntityTemplatesOnly() {
		return customEntityTemplatesOnly;
	}

	public void setCustomEntityTemplatesOnly(boolean customEntityTemplatesOnly) {
		this.customEntityTemplatesOnly = customEntityTemplatesOnly;
	}

	public boolean isIncludeNonManagedEntities() {
		return includeNonManagedEntities;
	}

	public void setIncludeNonManagedEntities(boolean includeNonManagedEntities) {
		this.includeNonManagedEntities = includeNonManagedEntities;
	}

	public boolean isIncludeParentClassesOnly() {
		return includeParentClassesOnly;
	}

	public void setIncludeParentClassesOnly(boolean includeParentClassesOnly) {
		this.includeParentClassesOnly = includeParentClassesOnly;
	}

	public boolean isIncludeRelationships() {
		return includeRelationships;
	}

	public void setIncludeRelationships(boolean includeRelationships) {
		this.includeRelationships = includeRelationships;
	}

	public String getSortBy() {
		return sortBy;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public Long getCecId() {
		return cecId;
	}

	public void setCecId(Long cecId) {
		this.cecId = cecId;
	}

	public String getPrimitiveEntity() {
		return primitiveEntity;
	}

	public void setPrimitiveEntity(String primitiveEntity) {
		this.primitiveEntity = primitiveEntity;
	}
}

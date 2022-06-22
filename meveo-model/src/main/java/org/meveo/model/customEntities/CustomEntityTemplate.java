/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.model.customEntities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.BusinessEntity;
import org.meveo.model.CustomEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.ModuleItem;
import org.meveo.model.ModuleItemOrder;
import org.meveo.model.ObservableEntity;
import org.meveo.model.annotation.ImportOrder;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.sql.Neo4JStorageConfiguration;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.storage.Repository;

/**
 * The Class CustomEntityTemplate.
 *
 * @author Clément Bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@Entity
@ModuleItem(value = "CustomEntityTemplate", path ="customEntityTemplates")
@ModuleItemOrder(10)
@Cacheable
@ExportIdentifier({ "code" })
@Table(name = "cust_cet", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
		@Parameter(name = "sequence_name", value = "cust_cet_seq"), })
@NamedQueries({ @NamedQuery(name = "CustomEntityTemplate.getCETForCache", query = "SELECT cet from CustomEntityTemplate cet JOIN FETCH cet.availableStorages where cet.disabled=false order by cet.name "),
		@NamedQuery(name = "CustomEntityTemplate.getCETForConfiguration", query = "SELECT DISTINCT cet from CustomEntityTemplate cet join fetch cet.entityReference left join fetch cet.subTemplates where cet.disabled=false order by cet.name"),
		@NamedQuery(name = "CustomEntityTemplate.PrimitiveType", query = "SELECT cet.neo4JStorageConfiguration.primitiveType FROM CustomEntityTemplate cet WHERE code = :code"),
		@NamedQuery(name = "CustomEntityTemplate.getCETsByCategoryId", query = "SELECT cet FROM CustomEntityTemplate cet WHERE cet.customEntityCategory.id = :id"),
		@NamedQuery(name = "CustomEntityTemplate.ReSetCategoryEmptyByCategoryId", query = "UPDATE CustomEntityTemplate cet SET cet.customEntityCategory=NULL WHERE cet.customEntityCategory.id = :id") })
@ObservableEntity
@ImportOrder(2)
public class CustomEntityTemplate extends BusinessEntity implements Comparable<CustomEntityTemplate>, CustomModelObject {

	private static final long serialVersionUID = 8281478284763353310L;
	
	public static final String AUDIT_PREFIX = "audit_";

	/**
	 * Prefix for CustomEntityTemplate. If this prefix is changed, the hard-coded
	 * value in exportImportTemplates.xml must be updated too.
	 */
	public static String CFT_PREFIX = "CE";

	@Column(name = "name", length = 100, nullable = false)
	@Size(max = 100)
	@NotNull
	private String name;

	@Embedded
	private SQLStorageConfiguration sqlStorageConfiguration = new SQLStorageConfiguration();

	@Embedded
	private Neo4JStorageConfiguration neo4JStorageConfiguration = new Neo4JStorageConfiguration();

	/**
	 * Template that current template inherits from
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "super_template_id")
	private CustomEntityTemplate superTemplate;

	@OneToMany(mappedBy = "superTemplate", fetch = FetchType.LAZY)
	private List<CustomEntityTemplate> subTemplates;

	@OneToOne(mappedBy = "customEntityTemplate", fetch = FetchType.LAZY)
	private CustomEntityReference entityReference;

	/**
	 * Script to execute before persisting the entity
	 * @deprecated Will be removed in future releases
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pre_persist_script")
	private ScriptInstance prePersistScript;	//TODO: Remove the property

	/**
	 * Custom Entity Category
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "custom_entity_category")
	private CustomEntityCategory customEntityCategory;

	/**
	 * List of storages where the custom fields can be stored
	 */
	// @Column(name = "available_storages", columnDefinition = "TEXT")
	// @Type(type = JsonTypes.JSON_LIST)
	@ManyToMany
	@JoinTable(name = "cet_db_storage", inverseJoinColumns = @JoinColumn(name = "db_storage_code"), joinColumns = @JoinColumn(name = "cet_id"))
	private List<DBStorageType> availableStorages = new ArrayList<>();
	
	@Type(type = "numeric_boolean")
	@Column(name = "audited")
	private boolean audited = false;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cr_ev_list_script")
	private ScriptInstance crudEventListenerScript;
	
	/**
	 * Function used to determine whether an instance of the related {@link CustomEntity} is equal to an other
	 */
	@Column(name = "is_equal_fn", columnDefinition = "TEXT")
	private String isEqualFn;
	
	@Transient
	private transient CrudEventListenerScript<CustomEntity> crudEventListener;

	@Transient
	private boolean hasReferenceJpaEntity = false;
	
	@Transient
	private boolean isInDraft = false;
	
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "cet_storage_repository",
        joinColumns = @JoinColumn(name = "cet_id"),
        inverseJoinColumns = @JoinColumn(name = "repo_id")
    )
    private List<Repository> repositories = new ArrayList<>();
	
	/**
	 * @return the {@link #repositories}
	 */
	public List<Repository> getRepositories() {
		if (this.repositories == null) {
			this.repositories = new ArrayList<>();
		}
		return this.repositories;
	}

	/**
	 * @param repositories the repositories to set
	 */
	public void setRepositories(List<Repository> repositories) {
		this.repositories = repositories;
	}

	/**
	 * @return the {@link #isEqualFn}
	 */
	public String getIsEqualFn() {
		return isEqualFn;
	}

	/**
	 * @param isEqualFn the isEqualFn to set
	 */
	public void setIsEqualFn(String isEqualFn) {
		this.isEqualFn = isEqualFn;
	}

	/**
	 * @return the {@link #crudEventListenerScript}
	 */
	public ScriptInstance getCrudEventListenerScript() {
		return crudEventListenerScript;
	}

	/**
	 * @param crudEventListenerScript the crudEventListenerScript to set
	 */
	public void setCrudEventListenerScript(ScriptInstance crudEventListenerScript) {
		this.crudEventListenerScript = crudEventListenerScript;
	}

	/**
	 * @return the {@link #crudEventListener}
	 */
	public CrudEventListenerScript<CustomEntity> getCrudEventListener() {
		return crudEventListener;
	}

	/**
	 * @param crudEventListener the crudEventListener to set
	 */
	public void setCrudEventListener(CrudEventListenerScript<CustomEntity> crudEventListener) {
		this.crudEventListener = crudEventListener;
	}

	/**
	 * Sets the custom Entity Category.
	 *
	 * @param customEntityCategory the new custom Entity Category
	 */
	public void setCustomEntityCategory(CustomEntityCategory customEntityCategory) {
		this.customEntityCategory = customEntityCategory;
	}

	public boolean isInDraft() {
		return isInDraft;
	}

	public void setInDraft(boolean isInDraft) {
		this.isInDraft = isInDraft;
	}

	/**
	 * Gets the custom Entity Category.
	 *
	 * @return the custom Entity Category
	 */
	public CustomEntityCategory getCustomEntityCategory() {
		return customEntityCategory;
	}

	/**
	 * Instantiates it if null.
	 *
	 * @return the {@link SQLStorageConfiguration}
	 */
	public SQLStorageConfiguration getSqlStorageConfigurationNullSafe() {

		if (sqlStorageConfiguration == null) {
			sqlStorageConfiguration = new SQLStorageConfiguration();
		}

		return sqlStorageConfiguration;
	}

	/**
	 * Gets the sql storage configuration.
	 *
	 * @return the sql storage configuration
	 */
	public SQLStorageConfiguration getSqlStorageConfiguration() {
		if (availableStorages != null && availableStorages.contains(DBStorageType.SQL)) {
			return sqlStorageConfiguration;
		}

		return null;
	}

	/**
	 * Gets the neo 4 J storage configuration.
	 *
	 * @return the neo 4 J storage configuration
	 */
	public Neo4JStorageConfiguration getNeo4JStorageConfiguration() {
		if (availableStorages != null && availableStorages.contains(DBStorageType.NEO4J)) {
			return neo4JStorageConfiguration;
		}

		return null;
	}

	/**
	 * Sets the neo 4 J storage configuration.
	 *
	 * @param neo4jStorageConfiguration the new neo 4 J storage configuration
	 */
	public void setNeo4JStorageConfiguration(Neo4JStorageConfiguration neo4jStorageConfiguration) {
		neo4JStorageConfiguration = neo4jStorageConfiguration;
	}

	/**
	 * Sets the sql storage configuration.
	 *
	 * @param sqlStorageConfiguration the new sql storage configuration
	 */
	public void setSqlStorageConfiguration(SQLStorageConfiguration sqlStorageConfiguration) {
		this.sqlStorageConfiguration = sqlStorageConfiguration;
	}

	/**
	 * Gets the list of storages where the custom fields can be stored.
	 *
	 * @return the list of storages where the custom fields can be stored
	 */
	public List<DBStorageType> getAvailableStorages() {
		return availableStorages;
	}

	/**
	 * Sets the list of storages where the custom fields can be stored.
	 *
	 * @param availableStorages the new list of storages where the custom fields can be stored
	 */
	public void setAvailableStorages(List<DBStorageType> availableStorages) {
		this.availableStorages = availableStorages;
	}

	/**
	 * Gets the script to execute before persisting the entity.
	 *
	 * @deprecated Will be removed in future releases
	 * @return the script to execute before persisting the entity
	 */
	@Deprecated
	public ScriptInstance getPrePersistScript() {
		return prePersistScript;
	}

	/**
	 * Sets the script to execute before persisting the entity.
	 *
	 * @deprecated Will be removed in future releases
	 * @param prePersistScript the new script to execute before persisting the entity
	 */
	@Deprecated
	public void setPrePersistScript(ScriptInstance prePersistScript) {
		this.prePersistScript = prePersistScript;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getAppliesTo() {
		return CFT_PREFIX + "_" + getCode();
	}

	/**
	 * Gets the applies to.
	 *
	 * @param code the code
	 * @return the applies to
	 */
	public static String getAppliesTo(String code) {
		return CFT_PREFIX + "_" + code;
	}

	/**
	 * Gets the read permission.
	 *
	 * @return the read permission
	 */
	public String getReadPermission() {
		return CustomEntityTemplate.getReadPermission(code);
	}

	/**
	 * Gets the modify permission.
	 *
	 * @return the modify permission
	 */
	public String getModifyPermission() {
		return CustomEntityTemplate.getModifyPermission(code);
	}
	
	/**
	 * Gets the decrypt permission.
	 *
	 * @return the decrypt permission
	 */
	public String getDecrpytPermission() {
		return CustomEntityTemplate.getDecryptPermission(code);
	}

	@Override
	public int compareTo(CustomEntityTemplate cet1) {
		return StringUtils.compare(name, cet1.getName());
	}

	/**
	 * Gets the read permission.
	 *
	 * @param code the code
	 * @return the read permission
	 */
	public static String getReadPermission(String code) {
		return "CE_" + code + "-read";
	}

	/**
	 * Gets the modify permission.
	 *
	 * @param code the code
	 * @return the modify permission
	 */
	public static String getModifyPermission(String code) {
		return "CE_" + code + "-modify";
	}
	
	/**
	 * Gets the decrypt permission.
	 *
	 * @param code the code
	 * @return the decrypt permission
	 */
	public static String getDecryptPermission(String code) {
		return "CE_" + code + "-decrypt";
	}

	/**
	 * Gets the code from applies to.
	 *
	 * @param appliesTo the applies to
	 * @return the code from applies to
	 */
	public static String getCodeFromAppliesTo(String appliesTo) {
		if (appliesTo == null)
			return null;

		if (!appliesTo.startsWith("CE_")) {
			return null;
		}

		return appliesTo.substring(3);
	}

	/**
	 * Gets the template that current template inherits from.
	 *
	 * @return the template that current template inherits from
	 */
	public CustomEntityTemplate getSuperTemplate() {
		return superTemplate;
	}
	
	/**
	 * @param storage Storage type to check
	 * @return whether the template is stored in this storage
	 */
	public boolean storedIn(DBStorageType storage) {
		if(availableStorages == null) {
			return false;
		}
		return availableStorages.contains(storage);
	}

	/**
	 * Sets the template that current template inherits from.
	 *
	 * @param superTemplate the new template that current template inherits from
	 */
	public void setSuperTemplate(CustomEntityTemplate superTemplate) {
		this.superTemplate = superTemplate;
	}

	/**
	 * Gets the sub templates.
	 *
	 * @return the sub templates
	 */
	public List<CustomEntityTemplate> getSubTemplates() {
		return subTemplates;
	}

	/**
	 * Sets the sub templates.
	 *
	 * @param subTemplates the new sub templates
	 */
	public void setSubTemplates(List<CustomEntityTemplate> subTemplates) {
		this.subTemplates = subTemplates;
	}

	/**
	 * Gets the entity reference.
	 *
	 * @return the entity reference
	 */
	public CustomEntityReference getEntityReference() {
		return entityReference;
	}

	/**
	 * Sets the entity reference.
	 *
	 * @param entityReference the new entity reference
	 */
	public void setEntityReference(CustomEntityReference entityReference) {
		this.entityReference = entityReference;
	}

	/**
	 * /!\ The subTemplates field should have been fetch, will raise an exception
	 * otherwise.
	 *
	 * @return the cet with all of its descendance
	 */
	public List<CustomEntityTemplate> descendance() {
		List<CustomEntityTemplate> descendance = new ArrayList<>();
		descendance.add(this);
		for (CustomEntityTemplate descendant : subTemplates) {
			descendance.addAll(descendant.descendance());
		}
		return descendance;
	}

	/**
	 * Ascendance.
	 *
	 * @return the cet with all of its ascendances
	 */
	public List<CustomEntityTemplate> ascendance() {
		List<CustomEntityTemplate> descendance = new ArrayList<>();
		descendance.add(this);
		if (this.getSuperTemplate() != null) {
			descendance.addAll(this.getSuperTemplate().ascendance());
		}
		return descendance;
	}

	/**
	 * Checks for reference jpa entity.
	 *
	 * @return true, if successful
	 */
	public boolean hasReferenceJpaEntity() {
		return hasReferenceJpaEntity;
	}

	/**
	 * Sets the checks for reference jpa entity.
	 *
	 * @param hasReferenceJpaEntity the new checks for reference jpa entity
	 */
	public void setHasReferenceJpaEntity(boolean hasReferenceJpaEntity) {
		this.hasReferenceJpaEntity = hasReferenceJpaEntity;
	}

	public boolean isStoreAsTable() {
		if (getSqlStorageConfiguration() == null) {
			return false;
		}

		return getSqlStorageConfiguration().isStoreAsTable();
	}	

	public boolean isAudited() {
		return audited;
	}

	public void setAudited(boolean audited) {
		this.audited = audited;
	}

	@Override
	public String getDbTableName() {
		return SQLStorageConfiguration.getDbTablename(this);
	}

}

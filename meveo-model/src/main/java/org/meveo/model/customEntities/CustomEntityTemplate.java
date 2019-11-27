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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.ModuleItem;
import org.meveo.model.ObservableEntity;
import org.meveo.model.annotation.ImportOrder;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.sql.Neo4JStorageConfiguration;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.scripts.ScriptInstance;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Clément Bareth
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @version 6.6.0
 */
@Entity
@ModuleItem("CustomEntityTemplate")
@Cacheable
@ExportIdentifier({ "code" })
@Table(name = "cust_cet", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "cust_cet_seq"), })
@NamedQueries({
		@NamedQuery(name = "CustomEntityTemplate.getCETForCache", query = "SELECT cet from CustomEntityTemplate cet where cet.disabled=false order by cet.name "),
		@NamedQuery(name = "CustomEntityTemplate.getCETForConfiguration", query = "SELECT DISTINCT cet from CustomEntityTemplate cet join fetch cet.entityReference left join fetch cet.subTemplates where cet.disabled=false order by cet.name"),
		@NamedQuery(name = "CustomEntityTemplate.PrimitiveType", query = "SELECT cet.neo4JStorageConfiguration.primitiveType FROM CustomEntityTemplate cet WHERE code = :code"),
		@NamedQuery(name = "CustomEntityTemplate.getCETsByCategoryId", query = "SELECT cet FROM CustomEntityTemplate cet WHERE cet.customEntityCategory.id = :id"),
		@NamedQuery(name = "CustomEntityTemplate.ReSetCategoryEmptyByCategoryId", query = "UPDATE CustomEntityTemplate cet SET cet.customEntityCategory=NULL WHERE cet.customEntityCategory.id = :id")
})
@ObservableEntity
@ImportOrder(2)
public class CustomEntityTemplate extends BusinessEntity implements Comparable<CustomEntityTemplate>, CustomModelObject {

	private static final long serialVersionUID = 8281478284763353310L;

	/**
	 * Prefix for CustomEntityTemplate. If this prefix is changed, the hard-coded value in exportImportTemplates.xml must be updated too.
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
	 * List of storages where the custom fields can be stored
	 */
	@Column(name = "available_storages", columnDefinition = "TEXT")
	@Type(type = "jsonList")
	private List<DBStorageType> availableStorages = new ArrayList<>();

	public SQLStorageConfiguration getSqlStorageConfiguration() {
		if(availableStorages != null && availableStorages.contains(DBStorageType.SQL)) {
			return sqlStorageConfiguration;
		}
		
		return null;
	}

	public Neo4JStorageConfiguration getNeo4JStorageConfiguration() {
		if(availableStorages != null && availableStorages.contains(DBStorageType.NEO4J)) {
			return neo4JStorageConfiguration;
		}
		
		return null;
	}

	public void setNeo4JStorageConfiguration(Neo4JStorageConfiguration neo4jStorageConfiguration) {
		neo4JStorageConfiguration = neo4jStorageConfiguration;
	}




	public void setSqlStorageConfiguration(SQLStorageConfiguration sqlStorageConfiguration) {
		this.sqlStorageConfiguration = sqlStorageConfiguration;
	}

	public List<DBStorageType> getAvailableStorages() {
		return availableStorages;
	}

	public void setAvailableStorages(List<DBStorageType> availableStorages) {
		this.availableStorages = availableStorages;
	}

	public ScriptInstance getPrePersistScript() {
		return prePersistScript;
	}

	public void setPrePersistScript(ScriptInstance prePersistScript) {
		this.prePersistScript = prePersistScript;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
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

	/**
	 * /!\ The subTemplates field should have been fetch, will raise an exception otherwise
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
	 * @return the cet with all of its ascendances
	 */
	public List<CustomEntityTemplate> ascendance() {
		List<CustomEntityTemplate> descendance = new ArrayList<>();
		descendance.add(this);
		if(this.getSuperTemplate() != null) {
			descendance.addAll(this.getSuperTemplate().ascendance());
		}
		return descendance;
	}

	public String getSqlConfigurationCode() {
		
		return sqlStorageConfiguration == null ? null : sqlStorageConfiguration.getSqlConfigurationCode();
	}


}

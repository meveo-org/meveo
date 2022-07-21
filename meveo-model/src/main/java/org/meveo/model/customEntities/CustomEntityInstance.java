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

import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.commons.utils.StringUtils;
import org.meveo.jackson.deserializers.CustomEntityInstanceDeserializer;
import org.meveo.jackson.serializers.CustomEntityInstanceSerializer;
import org.meveo.model.BusinessCFEntity;
import org.meveo.model.CustomFieldEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.ModuleItem;
import org.meveo.model.ModuleItemOrder;
import org.meveo.model.ObservableEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.util.PersistenceUtils;
import org.meveo.model.storage.Repository;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Clément Bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @lastModifiedVersion 6.9.0
 */
@Entity
@ObservableEntity
@Cacheable
@ModuleItem(value = "CustomEntityInstance", path = "customEntityInstances")
@ModuleItemOrder(50)
@CustomFieldEntity(cftCodePrefix = "CE", cftCodeFields = "cetCode")
@ExportIdentifier({ "code", "cetCode" })
@Table(name = "cust_cei", uniqueConstraints = @UniqueConstraint(columnNames = { "code", "cet_code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
		@Parameter(name = "sequence_name", value = "cust_cei_seq"), })
@JsonSerialize(using = CustomEntityInstanceSerializer.class)
@JsonDeserialize(using = CustomEntityInstanceDeserializer.class)
public class CustomEntityInstance extends BusinessCFEntity {

	private static final long serialVersionUID = 8281478284763353310L;

	@Column(name = "cet_code", nullable = false)
	@Size(max = 255)
	@NotNull
	public String cetCode;

	@Column(name = "parent_uuid", updatable = false, length = 60)
	@Size(max = 60)
	public String parentEntityUuid;

	@Transient
	private CustomEntityTemplate cet;
	
	@Transient
	private Map<String, CustomFieldTemplate> fieldTemplates;

	@Transient
	private String tableName;

	@Transient
	private CustomFieldValues cfValuesOld = new CustomFieldValues();
	
	@Transient
	private Repository repository;
	
	/**
	 * @return the {@link #repository}
	 */
	public Repository getRepository() {
		return repository;
	}

	/**
	 * @param repository the repository to set
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (!(obj instanceof CustomEntityInstance)) {
			return false;
		}

		CustomEntityInstance other = (CustomEntityInstance) obj;

		if (getId() != null && other.getId() != null && getId().equals(other.getId())) {
			return true;
		}

		if (code == null && other.getCode() != null) {
			return false;
		} else if (code != null && !code.equals(other.getCode())) {
			return false;
		} else if (cetCode == null && other.getCetCode() != null) {
			return false;
		} else
			return cetCode == null || cetCode.equals(other.getCetCode());
	}

	@Override
	public String toString() {
		return "CustomEntityInstance [cetCode=" + cetCode + ", code=" + code + ", id=" + id + "]";
	}

	public String getCetCode() {
		return cetCode;
	}

	public void setCetCode(String cetCode) {
		this.cetCode = cetCode;
	}

	public void setParentEntityUuid(String parentEntityUuid) {
		this.parentEntityUuid = parentEntityUuid;
	}

	public String getParentEntityUuid() {
		return parentEntityUuid;
	}

	public CustomEntityTemplate getCet() {
		return cet;
	}

	public void setCet(CustomEntityTemplate cet) {
		this.cet = cet;
		if(cet != null) this.cetCode = cet.getCode();
	}

	/**
	 * Retrieves the computed table name base on cetCode.
	 *
	 * @return table name use by SQL
	 */
	public String getTableName() {
		if (StringUtils.isBlank(tableName)) {
			tableName = SQLStorageConfiguration.getCetDbTablename(cetCode);
		}

		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public CustomFieldValues getCfValuesOld() {
		return cfValuesOld;
	}
	
	public <T> T get(String key) {
		return (T) this.getCfValuesAsValues().get(key);
	}

	public CustomFieldValues getCfValuesOldNullSafe() {
		if (cfValuesOld == null) {
			cfValuesOld = new CustomFieldValues();
		}

		return cfValuesOld;
	}

	public void setCfValuesOld(CustomFieldValues cfValuesOld) {
		this.cfValuesOld = cfValuesOld;
	}

	/**
	 * @return the {@link #fieldTemplates}
	 */
	public Map<String, CustomFieldTemplate> getFieldTemplates() {
		return fieldTemplates;
	}

	/**
	 * @param fieldTemplates the fieldTemplates to set
	 */
	public void setFieldTemplates(Map<String, CustomFieldTemplate> fieldTemplates) {
		this.fieldTemplates = fieldTemplates;
	}
	
	public Map<String, Object> getValues(DBStorageType storageType) {
		return PersistenceUtils.filterValues(fieldTemplates, getCfValuesAsValues(), cet, storageType);
	}
	
}
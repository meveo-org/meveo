/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.model.persistence;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.meveo.model.scripts.ScriptInstance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration class that represent the data storage handled by the application
 */
@Entity
@Table(name = "db_storage_type")
public class DBStorageType implements Serializable {
	
	public static final DBStorageType SQL;
	public static final DBStorageType NEO4J;
	
	static {
		SQL = new DBStorageType();
		SQL.code = "SQL";
		SQL.storageImplName = "org.meveo.persistence.impl.SQLStorageImpl";
		
		NEO4J = new DBStorageType();
		NEO4J.code = "NEO4J";
		NEO4J.storageImplName = "org.meveo.persistence.impl.Neo4jStorageImpl";
	}
	
	public static DBStorageType valueOf(String name) {
		return List.of(SQL, NEO4J).stream()
			.filter(e -> e.code.equals(name))
			.findFirst()
			.orElse(null);
	}
	
	@Id
	@Column(name = "code")
	@JsonValue
	private String code;
	
	@Column(name = "storage_impl_name")
	private String storageImplName;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "storage_impl_script_id")
	private ScriptInstance storageImplScript;
	
	public String name() {
		return this.code;
	}

	/**
	 * @return the {@link #code}
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the {@link #storageImplName}
	 */
	public String getStorageImplName() {
		return storageImplName;
	}

	/**
	 * @param storageImplName the storageImplName to set
	 */
	public void setStorageImplName(String storageImplName) {
		this.storageImplName = storageImplName;
	}

	/**
	 * @return the {@link #storageImplScript}
	 */
	public ScriptInstance getStorageImplScript() {
		return storageImplScript;
	}

	/**
	 * @param storageImplScript the storageImplScript to set
	 */
	public void setStorageImplScript(ScriptInstance storageImplScript) {
		this.storageImplScript = storageImplScript;
	}

	@Override
	public int hashCode() {
		return Objects.hash(code);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DBStorageType other = (DBStorageType) obj;
		return Objects.equals(code, other.code);
	}

	@Override
	public String toString() {
		return this.code;
	}
	
}

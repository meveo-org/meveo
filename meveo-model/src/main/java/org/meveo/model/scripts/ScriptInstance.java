/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
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
package org.meveo.model.scripts;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Parameter;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.ModuleItem;
import org.meveo.model.ModuleItemOrder;
import org.meveo.model.ObservableEntity;
import org.meveo.model.annotation.ImportOrder;
import org.meveo.model.security.Role;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10
 */
@Entity
@ModuleItem(value = "ScriptInstance", path = "scriptInstances")
@ModuleItemOrder(60)
@ObservableEntity
@Table(name = "meveo_script_instance")
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
		@Parameter(name = "sequence_name", value = "meveo_function_seq") })
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@NamedQueries({
		@NamedQuery(name = "CustomScript.updateScript", query = "UPDATE ScriptInstance SET script = :script WHERE code = :code"),
		@NamedQuery(name = "CustomScript.countScriptInstanceOnError", query = "select count (*) from ScriptInstance o where o.error=:isError "),
		@NamedQuery(name = "CustomScript.getScriptInstanceOnError", query = "from ScriptInstance o where o.error=:isError "),
		@NamedQuery(name = "CustomScript.getScriptInstanceByTypeActive", query = "from ScriptInstance o where o.sourceTypeEnum=:sourceTypeEnum and o.disabled = false")
})
@ImportOrder(4)
@ExportIdentifier({ "code" })
public class ScriptInstance extends CustomScript implements Comparable<ScriptInstance> {

	private static final long serialVersionUID = -7691357496569390167L;
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "adm_script_exec_role", joinColumns = @JoinColumn(name = "script_instance_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> executionRoles;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "adm_script_sourc_role", joinColumns = @JoinColumn(name = "script_instance_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> sourcingRoles;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "adm_script_maven_dependency", joinColumns = @JoinColumn(name = "script_instance_id"), inverseJoinColumns = @JoinColumn(name = "maven_coordinates"))
	private Set<MavenDependency> mavenDependencies;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "meveo_script_instance_script_instance", joinColumns = @JoinColumn(name = "script_instance_id"), inverseJoinColumns = @JoinColumn(name = "child_script_instance_id"))
	private Set<ScriptInstance> importScriptInstances;
	
	public Set<Role> getExecutionRolesNullSafe() {
		if (executionRoles == null) {
			executionRoles = new HashSet<Role>();
		}
		return getExecutionRoles();
	}

	/**
	 * @return the executionRoles
	 */
	public Set<Role> getExecutionRoles() {
		return executionRoles;
	}

	/**
	 * @param executionRoles the executionRoles to set
	 */
	public void setExecutionRoles(Set<Role> executionRoles) {
		this.executionRoles = executionRoles;
	}

	public Set<Role> getSourcingRolesNullSafe() {
		if (sourcingRoles == null) {
			sourcingRoles = new HashSet<Role>();
		}
		return getSourcingRoles();
	}

	/**
	 * @return the sourcingRoles
	 */
	public Set<Role> getSourcingRoles() {
		return sourcingRoles;
	}

	/**
	 * @param sourcingRoles the sourcingRoles to set
	 */
	public void setSourcingRoles(Set<Role> sourcingRoles) {
		this.sourcingRoles = sourcingRoles;
	}

	public Set<MavenDependency> getMavenDependenciesNullSafe() {
		if (mavenDependencies == null) {
			mavenDependencies = new HashSet<MavenDependency>();
		}
		return getMavenDependencies();
	}

	public Set<MavenDependency> getMavenDependencies() {
		return mavenDependencies;
	}

	public void setMavenDependencies(Set<MavenDependency> mavenDependencies) {
		this.mavenDependencies = mavenDependencies;
	}

	public Set<ScriptInstance> getImportScriptInstancesNullSafe() {
		if (importScriptInstances == null) {
			importScriptInstances = new HashSet<ScriptInstance>();
		}
		return getImportScriptInstances();
	}
	
	/**
	 * @return the importScriptInstances
	 */
	public Set<ScriptInstance> getImportScriptInstances() {
		return importScriptInstances;
	}

	/**
	 * @param importScriptInstances the importScriptInstances to set
	 */
	public void setImportScriptInstances(Set<ScriptInstance> importScriptInstances) {
		this.importScriptInstances = importScriptInstances;
	}

	@Override
	public String toString() {
		return "ScriptInstance [code=" + code + ", description=" + description + ", id=" + id + "]";
	}
	
	public Set<ScriptInstance> getTransitiveScripts() {
		if (getImportScriptInstances() != null && !getImportScriptInstances().isEmpty()) {
			return Stream.concat(getImportScriptInstances().stream(), 
					getImportScriptInstances().stream().flatMap(script -> script.getTransitiveScripts().stream()))
					.collect(Collectors.toSet());
		} else {
			return Set.of();
		}
	}

	@Override
	public int compareTo(ScriptInstance o) {
		if (o == null || o.getCode() == null) {
			return -1;
		}
		
		if (this.getTransitiveScripts().contains(o)) {
			return 1;
		}
		
		if (o.getTransitiveScripts().contains(this)) {
			return -1;
		}
		
		return 0;
	}

}
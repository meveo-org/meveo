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

import java.util.*;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.ModuleItem;
import org.meveo.model.annotation.ImportOrder;
import org.meveo.model.security.Role;

@Entity
@ModuleItem
@Cacheable
@Table(name = "meveo_script_instance")
@GenericGenerator(
        name = "ID_GENERATOR",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@Parameter(name = "sequence_name", value = "meveo_function_seq")}
)
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@NamedQueries({ @NamedQuery(name = "CustomScript.countScriptInstanceOnError", query = "select count (*) from ScriptInstance o where o.error=:isError "),
    @NamedQuery(name = "CustomScript.getScriptInstanceOnError", query = "from ScriptInstance o where o.error=:isError "),
    @NamedQuery(name = "CustomScript.getScriptInstanceByTypeActive", query = "from ScriptInstance o where o.sourceTypeEnum=:sourceTypeEnum and o.disabled = false")})
@ImportOrder(4)
@ExportIdentifier({ "code" })
public class ScriptInstance extends CustomScript {

    private static final long serialVersionUID = -7691357496569390167L;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "adm_script_exec_role", joinColumns = @JoinColumn(name = "script_instance_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> executionRoles = new HashSet<Role>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "adm_script_sourc_role", joinColumns = @JoinColumn(name = "script_instance_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> sourcingRoles = new HashSet<Role>();

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

    /**
     * @return the sourcingRoles
     */
    public Set<Role> getSourcingRoles() {
        return sourcingRoles;
    }
}
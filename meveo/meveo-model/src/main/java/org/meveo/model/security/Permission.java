/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.meveo.model.security;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jboss.seam.annotations.security.permission.PermissionAction;
import org.jboss.seam.annotations.security.permission.PermissionDiscriminator;
import org.jboss.seam.annotations.security.permission.PermissionRole;
import org.jboss.seam.annotations.security.permission.PermissionTarget;
import org.jboss.seam.annotations.security.permission.PermissionUser;
import org.meveo.model.BaseEntity;

/**
 * @author Ignas Lelys
 * @created Dec 3, 2010
 *
 */
@Entity
@Table(name = "ADM_ROLE_PERMISSION")
@SequenceGenerator(name = "ID_GENERATOR", sequenceName = "ADM_ROLE_PERMISSION_SEQ")
@org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Permission extends BaseEntity {
    
    private static final long serialVersionUID = 1L;

    @PermissionUser
    @PermissionRole
    @Column(name = "ROLE", nullable = false)
    private String role;

    @PermissionTarget
    @Column(name = "TARGET", nullable = false)
    private String target;

    @PermissionAction
    @Column(name = "ACTION", nullable = false, length = 1500)
    private String action;

    @PermissionDiscriminator
    @Column(name = "DISCRIMINATOR", nullable = false)
    private String discriminator;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }
}

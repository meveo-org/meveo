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
import org.meveo.model.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Clément Bareth
 * @lastModifiedVersion 6.3.0
 */
@Entity
@ObservableEntity
@Cacheable
@ModuleItem("CustomEntityInstance")
@CustomFieldEntity(cftCodePrefix = "CE", cftCodeFields = "cetCode")
@ExportIdentifier({ "code", "cetCode"})
@Table(name = "cust_cei", uniqueConstraints = @UniqueConstraint(columnNames = { "code", "cet_code"}))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {@Parameter(name = "sequence_name", value = "cust_cei_seq"), })
public class CustomEntityInstance extends BusinessCFEntity {

    private static final long serialVersionUID = 8281478284763353310L;

    @Column(name = "cet_code", nullable = false)
    @Size(max = 255)
    @NotNull
    public String cetCode;

    @Column(name = "parent_uuid", updatable = false, length = 60)
    @Size(max = 60)
    public String parentEntityUuid;

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
        } else if (!code.equals(other.getCode())) {
            return false;
        } else if (cetCode == null && other.getCetCode() != null) {
            return false;
        } else return cetCode == null || cetCode.equals(other.getCetCode());
    }
}
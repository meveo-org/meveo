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

package org.meveo.model.persistence.sql;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.meveo.model.BaseEntity;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;

@Embeddable
public class SQLStorageConfiguration {

    /**
     * Should data be stored in a separate table
     */
    @Type(type = "numeric_boolean")
    @Column(name = "store_as_table", nullable = false)
    @NotNull
    private boolean storeAsTable = false;

    /**
     * Get a database field name derived from a code value. Lowercase and spaces replaced by "_".
     *
     * @param code Field code
     * @return Database field name
     */
    public static String getCetDbTablename(String code) {
        return BaseEntity.cleanUpAndLowercaseCodeOrId(code);
    }
    
    /**
     * Get a database field name derived from a code value. Lowercase and spaces replaced by "_".
     *
     * @param cet Custom entity template to retrieve code
     * @return Database field name
     */
    public static String getDbTablename(CustomEntityTemplate cet) {
        return BaseEntity.cleanUpAndLowercaseCodeOrId(cet.getCode());
    }
    
    /**
     * Get a database field name derived from code, start cet code and end cet code. Lowercase and spaces replaced by "_".
     *
     * @param crt {@link CustomRelationshipTemplate} to retrieve code
     * @return Database table name
     */
    public static String getDbTablename(CustomRelationshipTemplate crt) {
        return BaseEntity.cleanUpAndLowercaseCodeOrId(crt.getCode());
    }

    public boolean isStoreAsTable() {
        return storeAsTable;
    }

    public void setStoreAsTable(boolean storeAsTable) {
        this.storeAsTable = storeAsTable;
    }
}

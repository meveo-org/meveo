/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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
package org.meveo.model.technicalservice;

import org.meveo.interfaces.technicalservice.description.EntityDescription;
import org.meveo.model.customEntities.CustomEntityTemplate;

import javax.persistence.*;

/**
 * Description of an entity in stake for the connector.
 *
 * @author Clément Bareth
 */
@Entity
@DiscriminatorValue("entity_description")
public class MeveoEntityDescription extends Description implements EntityDescription {

    @JoinColumn(name = "cet_id")
    @ManyToOne
    private CustomEntityTemplate type;

    /**
     * The CustomEntityTemplate described.
     *
     * @return The CET described
     */
    public CustomEntityTemplate getCet() {
        return type;
    }

    @Override
    public String getType() {
        return type.getCode();
    }

    /**
     * The CustomEntityTemplate described.
     *
     * @param customEntityTemplate The CustomEntityTemplate to describe
     */
    public void setType(CustomEntityTemplate customEntityTemplate) {
        this.type = customEntityTemplate;
    }

    @Override
    public String getTypeName() {
        return type.getCode();
    }
    
    public String getAppliesTo() {
    	return type.getAppliesTo();
    }

}

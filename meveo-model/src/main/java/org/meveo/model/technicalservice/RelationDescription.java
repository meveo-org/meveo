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

import org.meveo.interfaces.technicalservice.description.RelationshipDescription;
import org.meveo.model.customEntities.CustomRelationshipTemplate;

import javax.persistence.*;

/**
 * Description of relation in stake for the connector.
 *
 * @author Clément Bareth
 */
@Entity
@DiscriminatorValue("relation_description")
public class RelationDescription extends Description implements RelationshipDescription {

    @Column(name = "source_name")
    private String source;

    @Column(name = "target_name")
    private String target;

    @ManyToOne
    @JoinColumn(name = "crt_id")
    private CustomRelationshipTemplate type;

    @PrePersist @PreUpdate
    private void prePersist(){
        super.setName(this.getName());
    }

    /**
     * @return The CustomRelationshipTemplate for the described relation
     */
    public CustomRelationshipTemplate getCrt() {
        return type;
    }

    /**
     * @param type The CustomRelationshipTemplate for the described relation
     */
    public void setType(CustomRelationshipTemplate type) {
        this.type = type;
    }

    /**
     * @return Source entity instance name of the relation
     */
    @Override
    public String getSource() {
        return this.source;
    }

    /**
     *
     * @param source Source entity instance name of the relation
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Target entity instance name of the relation
     */
    @Override
    public String getTarget() {
        return target;
    }

    /**
     * @param target Target entity instance name of the relation
     */
    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String getType() {
        return type.getCode();
    }

    @Override
    public String getName() {
        return source + "-" + target;
    }

    @Override
    public String getTypeName() {
        return type.getCode();
    }



}

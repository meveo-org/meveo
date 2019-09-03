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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.interfaces.technicalservice.description.properties.PropertyDescription;
import org.meveo.model.crm.CustomFieldTemplate;

import javax.persistence.*;

@Entity
@GenericGenerator(
        name = "ID_GENERATOR",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@Parameter(name = "sequence_name", value = "property_description_seq")}
)
@Table(name = "property_description")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "direction")
public abstract class MeveoPropertyDescription implements PropertyDescription {

    @Id
    @GeneratedValue(generator = "ID_GENERATOR", strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_id")
    private Description description;

    @ManyToOne
    @JoinColumn(name = "cft_id")
    private CustomFieldTemplate property;
    /**
     * CustomFieldTemplate linked to the CustomEntityTemplate described
     *
     * @return The CustomFieldTemplate object
     */
    public CustomFieldTemplate getCet() {
        return property;
    }

    /**
     * CustomFieldTemplate linked to the CustomEntityTemplate described
     *
     * @param property The CustomFieldTemplate object
     */
    public void setProperty(CustomFieldTemplate property) {
        this.property = property;
    }

    /**
     *
     * @return The base entity or relation {@link Description}
     */
    public Description getDescription() {
        return description;
    }

    /**
     *
     * @param description  The base entity or relation {@link Description}
     */
    public void setDescription(Description description) {
        this.description = description;
    }

    /**
     *
     * @return Identifier in database
     */
    public long getId() {
        return id;
    }

    @Override
    public String getProperty() {
        return property.getCode();
    }
}

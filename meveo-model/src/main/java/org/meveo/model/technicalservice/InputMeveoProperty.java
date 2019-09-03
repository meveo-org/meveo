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
package org.meveo.model.technicalservice;

import org.meveo.interfaces.technicalservice.description.properties.InputPropertyDescription;

import javax.persistence.*;

/**
 * Description of an input property of an entity or relation.
 *
 * @author Clément Bareth
 */
@Entity(name = "InputMeveoProperty")
@DiscriminatorValue("input")
public class InputMeveoProperty extends MeveoPropertyDescription implements InputPropertyDescription {

    private boolean required;

    @Enumerated(EnumType.STRING)
    private Comparator comparator;

    @Column(name = "comparison_value")
    private String comparisonValue;

    @Column(name = "default_value")
    private String defaultValue;

    /**
     * Whether the property is mandatory.
     *
     * @return "true" if the property is mandatory.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Whether the property is mandatory.
     *
     * @param required "true" if the property is mandatory.
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Comparator that permit to constraints the value of the property.
     *
     * @return The comparator enumeration value
     */
    public Comparator getComparator() {
        return comparator;
    }

    /**
     * Comparator that permit to constraints the value of the property.
     */
    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }

    /**
     * Value to compare with when the comparator is provided.
     *
     * @return The comparison value.
     */
    public String getComparisonValue() {
        return comparisonValue;
    }

    /**
     * Value to compare with when the comparator is provided.
     *
     * @param comparisonValue The comparison value
     */
    public void setComparisonValue(String comparisonValue) {
        this.comparisonValue = comparisonValue;
    }

    /**
     * If property is not required, default value to give to the property.
     *
     * @return The default value
     */
    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * If property is not required, default value to give to the property.
     *
     * @param defaultValue The default value
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object obj) {

        if(obj == null){
            return false;
        }

        if(obj instanceof InputMeveoProperty){
            return ((InputMeveoProperty) obj).getId() == this.getId();
        }else{
            return false;
        }
    }
}

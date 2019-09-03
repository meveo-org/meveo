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
package org.meveo.api.dto.technicalservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.meveo.interfaces.technicalservice.description.properties.InputPropertyDescription;
import org.meveo.model.technicalservice.Comparator;

import java.util.Objects;

/**
 * Description of an input property of an entity or relation.
 *
 * @author Clément Bareth
 */
public class InputPropertyDto implements InputPropertyDescription {

    @JsonProperty(required = true)
    private String property;
    @JsonProperty(required = true)
    private boolean required;
    private Comparator comparator;
    private String comparisonValue;
    private String defaultValue;
    private String descriptionName;

    /**
     * Code of CustomEntityTemplate property
     *
     * @return Code of CustomEntityTemplate property
     */
    @Override
    public String getProperty() {
        return property;
    }

    /**
     * Code of CustomEntityTemplate property
     *
     * @param property Code of CustomEntityTemplate property
     */
    public void setProperty(String property) {
        this.property = property;
    }

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
     *
     * @param comparator The comparator enumeration value
     */
    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }

    /**
     * Value to compare with when the comparator is provided.
     *
     * @return The comparison value
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
     * @return The default value.
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

    /**
     * Name of the description whose property belongs to
     *
     * @return Name of the input entity or relation
     */
    public String getDescriptionName() {
        return descriptionName;
    }

    /**
     * Name of the description whose property belongs to
     *
     * @param descriptionName Name of the the input entity or relation
     */
    public void setDescriptionName(String descriptionName) {
        this.descriptionName = descriptionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputPropertyDto that = (InputPropertyDto) o;
        return property.equals(that.property) && descriptionName.equals(that.descriptionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, descriptionName);
    }
}

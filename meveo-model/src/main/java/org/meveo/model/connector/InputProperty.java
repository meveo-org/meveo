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
package org.meveo.model.connector;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * Description of an input property of an entity or relation.
 *
 * @author Clément Bareth
 */
public class InputProperty {

    /**
     * Name of the related property
     */
    @JsonProperty(required = true)
    private String property;

    /**
     * Whether the property is mandatory.
     */
    @JsonProperty(required = true)
    private boolean required;

    /**
     * Comparator that permit to constraints the value of the property.
     */
    private Comparator comparator;

    /**
     * Value to compare with when the comparator is provided.
     */
    private String comparisonValue;

    /**
     * If property is not required, default value to give to the property.
     */
    private String defaultValue;

    /**
     * Name of the related property
     * @return The name of the related property
     */
    public String getProperty() {
        return property;
    }

    /**
     * Whether the property is mandatory.
     * @return "true" if the property is mandatory.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Comparator that permit to constraints the value of the property.
     * @return The comparator wrapped in an optional or an empty optional if the comparator is not provided.
     */
    public Optional<Comparator> getComparator() {
        return Optional.ofNullable(comparator);
    }

    /**
     * Value to compare with when the comparator is provided.
     * @return The comparison value wrapped in an optional or an empty optional if the comparison value is not provided.
     */
    public Optional<String> getComparisonValue() {
        return Optional.ofNullable(comparisonValue);
    }

    /**
     * If property is not required, default value to give to the property.
     * @return The default value wrapped in an optional or an empty optional if the default value is not provided.
     */
    public Optional<String> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }
}

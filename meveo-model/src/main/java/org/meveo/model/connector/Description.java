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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Describe the input and output properties for a variable
 *
 * @author Clément Bareth
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "descriptionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EntityDescription.class, name = "EntityDescription"),
        @JsonSubTypes.Type(value = RelationDescription.class, name = "RelationDescription"),
})
public abstract class Description {

    /**
     * List of properties that are defined as inputs. Non empty list implies input = true.
     */
    @JsonProperty
    private List<InputProperty> inputProperties = new ArrayList<>();

    /**
     * List of properties that are defined as outputs. Non empty list implies output = true.
     */
    @JsonProperty
    private List<OutputProperty> outputProperties = new ArrayList<>();

    /**
     * Ontology type of the variable.
     */
    @JsonProperty(required = true)
    private String type;

    /**
     * Whether the variable is defined as input of the connector.
     */
    @JsonProperty
    private boolean input;

    /**
     * Whether the variable is defined as output of the connector.
     */
    @JsonProperty
    private boolean output;

    /**
     * List of properties that are defined as inputs. Non empty list implies input = true.
     * @return The list of the properties that are defined as inputs.
     */
    public List<InputProperty> getInputProperties() {
        return inputProperties;
    }

    /**
     * List of properties that are defined as outputs. Non empty list implies output = true.
     * @return The list of the properties that are defined as inputs.
     */
    public List<OutputProperty> getOutputProperties() {
        return outputProperties;
    }

    /**
     * Ontology type of the variable.
     * @return The ontology type of the variable
     */
    public String getType() {
        return type;
    }

    /**
     * Whether the variable is defined as input of the connector.
     * @return "true" if the variable is an input.
     */
    public boolean isOutput() {
        return output;
    }

    /**
     * Whether the variable is defined as output of the connector.
     * @return "false" if the variable is an input.
     */
    public boolean isInput(){
        return input;
    }

    /**
     * Name of the variable described
     * @return The name of the variable
     */
    public abstract String getName();
}

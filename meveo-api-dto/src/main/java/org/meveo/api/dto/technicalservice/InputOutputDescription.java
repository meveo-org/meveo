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

import static org.meveo.api.dto.ProcessEntityDescription.ENTITY_DESCRIPTION;
import static org.meveo.api.dto.technicalservice.ProcessRelationDescription.RELATION_DESCRIPTION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.meveo.api.dto.ProcessEntityDescription;
import org.meveo.interfaces.technicalservice.description.TechnicalServiceDescription;
import org.meveo.model.technicalservice.Description;
import org.meveo.model.technicalservice.InputMeveoProperty;
import org.meveo.model.technicalservice.OutputMeveoProperty;
import org.meveo.model.technicalservice.RelationDescription;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The Class InputOutputDescription.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "descriptionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProcessEntityDescription.class, name = ENTITY_DESCRIPTION),
        @JsonSubTypes.Type(value = ProcessRelationDescription.class, name = RELATION_DESCRIPTION)
})
public abstract class InputOutputDescription implements TechnicalServiceDescription {

    @JsonProperty
    private List<InputPropertyDto> inputProperties = new ArrayList<>();

    @JsonProperty
    private List<OutputPropertyDto> outputProperties = new ArrayList<>();

    @JsonProperty(required = true)
    @NotNull
    private String type;

    @JsonProperty
    private boolean input;

    @JsonProperty
    private boolean output;
    
    private boolean isInherited;
    
    /**
     * Whether the description is inherited
     */
    public boolean isInherited() {
		return isInherited;
	}

	/**
	 * Sets whether the property is inherited.
	 *
	 * @param isInherited the new inherited
	 */
	public void setInherited(boolean isInherited) {
		this.isInherited = isInherited;
	}

	/**
     * List of properties that are defined as inputs. Non empty list implies input = true.
     *
     * @return The list of the properties that are defined as inputs.
     */
    @Override
    public List<InputPropertyDto> getInputProperties() {
        return inputProperties;
    }

    /**
     * List of properties that are defined as inputs. Non empty list implies input = true.
     *
     * @param inputProperties The List of properties that are defined as inputs.
     */
    public void setInputProperties(List<InputPropertyDto> inputProperties) {
        this.inputProperties = inputProperties;
    }

    /**
     * List of properties that are defined as outputs. Non empty list implies output = true.
     *
     * @return The list of the properties that are defined as inputs.
     */
    @Override
    public List<OutputPropertyDto> getOutputProperties() {
        return outputProperties;
    }

    /**
     * List of properties that are defined as outputs. Non empty list implies output = true.
     *
     * @param outputProperties List of properties that are defined as outputs.
     */
    public void setOutputProperties(List<OutputPropertyDto> outputProperties) {
        this.outputProperties = outputProperties;
    }

    /**
     * Custom entity template code that the object describe.
     *
     * @return The code of the CET described
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Custom entity template code that the object describe.
     *
     * @param type The code of the CET described
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Whether the variable is defined as output of the technical service.
     *
     * @return "false" if the variable is not an output.
     */
    @Override
    public boolean isOutput() {
        return output;
    }

    /**
     * Whether the variable is defined as output of the technical service.
     *
     * @param output "false" if the variable is not an output.
     */
    public void setOutput(boolean output) {
        this.output = output;
    }

    /**
     * Whether the variable is defined as input of the technical service.
     *
     * @return "false" if the variable is not an input.
     */
    @Override
    public boolean isInput() {
        return input;
    }

    /**
     * Whether the variable is defined as input of the technical service.
     *
     * @param input "false" if the variable is not an input.
     */
    public void setInput(boolean input) {
        this.input = input;
    }

    /**
     * Name of the variable described
     *
     * @return The instance name of the variable described
     */
    @Override
    public abstract String getName();

    /**
	 * Name of the variable described.
	 *
	 * @param name The instance name of the variable described
	 */
    public abstract void setName(String name);

    /**
	 * Create new {@link InputOutputDescription} from a {@link Description}.
	 *
	 * @param desc the {@link Description} to transform
	 * @return the created {@link InputOutputDescription}
	 */
    public static InputOutputDescription fromDescription(Description desc) {
        InputOutputDescription descriptionDto;
        if (desc instanceof RelationDescription) {
            descriptionDto = new ProcessRelationDescription();
            ((ProcessRelationDescription) descriptionDto).setSource(((RelationDescription) desc).getSource());
            ((ProcessRelationDescription) descriptionDto).setTarget(((RelationDescription) desc).getTarget());
        } else {
            descriptionDto = new ProcessEntityDescription();
        }
        descriptionDto.setInherited(desc.isInherited());
        descriptionDto.setName(desc.getName());
        descriptionDto.setType(desc.getTypeName());
        descriptionDto.setInput(desc.isInput());
        descriptionDto.setOutput(desc.isOutput());
        final List<InputPropertyDto> inputProperties = new ArrayList<>();
        final List<OutputPropertyDto> outputProperties = new ArrayList<>();
        
        for (InputMeveoProperty p : desc.getInputProperties()) {
            InputPropertyDto inputPropertyDto = new InputPropertyDto(p);
            inputProperties.add(inputPropertyDto);
        }
        
        for (OutputMeveoProperty p : desc.getOutputProperties()) {
            OutputPropertyDto outputPropertyDto = new OutputPropertyDto(p);
            outputProperties.add(outputPropertyDto);
        }
        
        descriptionDto.setInputProperties(inputProperties);
        descriptionDto.setOutputProperties(outputProperties);
        return descriptionDto;
    }

    /**
	 * Create a new list of {@link InputOutputDescription} from a collection of{@link Description}.
	 *
	 * @param collection the list {@link Description} to transform
	 * @return the created list of {@link InputOutputDescription}
	 */
    public static List<InputOutputDescription> fromDescriptions(Collection<Description> collection) {
        return collection
                .stream()
                .map(InputOutputDescription::fromDescription)
                .collect(Collectors.toList());
    }





}

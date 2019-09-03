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
import org.hibernate.annotations.Where;
import org.meveo.interfaces.technicalservice.description.TechnicalServiceDescription;
import org.meveo.model.scripts.Function;
import org.meveo.validation.constraint.subtypeof.SubTypeOf;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Describe the input and output properties for a variable
 *
 * @author Clément Bareth
 */
@GenericGenerator(
        name = "ID_GENERATOR",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@Parameter(name = "sequence_name", value = "technical_services_description_seq")}
)
@Entity
@Table(name = "technical_services_description")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "description_type")
public abstract class Description implements TechnicalServiceDescription {

    @Id
    @GeneratedValue(generator = "ID_GENERATOR", strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "entity_name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Function.class)
    @JoinColumn(name = "service_id")
    @SubTypeOf(TechnicalService.class)
    private Function service;

    /**
     * List of properties that are defined as inputs. Non empty list implies input = true.
     */
    @OneToMany(mappedBy = "description", targetEntity = MeveoPropertyDescription.class, cascade = CascadeType.ALL)
    @Where(clause = "direction ='input'")
    private List<InputMeveoProperty> inputProperties = new ArrayList<>();

    /**
     * List of properties that are defined as outputs. Non empty list implies output = true.
     */
    @OneToMany(mappedBy = "description", targetEntity = MeveoPropertyDescription.class, cascade = CascadeType.ALL)
    @Where(clause = "direction = 'output'")
    private List<OutputMeveoProperty> outputProperties = new ArrayList<>();

    /**
     * Whether the variable is defined as input of the connector.
     */
    private boolean input;

    /**
     * Whether the variable is defined as output of the connector.
     */
    private boolean output;

    /**
     * List of properties that are defined as inputs. Non empty list implies input = true.
     * @return The list of the properties that are defined as inputs.
     */
    @Override
    public List<InputMeveoProperty> getInputProperties() {
        return inputProperties;
    }

    /**
     * List of properties that are defined as outputs. Non empty list implies output = true.
     * @return The list of the properties that are defined as inputs.
     */
    @Override
    public List<OutputMeveoProperty> getOutputProperties() {
        return outputProperties;
    }

    /**
     * Whether the variable is defined as input of the connector.
     * @return "true" if the variable is an input.
     */
    @Override
    public boolean isOutput() {
        return output;
    }

    /**
     * Whether the variable is defined as output of the connector.
     * @return "false" if the variable is an input.
     */
    @Override
    public boolean isInput(){
        return input;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Instance name of the variable described
     *
     * @param name Instance name of the variable described
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Type name of the described entity
     */
    public abstract String getTypeName();

    /**
     * @param inputProperties Input properties defined for the described entity
     */
    public void setInputProperties(List<InputMeveoProperty> inputProperties) {
        this.inputProperties = inputProperties;
    }

    /**
     * @param outputProperties Output properties defined for the described entity
     */
    public void setOutputProperties(List<OutputMeveoProperty> outputProperties) {
        this.outputProperties = outputProperties;
    }

    /**
     * @param input Whether the described entity is an input
     */
    public void setInput(boolean input) {
        this.input = input;
    }

    /**
     * @param output Whether the described entity is an output
     */
    public void setOutput(boolean output) {
        this.output = output;
    }

    /**
     * @return Technical service that is described
     */
    public Function getService() {
        return service;
    }

    /**
     * @param service Technical service that is described
     */
    public void setService(TechnicalService service) {
        this.service = service;
    }

    /**
     * @return The id of the description in the database
     */
    public long getId() {
        return id;
    }

}

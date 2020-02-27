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
package org.meveo.interfaces.technicalservice.description;

import org.meveo.interfaces.technicalservice.description.properties.InputPropertyDescription;
import org.meveo.interfaces.technicalservice.description.properties.PropertyDescription;
import org.meveo.model.technicalservice.TechnicalService;

import java.util.List;

/**
 * Description of an input or output of a {@link TechnicalService}
 * 
 * @author clement.bareth
 * @since 6.0.0
 * @version 6.8.0
 */
public interface TechnicalServiceDescription {

    /**
     * List of properties that are defined as inputs. Non empty list implies input = true.
     *
     * @return The list of the properties that are defined as inputs.
     */
    List<? extends InputPropertyDescription> getInputProperties();

    /**
     * List of properties that are defined as outputs. Non empty list implies output = true.
     *
     * @return The list of the properties that are defined as inputs.
     */
    List<? extends PropertyDescription> getOutputProperties();

    /**
     * Custom entity template code that the object describe.
     *
     * @return The code of the CET described
     */
    String getType();

    /**
     * Whether the variable is defined as output of the technical service.
     *
     * @return "false" if the variable is not an output.
     */
    boolean isOutput();

    /**
     * Whether the variable is defined as input of the technical service.
     *
     * @return "false" if the variable is not an input.
     */
    boolean isInput();

    /**
     * Name of the variable described
     *
     * @return The instance name of the variable described
     */
    String getName();
    
    /**
     * @return true if the description is inherited
     */
    boolean isInherited();

}

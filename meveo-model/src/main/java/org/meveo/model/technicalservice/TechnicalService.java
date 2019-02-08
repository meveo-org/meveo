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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.FunctionIO;

/**
 * @author Clément Bareth
 */
@MappedSuperclass
public class TechnicalService extends Function {

	private static final long serialVersionUID = 1L;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
    private List<Description> descriptions;

    /**
     * Description of the inputs and outputs of the connector
     */
    public List<Description> getDescriptions() {
        return descriptions;
    }

    /**
     * @param descriptions Description of the inputs and outputs of the connector
     */
    public void setDescriptions(List<Description> descriptions) {
        this.descriptions = descriptions;
    }

    /**
     * Name of the connector
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Name of the connector
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param contextMap Context map of the current execution
     * @return {@code true} if the service can be used for the given context
     */
    public boolean isApplicable(Map<String, Object> contextMap) { 
    	return true;
    }
    
	@Override
	public List<FunctionIO> getInputs() {
		List<FunctionIO> inputs = new ArrayList<>();
		descriptions.stream()
			.filter(Description::isInput)
			.forEach(d -> {
				d.getInputProperties().forEach(prop -> {
					FunctionIO inp = new FunctionIO();
					inp.setName(d.getName()+"."+prop.getCet().getCode());
					inp.setDescription(prop.getCet().getDescription());
					inp.setType(prop.getCet().getFieldType().toString());
					inputs.add(inp);
				});
			});
		return inputs;
	}

    @Override
    public List<FunctionIO> getOutputs() {
        List<FunctionIO> inputs = new ArrayList<>();
        descriptions.stream()
                .filter(Description::isInput)
                .forEach(d -> {
                    d.getOutputProperties().forEach(prop -> {
                        FunctionIO inp = new FunctionIO();
                        inp.setName(d.getName()+"."+prop.getCet().getCode());
                        inp.setDescription(prop.getCet().getDescription());
                        inp.setType(prop.getCet().getFieldType().toString());
                        inputs.add(inp);
                    });
                });
        return inputs;
    }

	@Override
	public boolean hasInputs() {
		return descriptions.stream().anyMatch(d -> d.isInput() && !d.getInputProperties().isEmpty());
	}

    @Override
    public boolean hasOutputs() {
        return descriptions.stream().anyMatch(d -> d.isOutput() && !d.getInputProperties().isEmpty());
    }

    @Override
    public String getFunctionType() {
        return "TechinalService";
    }
}

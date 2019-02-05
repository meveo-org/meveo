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
package org.meveo.model.technicalservice.endpoint;

import org.meveo.model.technicalservice.InputMeveoProperty;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 * Embeddable representation of an endpoint parameter.
 *
 * @author clement.bareth
 * @since 01.02.2019
 */
@Embeddable
public class EndpointParameter implements Serializable {

    /**
     * Endpoint associated to the parameter
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id")
    private Endpoint endpoint;

    /**
     * Input property of the technical service described by the endpoint
     */
    @ManyToOne
    @JoinColumn(name = "parameter_id")
    private InputMeveoProperty parameter;

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public InputMeveoProperty getParameter() {
        return parameter;
    }

    public void setParameter(InputMeveoProperty parameter) {
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return parameter.getDescription().getName() + "." + parameter.getProperty();
    }
}

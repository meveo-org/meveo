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

import javax.persistence.*;
import java.util.Objects;

/**
 * Configuration of an endpoint allowing to use a technical service.
 *
 * @author clement.bareth
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
@Entity
@Table(name = "endpoint_path_parameter")
@NamedQuery(name = "deletePathParameterByEndpoint", query = "DELETE from EndpointPathParameter e WHERE e.endpointParameter.endpoint.id=:endpointId")
public class EndpointPathParameter {

    @EmbeddedId
    private EndpointParameter endpointParameter;

    /**
     * Position of the parameter the endpoint's path parameter list.
     * This column is used only for JPA to build list in right order.
     */
    @Column(name = "position", nullable = false)
    private int position = 0;

    @PrePersist @PreUpdate
    private void prePersist(){
        position = endpointParameter.getEndpoint().getPathParametersNullSafe().indexOf(this);
    }

    @Override
    public String toString() {
        return endpointParameter.toString();
    }

    public EndpointParameter getEndpointParameter() {
        return endpointParameter;
    }

    public void setEndpointParameter(EndpointParameter endpointParameter) {
        this.endpointParameter = endpointParameter;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndpointPathParameter that = (EndpointPathParameter) o;
        return getEndpointParameter().getParameter().equals(that.getEndpointParameter().getParameter());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEndpointParameter(), getPosition());
    }
}

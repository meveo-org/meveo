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

import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.meveo.model.scripts.Executable;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Clément Bareth
 */
@Entity
@Table(name = "technical_services", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "version"}))
@GenericGenerator(
        name = "ID_GENERATOR",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@Parameter(name = "sequence_name", value = "technical_services_seq")}
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "service_type")
public class TechnicalService extends Executable {

	private static final long serialVersionUID = 1L;

    @Column(name = "name")
    private String name;

    @JsonProperty
    @Column(name = "descriptions", columnDefinition = "TEXT")
    @Type(type = "json")
    private List<Description> descriptions;

    @Column(name = "service_type", insertable = false, updatable = false)
    private String serviceType;
    
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
     * @return Name of the service type defined by the administrator
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * @param serviceType Name of the service type defined by the administrator
     */
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

}

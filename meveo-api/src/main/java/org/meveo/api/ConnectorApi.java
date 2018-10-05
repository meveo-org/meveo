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
package org.meveo.api;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.dto.ConnectorDto;
import org.meveo.api.dto.ConnectorsDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.connector.ConnectorService;
import org.meveo.model.connector.ConnectorInstance;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Connector management api
 *
 * @author Clément Bareth
 */
@Stateless
public class ConnectorApi extends BaseApi {

    @Inject
    private ConnectorService connectorService;

    private static ConnectorDto toDto(ConnectorInstance connectorInstance) {
        final ConnectorDto dto = new ConnectorDto();
        dto.setCode(connectorInstance.getCode());
        dto.setConnector(connectorInstance.getConnector());
        dto.setName(connectorInstance.getName());
        dto.setVersion(connectorInstance.getVersion());
        return dto;
    }

    private static ConnectorInstance fromDto(ConnectorDto postData) {
        final ConnectorInstance connectorInstance = new ConnectorInstance();
        connectorInstance.setCode(postData.getCode());
        connectorInstance.setConnector(postData.getConnector());
        connectorInstance.setName(postData.getName());
        connectorInstance.setVersion(postData.getVersion());
        return connectorInstance;
    }

    /**
     * Create a connector based on data provided in the dto.
     * If the version is not specified, create a new version of the connector if it already exists
     *
     * @param postData Data used to create the connector
     * @throws BusinessException If connector already exists for specified name and version
     */
    public void create(@Valid @NotNull ConnectorDto postData) throws BusinessException {
        final ConnectorInstance connectorInstance = fromDto(postData);
        if (postData.getVersion() == null) {
            int versionNumber = 1;
            final Optional<Integer> latestVersion = connectorService.latestVersionNumber(postData.getName());
            if (latestVersion.isPresent()) {
                versionNumber = latestVersion.get() + 1;
            }
            connectorInstance.setVersion(versionNumber);
        }
        connectorService.create(connectorInstance);
    }

    /**
     * Update the connector with the specified information
     *
     * @param postData New data of the connector
     * @throws EntityDoesNotExistsException if the connector to update does not exists
     * @throws BusinessException            if the connector can't be updated
     */
    public void update(@Valid @NotNull ConnectorDto postData) throws EntityDoesNotExistsException, BusinessException {
        ConnectorInstance connectorInstance;
        connectorInstance = getConnectorInstance(postData.getName(), postData.getVersion());
        connectorInstance.setConnector(postData.getConnector());
        connectorService.update(connectorInstance);
    }

    /**
     * Retrieve the connector with the specified name and version.
     * If version is not provided, retrieve the last version of the connector.
     *
     * @param connectorName Name of the connector to retrieve
     * @param version       Version of the connector to retrieve
     * @return The DTO object corresponding to the connector retrieved
     * @throws EntityDoesNotExistsException if the connector does not exists
     */
    public ConnectorDto findByNameAndVersionOrLatest(String connectorName, Integer version) throws EntityDoesNotExistsException {
        ConnectorInstance connectorInstance = getConnectorInstance(connectorName, version);
        return toDto(connectorInstance);
    }

    /**
     * List all the connectors present in database
     *
     * @param name Name filter - Search for connector that has a similar name
     * @return The list of all connectors DTOs object retrieved
     */
    public ConnectorsDto list(String name) {
        Map<String, Object> filter = new HashMap<>();
        if (name != null) {
            filter.put("wildcardOr " + name, null);
        }
        PaginationConfiguration config = new PaginationConfiguration(filter);
        List<ConnectorInstance> customEntityInstances = connectorService.list(config);
        List<ConnectorDto> customEntityInstanceDTOs = customEntityInstances.stream()
                .map(ConnectorApi::toDto)
                .collect(Collectors.toList());
        return new ConnectorsDto(customEntityInstanceDTOs);
    }

    /**
     * List of all the versions for a specified connector name
     *
     * @param connectorName Name of the connector to retrieve versions
     * @return The different versions of the connector
     * @throws EntityDoesNotExistsException If no versions of the connector exists
     */
    public ConnectorsDto listByName(String connectorName) throws EntityDoesNotExistsException {
        List<ConnectorInstance> customEntityInstances = connectorService.findByName(connectorName);
        if (customEntityInstances.isEmpty()) {
            throw new EntityDoesNotExistsException("Connector with name " + connectorName + " does not exists");
        }
        List<ConnectorDto> customEntityInstanceDTOs = customEntityInstances.stream()
                .map(ConnectorApi::toDto)
                .collect(Collectors.toList());
        return new ConnectorsDto(customEntityInstanceDTOs);
    }

    /**
     * Remove a connector. If version is provided, only remove the specified version.
     *
     * @param connectorName Name of the connector to remove
     * @param version       Specific version of the connector to remove - All versions will be removed if not provided.
     * @throws EntityDoesNotExistsException If no connector corresponds to the provided name
     * @throws BusinessException            If the connector can't be removed
     */
    public void remove(String connectorName, Integer version) throws EntityDoesNotExistsException, BusinessException {
        if (version != null) {
            final ConnectorInstance connectorInstanceByNameAndVersion = getConnectorInstanceByNameAndVersion(connectorName, version);
            connectorService.remove(connectorInstanceByNameAndVersion);
        } else {
            List<ConnectorInstance> customEntityInstances = connectorService.findByName(connectorName);
            for (ConnectorInstance connectorInstance : customEntityInstances) {
                connectorService.remove(connectorInstance);
            }
        }
    }

    /**
     * Create or update a connector based upon the provided data
     *
     * @param postData Data used to create or update the connector
     * @throws BusinessException if the connector can't be created or updated
     */
    public void createOrUpdate(@Valid @NotNull ConnectorDto postData) throws BusinessException {
        try {
            update(postData);
        } catch (EntityDoesNotExistsException e) {
            create(postData);
        }
    }

    private ConnectorInstance getConnectorInstance(String connectorName, Integer version) throws EntityDoesNotExistsException {
        ConnectorInstance connectorInstance;
        if (version != null) {
            connectorInstance = getConnectorInstanceByNameAndVersion(connectorName, version);
        } else {
            connectorInstance = connectorService.findLatestByName(connectorName)
                    .orElseThrow(() -> new EntityDoesNotExistsException("Connector with name " + connectorName + " does not exists"));
        }
        return connectorInstance;
    }

    private ConnectorInstance getConnectorInstanceByNameAndVersion(String connectorName, Integer version) throws EntityDoesNotExistsException {
        return connectorService.findByNameAndVersion(connectorName, version)
                .orElseThrow(() -> new EntityDoesNotExistsException("Connector with name " + connectorName + " and version " + version + " does not exists"));
    }
}

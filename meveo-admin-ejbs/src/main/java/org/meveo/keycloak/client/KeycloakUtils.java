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

package org.meveo.keycloak.client;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.meveo.commons.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.NotFoundException;

public class KeycloakUtils {

    private static Logger log = LoggerFactory.getLogger(KeycloakUtils.class);

    /**
     * @param session keycloak session
     * @param keycloakAdminClientConfig keycloak admin client config.
     * @return instance of Keycloak.
     */
    public static Keycloak getKeycloakClient(KeycloakSecurityContext session, KeycloakAdminClientConfig keycloakAdminClientConfig) {

        return KeycloakBuilder.builder() //
                .serverUrl(keycloakAdminClientConfig.getServerUrl()) //
                .realm(keycloakAdminClientConfig.getRealm()) //
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
                .clientId(keycloakAdminClientConfig.getClientId()) //
                .clientSecret(keycloakAdminClientConfig.getClientSecret()) //
                .authorization(session.getTokenString()) //
                .build();
    }

    /**
     * Remove a role representation from list of role representation.
     *
     * @param listRoleRepresentation list of role representation.
     * @param roleRepresentation role representation to remove.
     * @author akadid abdelmounaim
     * @lastModifiedVersion 5.0
     */
    public static List<RoleRepresentation> removeRole(List<RoleRepresentation> listRoleRepresentation, RoleRepresentation roleRepresentation) {
        List<RoleRepresentation> updatedListRoleRepresentation = new ArrayList<>();
        for (RoleRepresentation roleRepresentationItem : listRoleRepresentation) {
            if (!roleRepresentation.getName().equalsIgnoreCase(roleRepresentationItem.getName())) {
                updatedListRoleRepresentation.add(roleRepresentationItem);
            }
        }
        return updatedListRoleRepresentation;
    }

    /**
     * Reads the configuration from system property.
     *
     * @return KeycloakAdminClientConfig
     */
    public static KeycloakAdminClientConfig loadConfig() {
        KeycloakAdminClientConfig keycloakAdminClientConfig = new KeycloakAdminClientConfig();
        try {
            // override from system property
            String keycloakServer = System.getProperty("meveo.keycloak.url");
            if (!StringUtils.isBlank(keycloakServer)) {
                keycloakAdminClientConfig.setServerUrl(keycloakServer);
            }
            String realm = System.getProperty("meveo.keycloak.realm");
            if (!StringUtils.isBlank(realm)) {
                keycloakAdminClientConfig.setRealm(realm);
            }
            String clientId = System.getProperty("meveo.keycloak.client");
            if (!StringUtils.isBlank(clientId)) {
                keycloakAdminClientConfig.setClientId(clientId);
            }
            String clientSecret = System.getProperty("meveo.keycloak.secret");
            if (!StringUtils.isBlank(clientSecret)) {
                keycloakAdminClientConfig.setClientSecret(clientSecret);
            }

            log.debug("Found keycloak configuration: {}", keycloakAdminClientConfig);
        } catch (Exception e) {
            log.error("Error: Loading keycloak admin configuration. " + e.getMessage());
        }
        return keycloakAdminClientConfig;
    }

    public static void addToComposite(Keycloak keycloak, KeycloakAdminClientConfig keycloakAdminClientConfig, String client, String role, String compositeRole){
        final String clientUuid = keycloak.realm(keycloakAdminClientConfig.getRealm())
                .clients()
                .findByClientId(client)
                .get(0)
                .getId();
    	
    	RolesResource rolesResource = keycloak.realm(keycloakAdminClientConfig.getRealm())
                .clients()
                .get(clientUuid)
                .roles();
        

        final List<RoleRepresentation> existingRoles = rolesResource.list();

        final boolean roleExists = existingRoles.stream()
                .anyMatch(r -> r.getName().equals(role));

        if(!roleExists){
            RoleRepresentation roleRepresentation = new RoleRepresentation();
            roleRepresentation.setName(role);
            roleRepresentation.setClientRole(true);
            roleRepresentation.setComposite(false);

            rolesResource.create(roleRepresentation);
        }

        final boolean compositeExists = existingRoles.stream()
                .anyMatch(r -> r.getName().equals(compositeRole));

        if(!compositeExists){
            RoleRepresentation compositeRoleRepresentation = new RoleRepresentation();
            compositeRoleRepresentation.setName(compositeRole);
            compositeRoleRepresentation.setClientRole(true);
            compositeRoleRepresentation.setComposite(true);

            rolesResource.create(compositeRoleRepresentation);
        }

        final RoleResource compositeRoleResource = rolesResource.get(compositeRole);

        final boolean alreadyAdded = compositeRoleResource.getRoleComposites()
                .stream()
                .anyMatch(r -> r.getName().equals(role));

        if(!alreadyAdded){
            final RoleRepresentation roleToAdd = rolesResource.get(role).toRepresentation();
            compositeRoleResource.addComposites(Collections.singletonList(roleToAdd));
        }
    }

    public static void removeRoleInCompositeRole(Keycloak keycloak, KeycloakAdminClientConfig keycloakAdminClientConfig, String role, String compositeRole){
        final String clientUuid = keycloak.realm(keycloakAdminClientConfig.getRealm())
                .clients()
                .findByClientId(keycloakAdminClientConfig.getClientId())
                .get(0)
                .getId();

        final RolesResource rolesResource = keycloak.realm(keycloakAdminClientConfig.getRealm())
                .clients()
                .get(clientUuid)
                .roles();

        final RoleResource compositeRoleResource = rolesResource.get(compositeRole);

        try {
        	final RoleRepresentation roleToDelete = rolesResource.get(role).toRepresentation();
            compositeRoleResource.getRoleComposites().remove(roleToDelete);
        } catch(NotFoundException e) {
        	// Nothing to delete as the role did not exist
        }
    }
}

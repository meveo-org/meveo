package org.meveo.keycloak.client;

import static org.meveo.keycloak.client.KeycloakUtils.getKeycloakClient;
import static org.meveo.keycloak.client.KeycloakUtils.loadConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpStatus;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.RoleDto;
import org.meveo.api.dto.UserDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.StringUtils;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.slf4j.Logger;

/**
 * @author Clément Bareth
 * @author Edward P. Legaspi
 * @author akadid abdelmounaim
 * @since 20.03.2019
 * @lastModifiedVersion 6.0.5
 **/
@Stateless
public class KeycloakAdminClientService {

    @Inject
    private Logger log;

    @Inject
    @CurrentUser
    protected MeveoUser currentUser;

    @Resource
    private SessionContext ctx;
    
    public Set<String> getCurrentUserRoles(String client){
        final KeycloakPrincipal<?> callerPrincipal = (KeycloakPrincipal<?>) ctx.getCallerPrincipal();
        final KeycloakSecurityContext keycloakSecurityContext = callerPrincipal.getKeycloakSecurityContext();

        final AccessToken.Access resourceAccess = keycloakSecurityContext.getToken()
                .getResourceAccess(client);

        if(resourceAccess == null){
            return Collections.emptySet();
        }
        
        return resourceAccess.getRoles();
    }

    public void removeRole(String client, String role){
        final KeycloakPrincipal<?> callerPrincipal = (KeycloakPrincipal<?>) ctx.getCallerPrincipal();
        final KeycloakSecurityContext keycloakSecurityContext = callerPrincipal.getKeycloakSecurityContext();
        KeycloakAdminClientConfig keycloakAdminClientConfig = loadConfig();

        Keycloak keycloak = getKeycloakClient(keycloakSecurityContext, keycloakAdminClientConfig);
        
        final String clientId = keycloak.realm(keycloakAdminClientConfig.getRealm())
                .clients()
                .findByClientId(client)
                .get(0)
                .getId();

        try {
            keycloak.realm(keycloakAdminClientConfig.getRealm())
                    .clients()
                    .get(clientId)
                    .roles()
                    .get(role)
                    .remove();

        } catch (NotFoundException ignored){

        }
    }

    public void createClient(String name){
        final KeycloakPrincipal<?> callerPrincipal = (KeycloakPrincipal<?>) ctx.getCallerPrincipal();
        final KeycloakSecurityContext keycloakSecurityContext = callerPrincipal.getKeycloakSecurityContext();
        KeycloakAdminClientConfig keycloakAdminClientConfig = loadConfig();

        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId(name);
        clientRepresentation.setEnabled(true);
        clientRepresentation.setName(name);
        clientRepresentation.setId(name);
        clientRepresentation.setProtocol("openid-connect");
        clientRepresentation.setSecret(name);

        Keycloak keycloak = getKeycloakClient(keycloakSecurityContext, keycloakAdminClientConfig);

        final ClientsResource clients = keycloak.realm(keycloakAdminClientConfig.getRealm())
                .clients();

        if(clients.findByClientId(name).isEmpty()){
            clients.create(clientRepresentation);
        }
    }

    public void createRole(String client, String name){
        final KeycloakPrincipal<?> callerPrincipal = (KeycloakPrincipal<?>) ctx.getCallerPrincipal();
        final KeycloakSecurityContext keycloakSecurityContext = callerPrincipal.getKeycloakSecurityContext();
        KeycloakAdminClientConfig keycloakAdminClientConfig = loadConfig();

        Keycloak keycloak = getKeycloakClient(keycloakSecurityContext, keycloakAdminClientConfig);

        final String clientId = keycloak.realm(keycloakAdminClientConfig.getRealm())
                .clients()
                .findByClientId(client)
                .get(0)
                .getId();

        RolesResource rolesResource = keycloak.realm(keycloakAdminClientConfig.getRealm())
                .clients()
                .get(clientId)
                .roles();

        final List<RoleRepresentation> existingRoles = rolesResource.list();

        final boolean roleExists = existingRoles.stream()
                .anyMatch(r -> r.getName().equals(name));

        if(!roleExists){
            RoleRepresentation roleRepresentation = new RoleRepresentation();
            roleRepresentation.setComposite(true);
            roleRepresentation.setName(name);
            roleRepresentation.setId(name);

            rolesResource.create(roleRepresentation);
        }
    }
    
    /**
     * Add a role from source client to an exisiting composite role in target client
     * Both roles should already exists.
     *
	 */
    public void addToCompositeCrossClient(String clientSource, String clientTarget, String roleSourceName, String roleTargetName) throws BusinessException {
        String sourceClientId = null;
        String targetClientId = null;
        RoleRepresentation sourceRole = null;
        ClientResource targetClient = null;
        RoleResource targetRoleResource = null;
        	
    	KeycloakAdminClientConfig keycloakAdminClientConfig = loadConfig();
    	clientSource = clientSource != null ? clientSource : keycloakAdminClientConfig.getClientId();
    	clientTarget = clientTarget != null ? clientTarget : keycloakAdminClientConfig.getClientId();
    	
        final KeycloakPrincipal<?> callerPrincipal = (KeycloakPrincipal<?>) ctx.getCallerPrincipal();
        final KeycloakSecurityContext keycloakSecurityContext = callerPrincipal.getKeycloakSecurityContext();

        Keycloak keycloak = getKeycloakClient(keycloakSecurityContext, keycloakAdminClientConfig);

        sourceClientId = keycloak.realm(keycloakAdminClientConfig.getRealm())
                .clients()
                .findByClientId(clientSource)
                .get(0)
                .getId();
        
        targetClientId = keycloak.realm(keycloakAdminClientConfig.getRealm())
                .clients()
                .findByClientId(clientTarget)
                .get(0)
                .getId();
        
        try {
	        sourceRole = keycloak.realm(keycloakAdminClientConfig.getRealm())
	                .clients()
	                .get(sourceClientId)
	                .roles()
	                .get(roleSourceName)
	                .toRepresentation();
	        
        } catch(NotFoundException e) {
        	throw new BusinessException(sourceRole + " does not exists in " + sourceClientId + " client");
        }
        
        targetClient = keycloak.realm(keycloakAdminClientConfig.getRealm()).clients().get(targetClientId);
    	
        try {
        	targetRoleResource = targetClient.roles().get(roleTargetName);
        	targetRoleResource.toRepresentation();
        } catch(NotFoundException e) {
        	throw new BusinessException(roleTargetName + " does not exists in " + targetClientId + " client");
        }

    	targetRoleResource.addComposites(Collections.singletonList(sourceRole));

    }

    /**
     * Add a role from target client to a composite role of an default client.
     * Both roles should already exists.
     *
     * @param clientTarget Id of the client holding the composite role
     * @param roleCompositeSource composite role of the default client=
     * @param roleTargetToAdd role of the target client to add
     * @throws BusinessException if the source composite role does not exists in default client
     */
    public void addToCompositeCrossClient(String clientTarget, String roleCompositeSource, String roleTargetToAdd) throws BusinessException {
        KeycloakAdminClientConfig keycloakAdminClientConfig = loadConfig();
        final KeycloakPrincipal<?> callerPrincipal = (KeycloakPrincipal<?>) ctx.getCallerPrincipal();
        final KeycloakSecurityContext keycloakSecurityContext = callerPrincipal.getKeycloakSecurityContext();

        Keycloak keycloak = getKeycloakClient(keycloakSecurityContext, keycloakAdminClientConfig);

        final String defaultSourceClient = keycloak.realm(keycloakAdminClientConfig.getRealm())
                .clients()
                .findByClientId(keycloakAdminClientConfig.getClientId())
                .get(0)
                .getId();

        final String targetClient = keycloak.realm(keycloakAdminClientConfig.getRealm())
                .clients()
                .findByClientId(clientTarget)
                .get(0)
                .getId();

        final RoleRepresentation roleToAdd;

        try {
            roleToAdd = keycloak.realm(keycloakAdminClientConfig.getRealm())
                    .clients()
                    .get(targetClient)
                    .roles()
                    .get(roleTargetToAdd)
                    .toRepresentation();

        } catch (NotFoundException e) {
            throw new BusinessException("Role " + roleTargetToAdd + " does not exists in client " + clientTarget);
        }

        ClientResource defaultClient = keycloak.realm(keycloakAdminClientConfig.getRealm()).clients().get(defaultSourceClient);
        RoleResource roleResource = defaultClient.roles().get(roleCompositeSource);

        try {
            roleResource.addComposites(Collections.singletonList(roleToAdd));
        } catch(NotFoundException e) {
            throw new BusinessException("Role " + roleCompositeSource + " does not exists in client " + keycloakAdminClientConfig.getClientId());
        }

    }

    /**
     * Add a role to an other composite role for a given client. Create missing roles.
     *
     * @param client Client to manage
     * @param role Role to create and / or add to composite role
     * @param compositeRole Composite role to create and / or where to add the given role
     */
    public void addToComposite(String client, String role, String compositeRole){
        final KeycloakPrincipal<?> callerPrincipal = (KeycloakPrincipal<?>) ctx.getCallerPrincipal();
        final KeycloakSecurityContext keycloakSecurityContext = callerPrincipal.getKeycloakSecurityContext();
        KeycloakAdminClientConfig keycloakAdminClientConfig = loadConfig();

        Keycloak keycloak = getKeycloakClient(keycloakSecurityContext, keycloakAdminClientConfig);

        KeycloakUtils.addToComposite(keycloak, keycloakAdminClientConfig, client, role, compositeRole);

    }

    /**
     * Creates a user in keycloak. Also assigns the role.
     *
     * @param httpServletRequest http request
     * @param postData posted data to API
     * @param provider provider code to be added as attribute
     * @return user created id.
     * @throws BusinessException business exception
     * @throws EntityDoesNotExistsException entity does not exist exception.
     * @lastModifiedVersion 5.0.1
     */
    public String createUser(HttpServletRequest httpServletRequest, UserDto postData, String provider) throws BusinessException, EntityDoesNotExistsException {
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        KeycloakAdminClientConfig keycloakAdminClientConfig = loadConfig();
        Keycloak keycloak = getKeycloakClient(session, keycloakAdminClientConfig);

        // Define user
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setEmailVerified(true);
        if (!StringUtils.isBlank(postData.getUsername())) {
            user.setUsername(postData.getUsername());
        } else {
            user.setUsername(postData.getEmail());
        }
        user.setFirstName(postData.getFirstName());
        user.setLastName(postData.getLastName());
        user.setEmail(postData.getEmail());

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("origin", Collections.singletonList("OPENCELL-API"));
        if (ParamBean.isMultitenancyEnabled() && !StringUtils.isBlank(provider)) {
            attributes.put("provider", Collections.singletonList(provider));
        }

        user.setAttributes(attributes);

        // Get realm
        RealmResource realmResource = keycloak.realm(keycloakAdminClientConfig.getRealm());
        UsersResource usersResource = realmResource.users();

        // check if realm role exists
        // find realm roles and assign to the newly create user
        List<RoleRepresentation> externalRolesRepresentation = new ArrayList<>();
        if (postData.getExternalRoles() != null && !postData.getExternalRoles().isEmpty()) {
            RolesResource rolesResource = realmResource.roles();

            for (RoleDto externalRole : postData.getExternalRoles()) {
                try {
                    RoleRepresentation tempRole = rolesResource.get(externalRole.getName()).toRepresentation();
                    externalRolesRepresentation.add(tempRole);
                } catch (NotFoundException e) {
                    throw new EntityDoesNotExistsException(RoleRepresentation.class, externalRole.getName());
                }
            }
        }

        // Create user (requires manage-users role)
        Response response = usersResource.create(user);

        if (response.getStatus() != Status.CREATED.getStatusCode()) {
            log.error("Keycloak user creation with http status.code={} and reason={}", response.getStatus(), response.getStatusInfo().getReasonPhrase());

            if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                throw new BusinessException("Username or email already exists.");
            } else {
                throw new BusinessException("Unable to create user with httpStatusCode=" + response.getStatus());
            }
        }

        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

        log.debug("User created with userId: {}", userId);

        usersResource.get(userId).roles().realmLevel().add(externalRolesRepresentation);

        ClientRepresentation meveoWebClient = realmResource.clients() //
                .findByClientId(keycloakAdminClientConfig.getClientId()).get(0);

        // Get client level role (requires view-clients role)
        RoleRepresentation apiRole = realmResource.clients().get(meveoWebClient.getId()) //
                .roles().get(KeycloakConstants.ROLE_API_ACCESS).toRepresentation();
        RoleRepresentation guiRole = realmResource.clients().get(meveoWebClient.getId()) //
                .roles().get(KeycloakConstants.ROLE_GUI_ACCESS).toRepresentation();
        RoleRepresentation adminRole = realmResource.clients().get(meveoWebClient.getId()) //
                .roles().get(KeycloakConstants.ROLE_ADMINISTRATEUR).toRepresentation();
        RoleRepresentation userManagementRole = realmResource.clients().get(meveoWebClient.getId()) //
                .roles().get(KeycloakConstants.ROLE_USER_MANAGEMENT).toRepresentation();

        // Assign client level role to user
        usersResource.get(userId).roles() //
                .clientLevel(meveoWebClient.getId()).add(Arrays.asList(apiRole, guiRole, adminRole, userManagementRole));

        // Define password credential
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(postData.getPassword());

        // Set password credential
        usersResource.get(userId).resetPassword(credential);

        return userId;
    }

    /**
     * Updates a user in keycloak. Also assigns the role.
     *
     * @param httpServletRequest http request
     * @param postData posted data.
     * @throws BusinessException business exception.
     * @author akadid abdelmounaim
     * @lastModifiedVersion 5.0
     */
    public void updateUser(HttpServletRequest httpServletRequest, UserDto postData) throws BusinessException {
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        KeycloakAdminClientConfig keycloakAdminClientConfig = loadConfig();
        Keycloak keycloak = getKeycloakClient(session, keycloakAdminClientConfig);

        // Get realm
        RealmResource realmResource = keycloak.realm(keycloakAdminClientConfig.getRealm());
        UsersResource usersResource = realmResource.users();
        try {

            UserRepresentation userRepresentation = getUserRepresentationByUsername(usersResource, postData.getUsername());
            UserResource userResource = usersResource.get(userRepresentation.getId());

            userRepresentation.setFirstName(postData.getFirstName());
            userRepresentation.setLastName(postData.getLastName());
            userRepresentation.setEmail(postData.getEmail());

            // find realm roles and assign to the newly create user
            List<RoleRepresentation> rolesToAdd = new ArrayList<>();
            List<RoleRepresentation> rolesToDelete = realmResource.roles().list();

            if (postData.getExternalRoles() != null && !postData.getExternalRoles().isEmpty()) {
                RolesResource rolesResource = realmResource.roles();

                for (RoleDto externalRole : postData.getExternalRoles()) {
                    try {
                        RoleRepresentation tempRole = rolesResource.get(externalRole.getName()).toRepresentation();
                        rolesToAdd.add(tempRole);
                        rolesToDelete = KeycloakUtils.removeRole(rolesToDelete, tempRole);
                    } catch (NotFoundException e) {
                        throw new EntityDoesNotExistsException(RoleRepresentation.class, externalRole.getName());
                    }
                }

            }
            userResource.update(userRepresentation);

            // add from posted data
            usersResource.get(userRepresentation.getId()).roles().realmLevel().add(rolesToAdd);
            // delete other roles
            usersResource.get(userRepresentation.getId()).roles().realmLevel().remove(rolesToDelete);

            if (!StringUtils.isBlank(postData.getPassword())) {
                // Define password credential
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setTemporary(false);
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(postData.getPassword());

                // Set password credential
                userResource.resetPassword(credential);
            }
        } catch (ClientErrorException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                throw new BusinessException("Username or email already exists.");
            } else {
                throw new BusinessException("Failed updating user with error=" + e.getMessage());
            }
        } catch (Exception e) {
            throw new BusinessException("Failed updating user with error=" + e.getMessage());
        }
    }

    /**
     * Delete a role in composite role.
     *
     * @param role role
     * @param compositeRole composite role
     */
    public void removeRoleInCompositeRole(String role, String compositeRole){
        final KeycloakPrincipal<?> callerPrincipal = (KeycloakPrincipal<?>) ctx.getCallerPrincipal();
        final KeycloakSecurityContext keycloakSecurityContext = callerPrincipal.getKeycloakSecurityContext();
        KeycloakAdminClientConfig keycloakAdminClientConfig = loadConfig();

        Keycloak keycloak = getKeycloakClient(keycloakSecurityContext, keycloakAdminClientConfig);

        KeycloakUtils.removeRoleInCompositeRole(keycloak, keycloakAdminClientConfig, role, compositeRole);

    }

    /**
     * Deletes a user in keycloak.
     *
     * @param httpServletRequest http request
     * @param username user name
     * @throws BusinessException business exception.
     */
    public void deleteUser(HttpServletRequest httpServletRequest, String username) throws BusinessException {
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        KeycloakAdminClientConfig keycloakAdminClientConfig = loadConfig();
        Keycloak keycloak = getKeycloakClient(session, keycloakAdminClientConfig);

        // Get realm
        RealmResource realmResource = keycloak.realm(keycloakAdminClientConfig.getRealm());
        UsersResource usersResource = realmResource.users();
        try {
            UserRepresentation userRepresentation = getUserRepresentationByUsername(usersResource, username);

            // Create user (requires manage-users role)
            Response response = usersResource.delete(userRepresentation.getId());

            if (response.getStatus() != Status.NO_CONTENT.getStatusCode()) {
                log.error("Keycloak user deletion with httpStatusCode={} and reason={}", response.getStatus(), response.getStatusInfo().getReasonPhrase());
                throw new BusinessException("Unable to delete user with httpStatusCode=" + response.getStatus());
            }
        } catch (Exception e) {
            throw new BusinessException("Failed deleting user with error=" + e.getMessage());
        }
    }

    /**
     * Search for a user in keycloak via username.
     *
     * @param httpServletRequest http request
     * @param username user name
     * @return list of role
     * @author akadid abdelmounaim
     * @lastModifiedVersion 5.0
     */
    public List<RoleDto> findUserRoles(HttpServletRequest httpServletRequest, String username) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        KeycloakAdminClientConfig keycloakAdminClientConfig = loadConfig();
        Keycloak keycloak = getKeycloakClient(session, keycloakAdminClientConfig);

        RealmResource realmResource = keycloak.realm(keycloakAdminClientConfig.getRealm());
        UsersResource usersResource = realmResource.users();

        try {
            UserRepresentation userRepresentation = getUserRepresentationByUsername(usersResource, username);

            return userRepresentation != null ? usersResource.get(userRepresentation.getId()).roles().realmLevel().listEffective().stream()
                    .filter(p -> !KeycloakConstants.ROLE_KEYCLOAK_DEFAULT_EXCLUDED.contains(p.getName())).map(p -> new RoleDto(p.getName())).collect(Collectors.toList()) : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * List all the realm roles in keycloak.
     *
     * @param httpServletRequest http servlet request
     * @return list of role
     * @throws BusinessException business exception.
     */
    public List<RoleDto> listRoles(HttpServletRequest httpServletRequest) throws BusinessException {
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        KeycloakAdminClientConfig keycloakAdminClientConfig = loadConfig();
        Keycloak keycloak = getKeycloakClient(session, keycloakAdminClientConfig);

        // Get realm
        RealmResource realmResource = keycloak.realm(keycloakAdminClientConfig.getRealm());

        try {
            return realmResource.roles().list().stream().map(p -> new RoleDto(p.getName())).collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessException("Unable to list role.");
        }
    }

    /**
     * As the search function from keycloack doesn't perform exact search, we need to browse results to pick the exact username
     *
     * @param usersResource Users resource
     * @param username Username
     * @return User information
     * @throws BusinessException business exception.
     * @author akadid abdelmounaim
     * @lastModifiedVersion 5.0
     */
    public UserRepresentation getUserRepresentationByUsername(UsersResource usersResource, String username) throws BusinessException {
        UserRepresentation userRepresentation = null;
        List<UserRepresentation> userRepresentations = usersResource.search(username, null, null, null, null, null);
        for (UserRepresentation userRepresentationListItem : userRepresentations) {
            if (username.equalsIgnoreCase(userRepresentationListItem.getUsername())) {
                userRepresentation = userRepresentationListItem;
            }
        }

        if (userRepresentation == null) {
            throw new BusinessException("Unable to find user on keycloack.");
        }

        return userRepresentation;
    }

    /**
     * Creates a user in keycloak. Also assigns the role. It will add a provider code attribute to the user if multitenancy is activated. The provider will be the same as the
     * current user.
     *
     * @param httpServletRequest http request
     * @param postData posted data to API
     * @return user created id.
     * @throws BusinessException business exception
     * @throws EntityDoesNotExistsException entity does not exist exception.
     * @lastModifiedVersion 5.0.1
     */
    public String createUser(HttpServletRequest httpServletRequest, UserDto postData) throws BusinessException, EntityDoesNotExistsException {
        if (ParamBean.isMultitenancyEnabled() && !StringUtils.isBlank(currentUser.getProviderCode())) {
            return createUser(httpServletRequest, postData, currentUser.getProviderCode());
        }
        return createUser(httpServletRequest, postData, null);
    }

    /**
     * update a role from target client to a composite role of an default client.
     * Both roles should already exists.
     *
     * @param clientTarget Id of the client holding the composite role
     * @param roleCompositeSource composite role of the default client=
     * @param roleTargetToUpdate role of the target client to update
     */
    public void updateToCompositeCrossClient(String clientTarget, String roleCompositeSource, String roleTargetToUpdate) {
        KeycloakAdminClientConfig keycloakAdminClientConfig = loadConfig();
        final KeycloakPrincipal<?> callerPrincipal = (KeycloakPrincipal<?>) ctx.getCallerPrincipal();
        final KeycloakSecurityContext keycloakSecurityContext = callerPrincipal.getKeycloakSecurityContext();

        Keycloak keycloak = getKeycloakClient(keycloakSecurityContext, keycloakAdminClientConfig);

        final String defaultSourceClient = keycloak.realm(keycloakAdminClientConfig.getRealm())
                .clients()
                .findByClientId(keycloakAdminClientConfig.getClientId())
                .get(0)
                .getId();

        final RoleRepresentation roleToUpdate = keycloak.realm(keycloakAdminClientConfig.getRealm())
                .clients()
                .get(clientTarget)
                .roles()
                .get(roleTargetToUpdate)
                .toRepresentation();

        keycloak.realm(keycloakAdminClientConfig.getRealm())
                .clients()
                .get(defaultSourceClient)
                .roles()
                .get(roleCompositeSource)
                .update(roleToUpdate);
    }

    /**
     * retrieve all the roles for a given client & realm
     * @param clientId
     * @param realm
     * @return
     */
    public List<String> getCompositeRolesByRealmClientId(String clientId, String realm){
        final KeycloakPrincipal<?> callerPrincipal = (KeycloakPrincipal<?>) ctx.getCallerPrincipal();
        final KeycloakSecurityContext keycloakSecurityContext = callerPrincipal.getKeycloakSecurityContext();
        KeycloakAdminClientConfig keycloakAdminClientConfig = KeycloakUtils.loadConfig();
        Keycloak keycloak = KeycloakUtils.getKeycloakClient(keycloakSecurityContext, keycloakAdminClientConfig);

        final String clientUuid = keycloak.realm(realm)
                .clients()
                .findByClientId(clientId)
                .get(0)
                .getId();

        RolesResource rolesResource = keycloak.realm(realm)
                .clients()
                .get(clientUuid)
                .roles();

        List<RoleRepresentation> roleRepresentations = rolesResource.list();
        if (roleRepresentations == null){
            return Collections.emptyList();
        }
        List<String> roles = new ArrayList<>();
        for (RoleRepresentation roleRepresentation : roleRepresentations) {
            if (roleRepresentation.isComposite()) {
                roles.add(roleRepresentation.getName());
            }
        }
        return roles;
    }

    public void removeRole(String role){
        final KeycloakPrincipal<?> callerPrincipal = (KeycloakPrincipal<?>) ctx.getCallerPrincipal();
        final KeycloakSecurityContext keycloakSecurityContext = callerPrincipal.getKeycloakSecurityContext();
        KeycloakAdminClientConfig keycloakAdminClientConfig = loadConfig();

        Keycloak keycloak = getKeycloakClient(keycloakSecurityContext, keycloakAdminClientConfig);

        try {
            final String clientUuid = keycloak.realm(keycloakAdminClientConfig.getRealm())
                    .clients()
                    .findByClientId(keycloakAdminClientConfig.getClientId())
                    .get(0)
                    .getId();

            RolesResource rolesResource = keycloak.realm(keycloakAdminClientConfig.getRealm())
                    .clients()
                    .get(clientUuid)
                    .roles();

            rolesResource.get(role).remove();
        } catch (NotFoundException ignored){

        }
    }

	public String getChangePasswordUrl() {
		KeycloakAdminClientConfig kc = loadConfig();
		
		return String.format("%s/realms/%s/account/password", kc.getServerUrl(), kc.getRealm());
	}
}
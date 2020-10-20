/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
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
package org.meveo.service.admin.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.security.BlackListEntry;
import org.meveo.model.security.EntityPermission;
import org.meveo.model.security.EntityPermission.EntityPermissionId;
import org.meveo.model.security.Permission;
import org.meveo.model.security.Role;
import org.meveo.model.security.WhiteListEntry;
import org.meveo.service.base.PersistenceService;

/**
 * @author Edward P. Legaspi
 * @since Apr 4, 2013
 */
@Stateless
public class PermissionService extends PersistenceService<Permission> {

    @Inject
    private RoleService roleService;

    @SuppressWarnings("unchecked")
    @Override
    public List<Permission> list() {
        QueryBuilder qb = new QueryBuilder(Permission.class, "p");
        boolean superAdmin = currentUser.hasRole("superAdminManagement");
        if (!superAdmin) {
            qb.addSqlCriterion("p.permission != :permission", "permission", "superAdminManagement");
        }
        return qb.getQuery(getEntityManager()).getResultList();
    }

    public Permission findByPermission(String permission) {

        try {
            Permission permissionEntity = getEntityManager().createNamedQuery("Permission.getPermission", Permission.class).setParameter("permission", permission)
                .getSingleResult();
            return permissionEntity;

        } catch (NoResultException | NonUniqueResultException e) {
            log.trace("No permission {} was found. Reason {}", permission, e.getClass().getSimpleName());
            return null;
        }

    }
    
    public void removeIfPresent(String permission) throws BusinessException {
    	Permission permissionEntity = this.findByPermission(permission);
    	if(permissionEntity != null) {
    		remove(permissionEntity);
    	}
    }

    public Permission createIfAbsent(String permission, String... rolesToAddTo) throws BusinessException {
        
        // Create permission if does not exist yet
    	boolean created = false;
        Permission permissionEntity = findByPermission(permission);
        if (permissionEntity == null) {
            permissionEntity = new Permission();
            permissionEntity.setName(permission);
            permissionEntity.setPermission(permission);
            this.create(permissionEntity);
            this.flush();
            created = true;
        }

        // Add to a role, creating role first if does not exist yet
        for (String roleName : rolesToAddTo) {
            Role role = roleService.findByName(roleName);
            if (role == null) {
                role = new Role();
                role.setName(roleName);
                role.setDescription(roleName);
                roleService.create(role);
            }

            // Hibernate.initialize(role.getPermissions());
            if (created || !role.getPermissions().contains(permissionEntity)) {
                role.getPermissions().add(permissionEntity);
                roleService.update(role);
            }
        }

        return permissionEntity;
    }
    
    public void addToWhiteList(Role role, Permission permission, String id) {
    	if(id == null) {
    		throw new IllegalArgumentException("Entity id must be provided");
    	}
    	
    	if(!this.getEntityManager().contains(role)) {
    		role = this.getEntityManager().getReference(Role.class, role.getId());
    	}
    	
    	if(!this.getEntityManager().contains(permission)) {
    		permission = this.getEntityManager().getReference(Permission.class, permission.getId());
    	}
    	
    	EntityPermissionId idEntry = new EntityPermissionId(role.getId(), permission.getId(), id);
    	Session session = this.getEntityManager().unwrap(Session.class);
    	WhiteListEntry whiteListEntry = session.find(WhiteListEntry.class, idEntry);
    	
    	if(whiteListEntry == null) {
    		whiteListEntry = new WhiteListEntry();
    	}
    	
    	whiteListEntry.setEntityId(id);
    	whiteListEntry.setRole(role);
    	whiteListEntry.setPermission(permission);
    	
    	session.saveOrUpdate(whiteListEntry);
    }
    
    public void addToBlackList(Role role, Permission permission, String id) {
    	if(id == null) {
    		throw new IllegalArgumentException("Entity id must be provided");
    	}
    	
    	if(!this.getEntityManager().contains(role)) {
    		role = this.getEntityManager().getReference(Role.class, role.getId());
    	}
    	
    	if(!this.getEntityManager().contains(permission)) {
    		permission = this.getEntityManager().getReference(Permission.class, permission.getId());
    	}
    	
    	EntityPermissionId idEntry = new EntityPermissionId(role.getId(), permission.getId(), id);
    	Session session = this.getEntityManager().unwrap(Session.class);
    	BlackListEntry blackListEntry = session.find(BlackListEntry.class, idEntry);
    	
    	if(blackListEntry == null) {
    		blackListEntry = new BlackListEntry();
    	}
    	
    	blackListEntry.setEntityId(id);
    	blackListEntry.setRole(role);
    	blackListEntry.setPermission(permission);
    	
    	session.saveOrUpdate(blackListEntry);
    }
    
    public void removeEntityPermission(Role role, String permission, String id) {
    	Permission p = findByPermission(permission);
    	EntityPermissionId idEntry = new EntityPermissionId(role.getId(), p.getId(), id);
    	EntityPermission entry = this.getEntityManager().find(EntityPermission.class, idEntry);
    	if(entry != null) {
    		this.getEntityManager().remove(entry);
    	}
    }
    
    public void removeEntityPermission(Role role, Permission permission, String id) {
    	EntityPermissionId idEntry = new EntityPermissionId(role.getId(), permission.getId(), id);
    	EntityPermission entry = this.getEntityManager().find(EntityPermission.class, idEntry);
    	if(entry != null) {
    		this.getEntityManager().remove(entry);
    	}
    }
    
    public void removeFromEntityPermissions(String permission, String id) {
    	this.getEntityManager().createQuery("DELETE FROM EntityPermission "
    			+ "WHERE permissionCode = :permissionCode "
    			+ "AND entityId = :entityId")
    		.setParameter("permissionCode", permission)
    		.setParameter("entityId", id)
    		.executeUpdate();
    }
    
    public List<Role> getWhiteListedRoles(Permission permission, String id) {
    	return this.getEntityManager().createQuery("SELECT role FROM WhiteListEntry "
    			+ "WHERE permissionCode = :permissionCode "
    			+ "AND entityId = :entityId", Role.class)
			.setParameter("permissionCode", permission.getPermission())
			.setParameter("entityId", id)
			.getResultList();
    }
    
    public List<Role> getBlackListedRoles(Permission permission, String id) {
    	return this.getEntityManager().createQuery("SELECT role FROM BlackListEntry WHERE permissionCode = :permissionCode AND entityId = :entityId", Role.class)
			.setParameter("permissionCode", permission.getPermission())
			.setParameter("entityId", id)
			.getResultList();
    }
    
    /**
     * 
     * @param permissionName
     * @param entityId
     * @return a map associating each role to its white / black list state for a given permission and entity id. <br>
     * 0 = not blacklisted and not whitelisted, 1 = whitelisted, 2 = blacklisted
     */
    public Map<Role, String> getEntityPermissionByRole(String permissionName, String entityId) {
    	String query = "SELECT r,\r\n" + 
				"    CASE WHEN w IS NOT NULL THEN '1'\r\n" + 
				"         WHEN b IS NOT NULL THEN '2'\r\n" + 
				"         ELSE '0'\r\n" + 
				"    END\r\n" + 
				"FROM Role r\r\n" + 
				"    LEFT JOIN r.whiteList w WITH w.permissionCode = :permissionCode AND w.entityId = :entityId \r\n" + 
				"    LEFT JOIN r.blackList b WITH b.permissionCode = :permissionCode AND b.entityId = :entityId\r\n" + 
				"    INNER JOIN r.permissions p WITH p.permission = :permissionCode \r\n" +
				"WHERE NOT r.name like 'CET%' AND NOT r.name like 'CRT%'";
    	
    	Map<Role, String> collect = this.getEntityManager().createQuery(query, Object[].class)
			.setParameter("permissionCode", permissionName)
			.setParameter("entityId", entityId)
			.getResultStream()
			.collect(Collectors.toMap(r -> (Role) r[0], r -> (String) r[1]));
    	
		return collect;
    }

}

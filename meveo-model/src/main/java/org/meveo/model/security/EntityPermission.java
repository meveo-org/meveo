package org.meveo.model.security;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.meveo.model.security.EntityPermission.EntityPermissionId;

@Entity
@Table(name = "entity_permission")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@IdClass(EntityPermissionId.class)
public class EntityPermission {
	
	@Id
	@JoinColumn(name = "role_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Role role;
	
	@Id
	@JoinColumn(name = "permission_id", nullable = false, referencedColumnName = "id")
	@ManyToOne(fetch = FetchType.LAZY)
	private Permission permission;
	
	@Column(name = "permission")
	@NotNull
	private String permissionCode;
	
	@Id
	@Column(name = "entity_id", nullable = false)
	@NotNull
	private String entityId;
	
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Permission getPermission() {
		return permission;
	}

	public String getPermissionCode() {
		return permissionCode;
	}

	public void setPermission(Permission permission) {
		this.permission = permission;
		this.permissionCode = permission.getPermission();
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public static class EntityPermissionId implements Serializable {

		private static final long serialVersionUID = -5456029385148908605L;
		
		private Long role;
		private Long permission;
		private String entityId;
		
		public EntityPermissionId() {
			super();
		}

		public EntityPermissionId(Long role, Long permission, String entityId) {
			super();
			this.role = role;
			this.permission = permission;
			this.entityId = entityId;
		}
		
		public Long getRole() {
			return role;
		}

		public void setRole(Long role) {
			this.role = role;
		}

		public Long getPermission() {
			return permission;
		}

		public void setPermission(Long permission) {
			this.permission = permission;
		}

		public String getEntityId() {
			return entityId;
		}

		public void setEntityId(String entityId) {
			this.entityId = entityId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
			result = prime * result + ((permission == null) ? 0 : permission.hashCode());
			result = prime * result + ((role == null) ? 0 : role.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EntityPermissionId other = (EntityPermissionId) obj;
			if (entityId == null) {
				if (other.entityId != null)
					return false;
			} else if (!entityId.equals(other.entityId))
				return false;
			if (permission == null) {
				if (other.permission != null)
					return false;
			} else if (!permission.equals(other.permission))
				return false;
			if (role == null) {
				if (other.role != null)
					return false;
			} else if (!role.equals(other.role))
				return false;
			return true;
		}

	}
}

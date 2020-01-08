package org.meveo.model.security;

import java.util.Collection;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.meveo.model.ExportIdentifier;
import org.meveo.model.IEntity;

@Entity
@ExportIdentifier("id")
@Table(name = "adm_permission_category")
public class PermissionCategory implements IEntity<String> {
	
	@Id
    @Column(name = "id")
	@Access(AccessType.PROPERTY)
	private String id;
	
	@OneToMany(mappedBy = "category")
	private Collection<Permission> permissions;
	
	public PermissionCategory() {
		
	}
	
	public PermissionCategory(String id) {
		super();
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;		
	}

	@Override
	public boolean isTransient() {
		return false;
	}

}

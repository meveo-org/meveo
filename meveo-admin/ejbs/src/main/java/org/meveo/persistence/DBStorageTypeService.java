package org.meveo.persistence;

import java.util.List;

import javax.inject.Inject;

import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.model.persistence.DBStorageType;

public class DBStorageTypeService {

	@Inject
	private EntityManagerWrapper emWrapper;
	
	public List<DBStorageType> list() {
		return emWrapper.getEntityManager()
			.createQuery("FROM DBStorageType", DBStorageType.class)
			.getResultList();
	}
}

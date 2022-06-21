package org.meveo.persistence;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.persistence.DBStorageType;

@Transactional
public class DBStorageTypeService implements Serializable {
	
	private static final long serialVersionUID = -8627029131570892361L;
	
	@Inject
	@MeveoJpa
	private transient EntityManagerWrapper emWrapper;
	
	
	public void create(DBStorageType storageType) {
		emWrapper.getEntityManager().merge(storageType);
	}
	
	@SuppressWarnings("unchecked")
	public List<DBStorageType> findTemplateStorages(String template) {
		return (List<DBStorageType>) emWrapper.getEntityManager()
			.createNativeQuery("SELECT dbSt FROM DBStorageType dbSt, CustomEntityTemplate cet, CustomRelationshipTemplate crt \n"
					+ "WHERE (dbSt.code IN cet.availableStorages OR dbSt.code IN crt.availableStorages) \n", DBStorageType.class)
			.getResultList();
	}
	
	public List<DBStorageType> list() {
		return emWrapper.getEntityManager()
			.createQuery("FROM DBStorageType", DBStorageType.class)
			.getResultList();
	}
}
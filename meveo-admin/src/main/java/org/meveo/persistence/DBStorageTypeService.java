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
		String query = "FROM DBStorageType dbSt \n" + 
					   "	WHERE EXISTS (FROM CustomEntityTemplate cet INNER JOIN cet.availableStorages cetStorage WHERE cet.code = :code AND cetStorage.code = dbSt.code) \n" +
					   "	OR EXISTS (FROM CustomRelationshipTemplate crt INNER JOIN crt.availableStorages crtStorage WHERE crt.code = :code AND crtStorage.code = dbSt.code) \n";
		return (List<DBStorageType>) emWrapper.getEntityManager()
			.createQuery(query, DBStorageType.class)
			.setParameter("code", template)
			.getResultList();
	}
	
	public List<DBStorageType> list() {
		return emWrapper.getEntityManager()
			.createQuery("FROM DBStorageType", DBStorageType.class)
			.getResultList();
	}
}

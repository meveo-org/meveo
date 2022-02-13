package org.meveo.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.script.ScriptInterface;

@RequestScoped
public class DBStorageTypeService {
	
	@Inject
	private EntityManagerWrapper emWrapper;
	
	@Inject
	private ScriptInstanceService scriptInstanceService;
	
	private ConcurrentHashMap<DBStorageType, StorageImpl> cache = new ConcurrentHashMap<>();
	
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
	
	public StorageImpl findImplementation(DBStorageType dbStorageType) {
		if (cache.get(dbStorageType) != null) {
			return cache.get(dbStorageType);
		}
		
		StorageImpl result;
		
		if (dbStorageType.getStorageImplScript() != null) {
			ScriptInstance scriptInstance = scriptInstanceService.findById(dbStorageType.getStorageImplScript().getId());
			ScriptInterface scriptInterface = scriptInstanceService.getExecutionEngine(scriptInstance, new HashMap<>());
			if (scriptInterface instanceof StorageImpl) {
				result = (StorageImpl) scriptInterface;
			} else {
				throw new IllegalArgumentException("Script" + scriptInstance.getCode() + " does not implements " + StorageImpl.class);
			}
		} else if (dbStorageType.getStorageImplName() != null) {
			try {
				result = (StorageImpl) CDI.current().select(Class.forName(dbStorageType.getStorageImplName()));
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Storage implementation " + dbStorageType.getStorageImplName() + " not found on classpath");
			}
		} else {
			throw new IllegalArgumentException("Incorrect DBStorage type object");
		}
		
		cache.put(dbStorageType, result);
		return result;
	}
}

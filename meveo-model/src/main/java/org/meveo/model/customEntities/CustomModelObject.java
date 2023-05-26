package org.meveo.model.customEntities;

import java.util.List;
import java.util.Set;

import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.storage.Repository;

public interface CustomModelObject {
	
	String getAppliesTo();

	String getCode();
	
	String getDbTableName();
	
	List<Repository> getRepositories();
	
	Set<DBStorageType> getAvailableStorages();

}

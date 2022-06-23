/**
 * 
 */
package org.meveo.persistence;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.storage.Repository;
import org.slf4j.Logger;

/**
 * Bean that handles the transactions for the cross storage
 * 
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 */
@RequestScoped
public class CrossStorageTransaction {
	
	@Inject
	private DBStorageTypeService dbStorageTypeService;
	
	@Inject
	private StorageImplProvider provider;
	
	private List<StorageImpl> storages; 
	
	@Inject
	private Logger log;
	
	private int stackedCalls = 0;
	
	@PostConstruct
	private void postConstruct() {
		storages = dbStorageTypeService.list()
			.stream()
			.map(provider::findImplementation)
			.collect(Collectors.toList());
		storages.forEach(StorageImpl::init);
	}

	public void beginTransaction(Repository repository, List<DBStorageType> storages) {
		storages.stream()
			.map(provider::findImplementation)
			.forEach(storageImpl -> storageImpl.beginTransaction(repository, stackedCalls));

		stackedCalls++;
	}
	
	public <T> T beginTransaction(Repository repository, DBStorageType storage) {
		return provider.findImplementation(storage).beginTransaction(repository, stackedCalls);
	}

	
	public void commitTransaction(Repository repository, List<DBStorageType> storages) {
		stackedCalls--;
		
		if(stackedCalls == 0) {
			storages.stream()
				.map(provider::findImplementation)
				.forEach(storageImpl -> storageImpl.commitTransaction(repository));
		}
	}
	
	public void rollbackTransaction(Exception exception, List<DBStorageType> storages) {
		log.warn("Transaction rolled back : ", exception);
		stackedCalls--;
		
		storages.stream()
			.map(provider::findImplementation)
			.forEach(storageImpl -> storageImpl.rollbackTransaction(stackedCalls));
	}
	
	@PreDestroy
	private void preDestroy() {
		try {
			storages.forEach(StorageImpl::destroy);
		} catch (Exception e) {
			log.error("Error destroying transaction", e);
		}

	}
}

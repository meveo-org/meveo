/**
 * 
 */
package org.meveo.persistence;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.hibernate.Session;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.impl.Neo4jStorageImpl;
import org.meveo.persistence.impl.SQLStorageImpl;
import org.neo4j.driver.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	@Inject
	private Neo4jStorageImpl neo4jStorageImpl;
	
	@Inject
	private SQLStorageImpl sqlStorageImpl;
	
	private List<StorageImpl> storages; 
	
	private static Logger log = LoggerFactory.getLogger(CrossStorageTransaction.class);
	
	private int stackedCalls = 0;
	
	@PostConstruct
	private void postConstruct() {
		storages = dbStorageTypeService.list()
			.stream()
			.map(provider::findImplementation)
			.collect(Collectors.toList());
		storages.forEach(StorageImpl::init);
	}

	public void beginTransaction(Repository repository, Collection<DBStorageType> storages) {
		for (var storage : storages) {
			var impl = provider.findImplementation(storage);
			for (var storageConf : repository.getStorageConfigurations(storage)) {
				impl.beginTransaction(storageConf, stackedCalls);
			}
		}

		stackedCalls++;
	}
	
	public <T> T beginTransaction(Repository repository, DBStorageType storage) {
		var impl = provider.findImplementation(storage);
		for (var storageConf : repository.getStorageConfigurations(storage)) {
			return impl.beginTransaction(storageConf, stackedCalls);
		}
		return null;
	}

	
	public void commitTransaction(Repository repository, Collection<DBStorageType> storages) {
		stackedCalls--;
		
		if(stackedCalls == 0) {
			for (var storage : storages) {
				var impl = provider.findImplementation(storage);
				for (var storageConf : repository.getStorageConfigurations(storage)) {
					impl.commitTransaction(storageConf);
				}
			}
		}
	}
	
	public void rollbackTransaction(Exception exception, Collection<DBStorageType> storages) {
		log.warn("Transaction rolled back : ", exception);
		stackedCalls--;
		
		storages.stream()
			.map(provider::findImplementation)
			.forEach(storageImpl -> storageImpl.rollbackTransaction(stackedCalls));
	}
	
	public Transaction getNeo4jTransaction(String repository) {
		return neo4jStorageImpl.getNeo4jTransaction(repository);
	}
	
	public Session getHibernateSession(String repository) {
		return sqlStorageImpl.getHibernateSession(repository);
	}
	
	public int getStackedCalls() {
		return stackedCalls;
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

/**
 * 
 */
package org.meveo.persistence;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.meveo.model.storage.Repository;
import org.meveo.persistence.neo4j.base.Neo4jConnectionProvider;
import org.meveo.persistence.sql.SQLConnectionProvider;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
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
	
	private UserTransaction userTx;
	
	@Inject
	private Neo4jConnectionProvider neo4jConnectionProvider;
	
	@Inject
	private SQLConnectionProvider sqlConnectionProvider;
	
	@Inject
	private Logger log;
	
	private Map<String, Session> neo4jSessions = new ConcurrentHashMap<>();
	private Map<String, Transaction> neo4jTransactions = new ConcurrentHashMap<>();
	
	private Map<String, org.hibernate.Session> hibernateSessions = new ConcurrentHashMap<>();
	
	private int stackedCalls = 0;
	
	@PostConstruct
	private void postConstruct() {
		// User transaction might be managed by container instead of bean
		try {
			userTx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
		} catch (Exception e) {
			// NOOP
		}
	}

	public void beginTransaction(Repository repository) {
		try {
			if(userTx != null && userTx.getStatus() == Status.STATUS_NO_TRANSACTION && stackedCalls == 0) {
				userTx.begin();
			}
			stackedCalls++;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		if(repository.getNeo4jConfiguration() != null) {
			getNeo4jTransaction(repository.getNeo4jConfiguration().getCode());
		}
		
		if(repository.getSqlConfiguration() != null) {
			getHibernateSession(repository.getSqlConfigurationCode());
		}
	}
	
	public org.hibernate.Session getHibernateSession(String repository) {
		try {
			if(userTx != null && userTx.getStatus() == Status.STATUS_NO_TRANSACTION) {
				userTx.begin();
			}
			return hibernateSessions.computeIfAbsent(repository, sqlConnectionProvider::getSession);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Transaction getNeo4jTransaction(String repository) {
		Session session = neo4jSessions.computeIfAbsent(repository, neo4jConnectionProvider::getSession);
		if(session == null) {
			throw new RuntimeException("Can't get session for repository " + repository);
		}
		return neo4jTransactions.computeIfAbsent(repository, code -> session.beginTransaction());
	}
	
	public Transaction getUserManagedTx(String repository) {
		Session session = neo4jSessions.computeIfAbsent(repository, neo4jConnectionProvider::getSession);
		if(session == null) {
			throw new RuntimeException("Can't get session for repository " + repository);
		}
		return session.beginTransaction();
	}
	
	public void commitTransaction(Repository repository) {
		stackedCalls--;
		
		if(stackedCalls == 0) {
			if(repository.getNeo4jConfiguration() != null) {
				Transaction neo4jTx = neo4jTransactions.remove(repository.getNeo4jConfiguration().getCode());
				if(neo4jTx == null) {
					throw new IllegalStateException("No running transaction for " + repository.getCode());
				}
				neo4jTx.success();
				neo4jTx.close();
			}
			
			try {
				if(userTx != null) {
					userTx.commit();
				}
			} catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SystemException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void rollbackTransaction(Exception exception) {
		log.warn("Transaction rolled back : ", exception);
		
		stackedCalls--;
		
		neo4jTransactions.values().forEach(Transaction::failure);
		
		if(stackedCalls == 0) {
			neo4jTransactions.values().forEach(Transaction::close);
		}
		
		try {
			if(userTx != null && userTx.getStatus() == Status.STATUS_ACTIVE) {
				if(stackedCalls == 0) {
					userTx.rollback();
				} else {
					userTx.setRollbackOnly();
				}
			}
		} catch (SecurityException | IllegalStateException | SystemException e) {
			throw new RuntimeException(e);
		}
	}
	
	@PreDestroy
	private void preDestroy() {
		try {
			neo4jTransactions.values().forEach(s -> s.close());
			neo4jSessions.values().forEach(Session::close);
			hibernateSessions.values().forEach(s -> s.close());
			neo4jTransactions.clear();
			
			if(userTx != null && userTx.getStatus() == Status.STATUS_ACTIVE) {
				userTx.commit();
			}
		} catch (Exception e) {
			log.error("Error destroying {}", this, e);
		}
	
	}
}

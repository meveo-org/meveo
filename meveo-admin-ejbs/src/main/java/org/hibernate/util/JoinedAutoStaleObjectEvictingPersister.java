package org.hibernate.util;

import java.io.Serializable;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.cache.spi.access.EntityDataAccess;
import org.hibernate.cache.spi.access.NaturalIdDataAccess;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;
import org.hibernate.service.spi.ServiceBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @since
 * @version
 */
public class JoinedAutoStaleObjectEvictingPersister extends JoinedSubclassEntityPersister {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public JoinedAutoStaleObjectEvictingPersister(PersistentClass persistentClass, EntityDataAccess cacheAccessStrategy, NaturalIdDataAccess naturalIdRegionAccessStrategy,
			PersisterCreationContext creationContext) throws HibernateException {
		super(persistentClass, cacheAccessStrategy, naturalIdRegionAccessStrategy, creationContext);
	}

	protected TransactionManager getJtaTransactionManager(SessionFactoryImplementor sessionFactoryImpl) {

		ServiceBinding<JtaPlatform> sb = sessionFactoryImpl.getServiceRegistry().locateServiceBinding(JtaPlatform.class);
		if (sb == null) {
			return null;
		}

		return sb.getService().retrieveTransactionManager();
	}

	@Override
	protected boolean update(Serializable id, Object[] fields, Object[] oldFields, Object rowId, boolean[] includeProperty, int j, Object oldVersion, Object object, String sql,
			SharedSessionContractImplementor session) throws HibernateException {

		try {
			return super.update(id, fields, oldFields, rowId, includeProperty, j, oldVersion, object, sql, session);

		} catch (StaleObjectStateException e) {
			if (hasCache()) {
				try {
					TransactionManager txManager = getJtaTransactionManager(session.getFactory());
					Transaction txn = txManager.suspend();
					try {
						final CacheKey ck = new CacheKey(id, getIdentifierType(), getRootEntityName(), session.getSessionIdentifier().toString(), session.getFactory());
						getCacheAccessStrategy().evict(ck);

					} finally {
						txManager.resume(txn);
					}

				} catch (Throwable t) {
					log.error(t.getMessage());
				}
			}

			throw e; // re-throw caller
		}
	}
}

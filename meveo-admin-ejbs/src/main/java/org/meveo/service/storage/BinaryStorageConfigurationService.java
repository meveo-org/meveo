package org.meveo.service.storage;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.storage.BinaryStorageConfiguration;
import org.meveo.service.base.PersistenceService;

/**
 * @author Edward P. Legaspi
 */
@Stateless
public class BinaryStorageConfigurationService extends PersistenceService<BinaryStorageConfiguration> {

	public BinaryStorageConfiguration findByCode(String code) {
		QueryBuilder qb = new QueryBuilder(BinaryStorageConfiguration.class, "b");
		qb.addCriterion("code", "=", code, true);
		try {
			return (BinaryStorageConfiguration) qb.getQuery(getEntityManager()).getSingleResult();

		} catch (NoResultException e) {
			return null;
		}

	}
}
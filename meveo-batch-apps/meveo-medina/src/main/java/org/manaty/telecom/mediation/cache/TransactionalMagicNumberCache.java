/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.manaty.telecom.mediation.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.ejb.HibernateEntityManager;
import org.manaty.model.telecom.mediation.cdr.MagicNumberCalculator;
import org.manaty.telecom.mediation.MedinaConfig;
import org.manaty.telecom.mediation.MedinaPersistence;
import org.manaty.utils.MagicNumberConverter;
import org.manaty.utils.SQLUtil;

/**
 * Class for managing Medina magic numbers cache. 
 * It supports transactions, because magic numbers also should be rollbacked from cache if whole transaction is rollbacked.
 * 
 * @author Ignas Lelys
 * @created Mar 30, 2009
 */
public final class TransactionalMagicNumberCache {

    private static final Logger logger = Logger.getLogger(TransactionalMagicNumberCache.class);

    private static String LOAD_MEDINA_CDR = "SELECT MC.HASH FROM MEDINA_CDR MC ORDER BY MC.ANALYSIS_DATE ASC";

    private static String INSERT_MEDINA_CDR = "INSERT INTO MEDINA_CDR (ID, ANALYSIS_DATE, HASH) VALUES(" + MedinaConfig.getHashId() + ", SYSDATE, ?)";

    private static String HASH_COLUMN_NAME = "HASH";

    /**
     * Magic numbers loaded to memory. Used to speed up CDR uniqueness check.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final Set cache = Collections.synchronizedSet(new MedinaCache(MedinaConfig.getMagicNumbersCount(),
            MagicNumberCalculator.getInstance().getMagicNumberLenght()));

    /**
     * Active cache transactions list.
     */
    private final Map<String, CacheTransaction> activeTransactions = Collections
            .synchronizedMap(new HashMap<String, CacheTransaction>());

    /**
     * Instance of cache manager.
     */
    private static TransactionalMagicNumberCache instance;

    static {
        try {
            instance = new TransactionalMagicNumberCache();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create cache manager instance!", e);
        }
    }

    /**
     * Static factory method to get instance of cache manager. Manager is
     * singleton.
     */
    public static TransactionalMagicNumberCache getInstance() {
        return instance;
    }

    /**
     * Private constructor.
     */
    private TransactionalMagicNumberCache() {
        super();
        loadCache();
    }

    /**
     * Loads all magic numbers from database and caches them to memory. Old
     * cache is cleared.
     * 
     * @returns Number of entities loaded.
     */
    @SuppressWarnings( { "deprecation", "unchecked" })
    private int loadCache() {
        cache.clear();
        EntityManager em = MedinaPersistence.getEntityManager();
        logger.info("Loading Magic numbers cache to memory...");
        Connection connection = ((HibernateEntityManager) em).getSession().connection();
        int entitiesLoaded = 0;
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            statement = connection.prepareStatement(LOAD_MEDINA_CDR);
            result = statement.executeQuery();
            while (result.next()) {
                entitiesLoaded++;
                cache.add(result.getString(HASH_COLUMN_NAME));
            }
        } catch (SQLException e) {
            logger.error("Magic numbers cache loading failed!", e);
            throw new RuntimeException();
        } finally {
            SQLUtil.closeResultSet(result);
            SQLUtil.closeStatements(statement);
        }
        logger.info(String.format("%s Magic numbers loaded from database to cache.", entitiesLoaded));
        return entitiesLoaded;
    }

    /**
     * Persists all magic numbers from transaction to database.
     * 
     * @param transaction
     *            Cache transaction.
     */
    @SuppressWarnings("deprecation")
    public void persistCacheTransaction(CacheTransaction transaction) {
        EntityManager em = MedinaPersistence.getEntityManager();
        Connection connection = ((HibernateEntityManager) em).getSession().connection();
        PreparedStatement insertStatement = null;
        try {
            insertStatement = connection.prepareStatement(INSERT_MEDINA_CDR);
            long processed = 0L;
            long processedTotal = 0L;
            final long batchSize = MedinaConfig.getSQLBatchSize();
            for (Object hash : transaction.getTempCache().toArray()) {
                insertStatement.setString(1, MagicNumberConverter.convertToString((byte[]) hash));
                insertStatement.addBatch();
                if (++processed == batchSize) {
                    insertStatement.executeBatch();
                    processedTotal += processed;
                    processed = 0L;
                }
            }
            if (processed > 0) {
                processedTotal += processed;
                insertStatement.executeBatch();
            }
            logger.info(String.format("Total %s Magic numbers from cache persisted to database.", processedTotal));
        } catch (SQLException e) {
            logger.error("Persisting Magic numbers cache failed!", e);
            throw new RuntimeException();
        } finally {
            SQLUtil.closeStatements(insertStatement);
        }
        logger.info("Magic numbers cache saved to database successfully.");
    }

    /**
     * Method to access magic numbers cache.
     * 
     * @return Hashes cache.
     */
    // TODO change do not expose
    @SuppressWarnings("rawtypes")
	public Set getCache() {
        return cache;
    }

    /**
     * Creates and returns new transaction for magic numbers.
     * 
     * @return {@link CacheTransaction}
     */
    public CacheTransaction getTransaction() {
        CacheTransaction transaction = new CacheTransaction();
        activeTransactions.put(transaction.getId(), transaction);
        return transaction;
    }

    /**
     * Checks if such magic exists in cache or in any active transaction.
     * 
     * !!! This method checks 'temp' cache transactions, so it can still reject magic number if the same is only in another 'temp' cache and can be later rollbacked. !!!
     * 
     * @param magicNumber
     *            Magic number to check it uniqueness.
     * @return True if such magic number already exists in cache.
     */
    public boolean contains(byte[] magicNumber) {
        // if cache contains magic number
        if (cache.contains(magicNumber)) {
            return true;
        }
        // need to synchronize, because using values() returns new collection
        synchronized (activeTransactions) {
            // if any active transaction contains magic number
            for (CacheTransaction transaction : activeTransactions.values()) {
                 if (transaction.getTempCache().contains(magicNumber)) {
                    return true;
                }
            }
        }
        // otherwise false
        return false;
    }

    /**
     * Commits transaction. Actually all it does, moves magic numbers from
     * transaction's temp cache to main cache and removes that transaction from
     * transactions list. Before committing cache transaction, all magic numbers
     * from temp cache must be persisted to database.
     * 
     * @param transaction Cache transaction to commit.
     */
    @SuppressWarnings("unchecked")
    private void commit(CacheTransaction transaction) {
        // add magic numbers to global cache
        Object[] tempCacheValues = transaction.getTempCache().toArray();
        for (Object magicNumber : tempCacheValues) {
            cache.add(magicNumber);
        }

        // remove transaction from active transactions
        activeTransactions.remove(transaction.getId());
    }

    /**
     * Simply remove from manager's active transactions list.
     * 
     * @param transaction Cache transaction to rollback.
     */
    private void rollback(CacheTransaction transaction) {
        activeTransactions.remove(transaction.getId());
    }

    /**
     * Cache transaction. One transaction per processing file is created. It
     * keeps all magic numbers found in that file.
     * 
     * @author Ignas
     * @created Jun 12, 2009
     */
    @SuppressWarnings("unchecked")
    public class CacheTransaction {

        private String id;

        @SuppressWarnings("rawtypes")
		private Set tempCache = Collections.synchronizedSet(new MedinaCache(MedinaConfig.getMagicNumbersCountInFile(),
                MagicNumberCalculator.getInstance().getMagicNumberLenght()));

        public CacheTransaction() {
            super();
            this.id = UUID.randomUUID().toString();
        }

        public String getId() {
            return id;
        }

        public boolean addToCache(byte[] magicNumber) {
            if (TransactionalMagicNumberCache.getInstance().contains(magicNumber)) {
                return false;
            } else {
                return tempCache.add(magicNumber);
            }
        }

        public void commit() {
            TransactionalMagicNumberCache.getInstance().commit(this);
        }

        public void rollback() {
            TransactionalMagicNumberCache.getInstance().rollback(this);
        }

        @SuppressWarnings("rawtypes")
		private Set getTempCache() {
            return tempCache;
        }
    }

}

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
package org.meveo.core.process;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.ejb.HibernateEntityManager;
import org.meveo.commons.exceptions.ConfigurationException;
import org.meveo.commons.utils.SQLUtils;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.Constants;
import org.meveo.persistence.MeveoPersistence;

import com.google.inject.Inject;

/**
 * Abstract processor. Override {@link getProcessStepChain()} method, to add
 * ticket processing steps you need.
 * 
 * @author Ignas Lelys
 * @created 2009.07.16
 */
public abstract class AbstractProcessor<T> implements Processor<T> {
    
    private static final Logger logger = Logger.getLogger(AbstractProcessor.class);
    
    /** Partition count for performance improvements. */
    public static final int PARTITION_COUNT = 100;
    
    /** First process step, that has link to next one (which has link to next one etc). Chain is configured in Guice module class. */
    protected AbstractProcessStep<T> processStepsChain;
    
    /** 
     * Prepared statements. Every processor has list of actions it has to do with persistence store.
     * This is {@link Map} that contains all queries that is used by concrete processing steps. Concrete
     * implementation of {@link AbstractProcessor} has list of all queries and returns them by overriding
     * {@link getNamedQueries()} method. Concrete processor implementation do not need to worry about prepared statement,
     * closing resources etc. It just provides list of queries as strings.
     */
    protected Map<String, PreparedStatement> statements = new HashMap<String, PreparedStatement>();

    /**
     * Constructor. It initialize all prepared statements provided by concrete Processor subclasses.
     */
    @SuppressWarnings({ "deprecation" })
    @Inject
    public AbstractProcessor(AbstractProcessStep<T> processStepsChain) {
        super();
        this.processStepsChain = processStepsChain;
        EntityManager em = MeveoPersistence.getEntityManager();
        Connection connection = ((HibernateEntityManager) em).getSession().connection();
        try {
            Map<String, String> queries = getNamedQueries();
            for (String queryName : queries.keySet()) {
                statements.put(queryName, connection.prepareStatement(queries.get(queryName)));
            }
        } catch (SQLException e) {
            throw new ConfigurationException("Could not access database", e);
        }
    }

    /**
     * Creates context parameters Map for processing steps and adds STATUS parameter to it, by defaul set to ONGOING.
     * 
     * @see org.meveo.core.process.Processor#process(java.lang.Object, org.meveo.core.inputhandler.TaskExecution)
     */
    public Map<String, Object> process(T ticket, TaskExecution<T> taskExecution) {
        Map<String, Object> contextParameters = new HashMap<String, Object>();
        contextParameters.put(Constants.STATUS, Constants.ONGOING_STATUS);
        contextParameters.put(Constants.ACCEPTED, true);
        processStepsChain.process(ticket, taskExecution, contextParameters);
        return contextParameters;
    }
    
    // TODO generic commit to batch statements etc...
    /**
     * Commit. Real commit logic must be implemented by concrete implementation of processor
     * in {@link doCommit()} method.
     */
    public void commit(TaskExecution<T> taskExecution) {
        try {
            doCommit(taskExecution);
        } catch (SQLException e) {
            logger.error("Database access exception when commiting", e);
            throw new ConfigurationException("Could not access database", e);
        } catch (Throwable t) {
            logger.error("Unexpected exception when commiting", t);
            throw new ConfigurationException("Error occured when committing", t);
        } finally {
            Collection<PreparedStatement> preparedStatementsList = statements.values();
            PreparedStatement[] preparedStatements = preparedStatementsList.toArray(new PreparedStatement[preparedStatementsList.size()]);
            SQLUtils.closeStatements(preparedStatements);
        }
    }
    
    /**
     * @see org.meveo.core.process.Processor#getStatementByName(java.lang.String)
     */
    public PreparedStatement getStatementByName(String statementName) {
        return statements.get(statementName);
    }

    /**
     * Return JDBC queries that will be used in processing process. Subclasses must have hashmap
     * of such queries, and this method must return it.
     */
    protected abstract Map<String, String> getNamedQueries();
    
    /**
     * Commit logic must be implemented in concrete {@link AbstractProcessor} subclasses.
     * 
     * @param taskExecution 
     * @throws SQLException
     */
    protected abstract void doCommit(TaskExecution<T> taskExecution) throws SQLException;

}

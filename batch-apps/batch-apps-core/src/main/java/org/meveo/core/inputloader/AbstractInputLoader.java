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
package org.meveo.core.inputloader;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.ejb.HibernateEntityManager;
import org.meveo.commons.exceptions.ConfigurationException;
import org.meveo.commons.utils.SQLUtils;
import org.meveo.config.MeveoConfig;
import org.meveo.persistence.MeveoPersistence;

import com.google.inject.Inject;

/**
 * Abstract InputLoader class.
 * 
 * @author Ignas Lelys
 * @created Apr 20, 2010
 * 
 */
public abstract class AbstractInputLoader implements InputLoader {

    private static final Logger logger = Logger.getLogger(AbstractInputLoader.class);

    @Inject
    protected MeveoConfig config;

    /**
     * Check if input was not already processed. This is standard implementation
     * that can and probably should be overridden. InputInfo entries shares same
     * database table with different discriminator for each
     * 
     * @param input
     *            Input to check.
     * @param inputName
     *            Original filename.
     * @return true if file was rejected, false otherwise.
     */
    protected boolean isDuplicateInput(File input, String inputName) {
        Statement statement = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Checking if input with name '%s' was processed before...", inputName));
            }
            String applicationName = config.getApplicationName();
            EntityManager em = MeveoPersistence.getEntityManager();
            @SuppressWarnings("deprecation")
            Connection connection = ((HibernateEntityManager) em).getSession().connection();
            statement = connection.createStatement();
            StringBuilder query = new StringBuilder(128);
            query.append("SELECT COUNT(*) FROM ADM_INPUT_HISTORY WHERE NAME = '").append(inputName).append("'").append(
                    " AND INPUT_TYPE = '").append(applicationName).append("'");
            Integer count = SQLUtils.getIntegerAndCloseResultSet(statement.executeQuery(query.toString()));
            if (count > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String
                            .format("Input with name '%s' was found in InputInfo DB entries. Input will be rejected",
                                    inputName));
                }
                return true;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(String
                            .format("Input with name '%s' was not processed before. Input is going to be processed.",
                                    inputName));
                }
                return false;

            }

        } catch (SQLException e) {
            throw new ConfigurationException("Could not access database", e);
        } catch (Exception e) {
            logger.error("Unexpected exception when checking if file was already processed.", e);
            throw new RuntimeException("Unexpected exception performing check on file!");
        } finally {
            SQLUtils.closeStatements(statement);
        }

    }
}

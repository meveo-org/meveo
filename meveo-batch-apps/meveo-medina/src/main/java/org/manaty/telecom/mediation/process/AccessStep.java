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
package org.manaty.telecom.mediation.process;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.CDRStatus;
import org.manaty.telecom.mediation.ConfigurationException;
import org.manaty.telecom.mediation.Queries;
import org.manaty.telecom.mediation.context.MediationContext;
import org.manaty.utils.SQLUtil;
import org.meveo.model.mediation.Access;

/**
 * AccessPoint data is retrieved using IMSI or MSISDN. If no AccessPoint is found return false and set error code to NO_ACCESS.
 * 
 * @author Donatas Remeika
 * @created Mar 6, 2009
 */
public class AccessStep extends AbstractProcessStep {

    public AccessStep(AbstractProcessStep nextStep) {
        super(nextStep);
    }

    /**
     * Does Access Point checking logic.
     */
    @Override
    protected boolean execute(MediationContext context) {
        logger.debug("excuting Access step");
        return loadAccess(context);
    }

    /**
     * Loads Access. This method can be overriden.
     * 
     * @param context Mediation context
     * @return True if AccessPoint was loaded successfully.
     */
    protected boolean loadAccess(MediationContext context) {
        CDR cdr = context.getCdr();
        Access access = null;
        Processor processor = context.getProcessor();
        if (cdr.getOriginUserId() != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Searching access for Ticket by origin UserId = %s", cdr.getOriginUserId()));
            }
            access = getAccess(processor.getAccesses(), processor.getAccessCacheByUserId(), cdr.getOriginUserId(), processor.getStatementFindAccessByUserId());
        }
        if (access == null) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Ticket rejected in Access: no access found for UserId = %s ", cdr.getOriginUserId()));
            }
            context.setAccepted(false);
            context.setStatus(CDRStatus.NO_ACCESS);
            return false;
        } else {
            if (logger.isDebugEnabled()) {
                logger
                    .debug(String
                        .format(
                            "Ticket Acces: id = '%s', providerCode = '%s', subscriptionCode = '%s', activationDate = '%s', status = '%s', statusDate = '%s'",
                            access.getProvider().getId(), access.getSubscription().getCode(),access.getSubscription().getSubscriptionDate(),access.getSubscription().getStatus(),access.getSubscription().getStatusDate()));
            }
            //access.getContexts().add(context);
            context.setOriginAccess(access);
            return true;
        }
    }

    /**
     * Creates Access bean from result.
     * 
     * @param resultList
     *        Query result list.
     * @return Access bean.
     */
    private Access getAccessFromResult(Object[] values) {
        if (values != null) {
            Access access = new Access();
            /*(Long) values[0], (String) values[1], (Long) values[2], (String) values[3], values[4] != null ? new java.util.Date(((Timestamp) values[4])
                .getTime()) : null, values[5] != null ? BillingStatusEnum.valueOf((String) values[5]) : null, values[6] != null ? new java.util.Date(((Timestamp) values[6])
                .getTime()) : null, (Long) values[7], values[8] != null ? new java.util.Date(((Timestamp) values[8]).getTime()) : null, (Boolean) values[9], (String) values[10],
                (String) values[11], (Long) values[12], values[13] != null ? new java.util.Date(((Timestamp) values[13]).getTime()) : null);
            */
            return access;
        }
        return null;
    }


    /**
     * Get AccessPoint data. Check in cache first. Put to cache if not there.
     * 
     * @param fullCache
     *        Full cache by AccessPoint id.
     * @param cache
     *        Cache by given key.
     * @param identifier
     *        IMSI or MSISDN.
     * @param query
     *        Query to search AccessPoint by.
     */
    protected Access getAccess(Map<Long, Access> fullCache, Map<String, Access> cache, String identifier, PreparedStatement statement) {

        if (logger.isDebugEnabled()) {
            if (cache.isEmpty()) {
                logger.debug("Cahce is empty. Not checking cache.");
            } else {
                logger.debug(String.format("Loading access from cache. Using identifier '%s'", identifier));
                logger.debug("Cahce contains following keys:");
                for (String cacheKey : cache.keySet()) {
                    logger.debug(String.format("Key: '%s'", cacheKey));
                }
            }
        }
        Access access = cache.get(identifier);
        if (access == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Access was not found in cache. Checking access in database.");
            }
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Using following queries for loading Access from database:.");
                    logger.debug(String.format("For IMSI search: '%s'", Queries.QUERY_GET_ACCESS_POINT_BY_IMSI));
                    logger.debug(String.format("For MSISDN search: '%s'", Queries.QUERY_GET_ACCESS_POINT_BY_MSISDN));
                }
                statement.setString(1, identifier);
                Object[] values = SQLUtil.getValuesAndCloseResultSet(statement.executeQuery(), Long.class, String.class, Long.class, String.class, Timestamp.class, String.class,
                    Timestamp.class, Long.class, Timestamp.class, Boolean.class, String.class, String.class, Long.class, Timestamp.class);
                access = getAccessFromResult(values);
                if (access != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Access with ID '%s' found.", access.getId()));
                    }
                    Access cachedAccess = fullCache.get(access.getId());
                    if (cachedAccess == null) {
                        fullCache.put(access.getId(), access);
                    } else {
                        access = cachedAccess;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Put Access with identifier '%s' to cache.", identifier));
                    }
                    cache.put(identifier, access);
                }
            } catch (SQLException e) {
                throw new ConfigurationException("Could not access database", e);
            }
        }
        return access;
    }

}

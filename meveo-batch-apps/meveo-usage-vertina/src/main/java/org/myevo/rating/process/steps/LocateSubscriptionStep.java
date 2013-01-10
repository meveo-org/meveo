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
package org.myevo.rating.process.steps;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.meveo.config.MeveoConfig;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.Constants;
import org.meveo.core.process.step.StepExecution;
import org.meveo.model.billing.Subscription;
import org.meveo.persistence.MeveoPersistence;
import org.myevo.rating.model.EDR;
import org.myevo.rating.ticket.EDRTicket;

/**
 * Locate subscription based on Access point. Subscription code is SUBS-PA-<AP id>
 * 
 * @author Andrius Karpavicius
 */
public class LocateSubscriptionStep extends AbstractProcessStep<EDRTicket> {

    private static final Logger logger = Logger.getLogger(LocateSubscriptionStep.class);

    private static final String subscriptionForPAPrefix = "SUBS-PA-";

    public LocateSubscriptionStep(AbstractProcessStep<EDRTicket> nextStep, MeveoConfig config) {
        super(nextStep, config);
    }

    @Override
    protected boolean execute(StepExecution<EDRTicket> stepExecution) {

        EDRTicket edrTicket = stepExecution.getTicket();
        EDR edr = edrTicket.getEdr();

        Subscription subscription = getSubscription(edr.getAccessPointId());
        if (subscription == null) {
            logger.error("Ticket will be rejected. No subscription with code " + edr.getAccessPointId() + " was found");
            stepExecution.addParameter(Constants.ACCEPTED, false);
            return false;

        } else {
            edrTicket.setSubscription(subscription);
            stepExecution.getTaskExecution().setProvider(subscription.getProvider());
            return true;
        }
    }

    private Subscription getSubscription(String code) {

        EntityManager em = MeveoPersistence.getEntityManager();

        if (code.indexOf(subscriptionForPAPrefix) < 0) {
            code = subscriptionForPAPrefix + code;
        }

        logger.debug("Locating Subscription with code : " + code);

        try {
            return (Subscription) em.createQuery("from Subscription where code = :code").setParameter("code", code).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
}
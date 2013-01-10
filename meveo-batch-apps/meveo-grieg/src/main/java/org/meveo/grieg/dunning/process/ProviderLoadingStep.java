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
package org.meveo.grieg.dunning.process;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.grieg.ticket.GriegTicket;
import org.meveo.config.MeveoConfig;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.StepExecution;
import org.meveo.grieg.dunning.ticket.DunningTicket;
import org.meveo.model.crm.Provider;
import org.meveo.persistence.MeveoPersistence;

/**
 * Loads provider from providerCode which is set in ticket.
 * 
 * @author Ignas Lelys
 * @created Apr 19, 2011
 *
 */
public class ProviderLoadingStep extends AbstractProcessStep<GriegTicket> {

    public ProviderLoadingStep(AbstractProcessStep<GriegTicket> nextStep, MeveoConfig config) {
        super(nextStep, config);
    }

    @Override
    protected boolean execute(StepExecution<GriegTicket> stepExecution) {
        TaskExecution<GriegTicket> taskExecution = stepExecution.getTaskExecution();
        
        if (taskExecution.getProvider() != null) {
            DunningTicket ticket = (DunningTicket) stepExecution.getTicket();
            try {
                EntityManager em = MeveoPersistence.getEntityManager();
                Query query = em.createQuery("select p from Provider p where p.code = :code");
                query.setParameter("code", ticket.getProviderCode());
                Provider provider = (Provider) query.getSingleResult();
                taskExecution.setProvider(provider);
            } catch (Exception e) {
                setNotAccepted(stepExecution, "PROVIDER_NOT_LOADED");
                return false;
            }
        }
        
        return true;
    }

}

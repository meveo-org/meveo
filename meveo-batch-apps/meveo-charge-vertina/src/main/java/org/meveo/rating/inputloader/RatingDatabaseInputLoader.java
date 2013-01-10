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
package org.meveo.rating.inputloader;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.commons.utils.StringUtils;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.inputloader.AbstractInputLoader;
import org.meveo.core.inputloader.Input;
import org.meveo.core.inputloader.InputNotLoadedException;
import org.meveo.model.billing.ApplicationChgStatusEnum;
import org.meveo.model.billing.ChargeApplication;
import org.meveo.model.billing.RatedTransaction;
import org.meveo.model.billing.RatedTransactionStatusEnum;
import org.meveo.persistence.MeveoPersistence;
import org.meveo.vertina.VertinaConfig;

import com.google.inject.Inject;

/**
 * Inputloader for rating
 * 
 * @author anasseh
 * @created 03.12.2010
 */

public class RatingDatabaseInputLoader extends AbstractInputLoader {

    private static final Logger logger = Logger.getLogger(RatingDatabaseInputLoader.class);

    
    @Inject
    VertinaConfig vertinaConfig;
    
    @SuppressWarnings("unchecked")
    @Override
    public Input loadInput() throws InputNotLoadedException {
        try {
            logger.info("Load charge applications");
            EntityManager em = MeveoPersistence.getEntityManager();
            List<ChargeApplication> chargeApplications = (List<ChargeApplication>) em
                    .createQuery(
                            "from ChargeApplication where status =:status and provider.code in ("
                                    + StringUtils.getArrayElements(vertinaConfig.getProviderCodes()) + ") ")
                    .setParameter("status", ApplicationChgStatusEnum.WAITING).setMaxResults((int)vertinaConfig.getSQLBatchSize()).getResultList();
           
            //load transactions to re-rate
            List<RatedTransaction> ratedTransactions = (List<RatedTransaction>) em
            .createQuery(
                    "from RatedTransaction where status =:status and provider.code in ("
                            + StringUtils.getArrayElements(vertinaConfig.getProviderCodes()) + ") ")
            .setParameter("status", RatedTransactionStatusEnum.TO_RERATE)
            .setMaxResults((int)vertinaConfig.getSQLBatchSize())
            .getResultList();
            boolean nothingToDo=true;
            ChargeAndTransactionInputObject inputObject = new ChargeAndTransactionInputObject();
            String inputName = "Rating_";
            if (chargeApplications != null && chargeApplications.size() > 0) {
               inputName += chargeApplications.size()+"_";// + "_" + System.currentTimeMillis();
                inputObject.setChargeApplications(chargeApplications);
                nothingToDo=false;
            }
            if(ratedTransactions!=null && ratedTransactions.size()>0){
            	inputName+="R"+ratedTransactions.size()+"_";
                inputObject.setRatedTransactions(ratedTransactions);
                nothingToDo=false;
            }
            if(nothingToDo){
                logger.info("Nothing to do."+Thread.currentThread().getId());
                return null;
            } else {
                logger.info(String.format("Loaded input name: %s", inputName));
                 return new Input(inputName, inputObject);
            }
        } catch (Exception e) {
            logger.error("Unexpected exception when searching chargeApplications", e);
            throw new InputNotLoadedException("Unexpected exception when searching chargeApplications");
        }
    }

    /**
     * @see org.meveo.core.inputloader.InputLoader#handleInputAfterFailure(org.meveo.core.inputloader.Input, java.lang.Throwable)
     */
    @Override
    public void handleInputAfterFailure(Input input, Throwable e) {

    }

    /**
     * @see org.meveo.core.inputloader.InputLoader#handleInputAfterProcessing(org.meveo.core.inputloader.Input, org.meveo.core.inputhandler.TaskExecution)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void handleInputAfterProcessing(Input input, TaskExecution taskExecution) {

    }

}

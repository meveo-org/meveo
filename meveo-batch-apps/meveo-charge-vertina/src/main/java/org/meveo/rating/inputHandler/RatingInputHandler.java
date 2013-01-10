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
package org.meveo.rating.inputHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.core.inputhandler.AbstractInputHandler;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.inputloader.Input;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.core.process.Processor;
import org.meveo.core.process.step.Constants;
import org.meveo.model.billing.ApplicationChgStatusEnum;
import org.meveo.model.billing.ChargeApplication;
import org.meveo.model.billing.RatedTransaction;
import org.meveo.model.billing.RatedTransactionStatusEnum;
import org.meveo.model.catalog.DiscountPlanMatrix;
import org.meveo.model.catalog.PricePlanMatrix;
import org.meveo.persistence.MeveoPersistence;
import org.meveo.rating.inputloader.ChargeAndTransactionInputObject;
import org.meveo.rating.ticket.RatingTicket;
import org.meveo.vertina.constants.VertinaConstants;

import com.google.inject.Inject;

/**
 * InputHandler for Rating engine.
 * 
 * @author anasseh
 * @created 03.12.2010
 */
public class RatingInputHandler extends AbstractInputHandler<RatingTicket> {

    private static final Logger logger = Logger.getLogger(RatingInputHandler.class);

    @Inject
    public RatingInputHandler(Processor<RatingTicket> processor, OutputProducer outputProducer) {
        super(processor, outputProducer);
    }

    @Override
    public TaskExecution<RatingTicket> executeInputHandling(Input input, TaskExecution<RatingTicket> taskExecution)
            throws Exception {
        int loadedChargeApplication = 0;
        int acceptedChargeApplication = 0;
        int rejectedChargeApplication = 0;

        // set time, set price all price plans and run through all charge applications and process them.
        taskExecution.addExecutionContextParameter(VertinaConstants.LIST_PRICE_PLAN_KEY, getPricePlans());
        taskExecution.addExecutionContextParameter(VertinaConstants.LIST_DISCOUNT_PLAN_KEY, getDiscountPlans());
        ChargeAndTransactionInputObject inputObject = (ChargeAndTransactionInputObject) input.getInputObject();
        List<ChargeApplication> chargeApplications = inputObject.getChargeApplications();
        if (chargeApplications != null) {
            logger.info(String.format("Found %s ChargeApplications", chargeApplications.size()));
            for (ChargeApplication chargeApplication : chargeApplications) {
    
                loadedChargeApplication++;
                RatingTicket ticket = new RatingTicket(chargeApplication,null);
                Map<String, Object> ticketContextParameters = processor.process(ticket, taskExecution);
                if ((Boolean) ticketContextParameters.get(Constants.ACCEPTED)) {
                    acceptedChargeApplication++;
                } else {
                    rejectedChargeApplication++;
                    String status = (String) ticketContextParameters.get(Constants.STATUS);
                    logger.info(String.format("Rejecting charge application with id = %s with reason '%s'", chargeApplication.getId(), status));
                    rejectChargeTicket(chargeApplication, status, taskExecution.getInputHistory().getId());
                }
            }
        } else {
            logger.info("No Charge applications found.");
        }
        
        List<RatedTransaction> ratedTransactions = inputObject.getRatedTransactions();
        if (ratedTransactions != null) {
            logger.info(String.format("Found %s ratedTransactions", ratedTransactions.size()));
            for (RatedTransaction ratedTransaction : ratedTransactions) {
    
                loadedChargeApplication++;
                RatingTicket ticket = new RatingTicket(null,ratedTransaction);
                Map<String, Object> ticketContextParameters = processor.process(ticket, taskExecution);
                if ((Boolean) ticketContextParameters.get(Constants.ACCEPTED)) {
                    acceptedChargeApplication++;
                } else {
                    rejectedChargeApplication++;
                    String status = (String) ticketContextParameters.get(Constants.STATUS);
                    logger.info(String.format("Rejecting transaction with id = %s with reason '%s'", ratedTransaction.getId(), status));
                    rejectReratedTicket(ratedTransaction, status, taskExecution.getInputHistory().getId());
                }
            }
        } else {
            logger.info("No transaction to re-rate.");
        }
        
        taskExecution.setParsedTicketsCount(loadedChargeApplication);
        taskExecution.setProcessedTicketsCount(acceptedChargeApplication);
        taskExecution.setRejectedTicketsCount(rejectedChargeApplication);
        return taskExecution;
    }
    
    /**
     * @param chargeApplication
     * @param status
     * @param historyId
     */
    protected void rejectChargeTicket(ChargeApplication chargeApplication, String status, Long historyId) {
        EntityManager em = MeveoPersistence.getEntityManager();
        chargeApplication.setStatus(ApplicationChgStatusEnum.REJECTED);
        chargeApplication.setRejectionReason(status);
        chargeApplication.setInputHistoryId(historyId);
        em.merge(chargeApplication);
    }
    

    /**
     * @param ratedTransaction
     * @param status
     * @param historyId
     */
    protected void rejectReratedTicket(RatedTransaction ratedTransaction, String status, Long historyId) {
        EntityManager em = MeveoPersistence.getEntityManager();
        ratedTransaction.setStatus(RatedTransactionStatusEnum.REJECTED);
        ratedTransaction.setRejectionReason(status);
        ratedTransaction.setInputHistoryId(historyId);
        em.merge(ratedTransaction);
    }
    

    // TODO cache it do not load on each request.
    @SuppressWarnings("unchecked")
    protected HashMap<String,HashMap<String,List<PricePlanMatrix>>> getPricePlans() {
    	HashMap<String,HashMap<String,List<PricePlanMatrix>>> result = new HashMap<String,HashMap<String, List<PricePlanMatrix>>>();
        EntityManager em = MeveoPersistence.getEntityManager();
        List<PricePlanMatrix> allPricePlans =  (List<PricePlanMatrix>) em.createQuery("from PricePlanMatrix").getResultList();
        if(allPricePlans!=null & allPricePlans.size()>0){
        	for(PricePlanMatrix pricePlan : allPricePlans){
            	if(!result.containsKey(pricePlan.getProvider().getCode())){
            		result.put(pricePlan.getProvider().getCode(), new HashMap<String,List<PricePlanMatrix>>());
            	}
            	HashMap<String,List<PricePlanMatrix>> providerPricePlans = result.get(pricePlan.getProvider().getCode());
            	if(!providerPricePlans.containsKey(pricePlan.getEventCode())){
            		providerPricePlans.put(pricePlan.getEventCode(), new ArrayList<PricePlanMatrix>());
             		logger.debug("Added pricePlan for provider="+pricePlan.getProvider().getCode()+" chargeCode="+pricePlan.getEventCode());
            	}
            	providerPricePlans.get(pricePlan.getEventCode()).add(pricePlan);
        	}
        }
        return result;
    }

    // TODO cache it do not load on each request.
    @SuppressWarnings("unchecked")
    protected HashMap<String,HashMap<String,List<DiscountPlanMatrix>>> getDiscountPlans() {
       	HashMap<String,HashMap<String,List<DiscountPlanMatrix>>> result = new HashMap<String,HashMap<String, List<DiscountPlanMatrix>>>();
        EntityManager em = MeveoPersistence.getEntityManager();
        List<DiscountPlanMatrix> allDiscountPlans = (List<DiscountPlanMatrix>) em.createQuery("from DiscountPlanMatrix").getResultList();
        if(allDiscountPlans!=null & allDiscountPlans.size()>0){
        	for(DiscountPlanMatrix discountPlan : allDiscountPlans){
               	if(!result.containsKey(discountPlan.getProvider().getCode())){
            		result.put(discountPlan.getProvider().getCode(), new HashMap<String,List<DiscountPlanMatrix>>());
            	}
            	HashMap<String,List<DiscountPlanMatrix>> providerDiscountPlans = result.get(discountPlan.getProvider().getCode());
             	if(!providerDiscountPlans.containsKey(discountPlan.getEventCode())){
             		providerDiscountPlans.put(discountPlan.getEventCode(), new ArrayList<DiscountPlanMatrix>());
             		logger.debug("Added discountPlan for provider="+discountPlan.getProvider().getCode()+" chargeCode="+discountPlan.getEventCode());
            	}
             	providerDiscountPlans.get(discountPlan.getEventCode()).add(discountPlan);
        	}
        }
        return result;
    }

}

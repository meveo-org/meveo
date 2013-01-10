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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.config.MeveoConfig;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.Constants;
import org.meveo.core.process.step.StepExecution;
import org.meveo.model.catalog.UsagePricePlanItem;
import org.meveo.persistence.MeveoPersistence;
import org.myevo.rating.model.EDR;
import org.myevo.rating.ticket.EDRTicket;

/**
 * Locate Usage price plan based on service code, customer account, usage type, origin zone and roaming
 * 
 * @author Andrius Karpavicius
 * 
 */
public class LocatePriceStep extends AbstractProcessStep<EDRTicket> {

    private static final Logger logger = Logger.getLogger(LocatePriceStep.class);

    public LocatePriceStep(AbstractProcessStep<EDRTicket> nextStep, MeveoConfig config) {
        super(nextStep, config);
    }

    @Override
    protected boolean execute(StepExecution<EDRTicket> stepExecution) {

        Map<String, Map<String, List<UsagePricePlanItem>>> pricePlanMap = getPricePlans();
        EDRTicket edrTicket = stepExecution.getTicket();
        EDR edr = edrTicket.getEdr();
        String serviceCode = edr.getServiceId();
        String type = edr.getType();
        Long customerAccountId = edrTicket.getSubscription().getUserAccount().getBillingAccount().getCustomerAccount().getId();

        UsagePricePlanItem pricePlan = null;
        // Either service code and usage type or service code, customer account and usage type must match
        // Check if there is any matching usage price plan by service code, customer account and usage type combination
        String key = serviceCode + "_" + customerAccountId;
        if (pricePlanMap.containsKey(key) && pricePlanMap.get(key).containsKey(type)) {
            pricePlan = getPricePlan(pricePlanMap.get(key).get(type), edr);
        }

        // Check if there is any matching usage price plan by service code and usage type combination
        if (pricePlan == null) {
            key = serviceCode;
            if (pricePlanMap.containsKey(key) && pricePlanMap.get(key).containsKey(type)) {
                pricePlan = getPricePlan(pricePlanMap.get(key).get(type), edr);
            }
        }

        if (null == pricePlan) {
            logger.error("Ticket will be rejected. No price plan match found for service code " + serviceCode + ", customer account id " + customerAccountId + ", type " + type
                    + ", origin zone " + edr.getOriginZone() + ", roaming " + edr.getRoaming());
            return rejectCurrentTicket(stepExecution);
        }

        edrTicket.setUsagePricePlanItem(pricePlan);
        return true;
    }

    private boolean rejectCurrentTicket(StepExecution<EDRTicket> stepExecution) {
        stepExecution.addParameter(Constants.ACCEPTED, false);
        return false;
    }

    public UsagePricePlanItem getPricePlan(List<UsagePricePlanItem> pricePlanList, EDR edr) {
        UsagePricePlanItem pricePlanItem = null;

        for (UsagePricePlanItem currentPricePlan : pricePlanList) {
            // do parameter matching, continue on false
            // list the parameters that is needed for this, please ensure

            logger.debug("Origin zone validated "
                    + validateParameters(edr.getOriginZone() != null ? edr.getOriginZone().toUpperCase() : null, currentPricePlan.getStringParam1(), null, null));
            logger.debug("'" + edr.getOriginZone() != null ? edr.getOriginZone().toUpperCase() : null + "' : '" + currentPricePlan.getStringParam1() + "'");
            logger.debug("Roaming validated "
                    + validateParameters(edr.getRoaming().toString().toUpperCase(), String.valueOf(currentPricePlan.isBooleanParam1()).toUpperCase(), null, null));
            logger.debug("'" + edr.getRoaming().toString().toUpperCase() + "' : '" + String.valueOf(currentPricePlan.isBooleanParam1()).toUpperCase() + "'");

            if (!validateParameters(edr.getOriginZone() != null ? edr.getOriginZone().toUpperCase() : null, currentPricePlan.getStringParam1(), null, null))
                continue;
            if (!validateParameters(edr.getRoaming().toString().toUpperCase(), String.valueOf(currentPricePlan.isBooleanParam1()).toUpperCase(), null, null))
                continue;

            // all parameters are valid return current price plan
            pricePlanItem = currentPricePlan;
            break;
        }
        return pricePlanItem;
    }

    public Boolean validateParameters(Object PI, Object VI, Object VI_MIN, Object VI_MAX) {
        // rk2 : if VI==null, the condition PI=VI always evaluate to true. if VI="__NULL" then PI=VI reads PI==null.
        // rk3 : If PI is of type Date or number then PI=VI means VI is an interval and VI.min<=PI<VI.max
        if (VI instanceof String) {
            if (((String) VI).equals("__NULL") & !(null == PI)) {
                return false;
            }
        }
        if (PI instanceof Long) {
            if (!((Long) VI_MIN <= (Long) PI & (Long) PI < (Long) VI_MAX)) {
                return false;
            }
        }
        if (PI instanceof Date) {
            if (!(((Date) VI_MIN).before((Date) PI) & ((Date) VI_MAX).after((Date) PI) || ((Date) VI_MIN).equals((Date) PI))) {
                return false;
            }
        }
        if (PI instanceof String) {
            if (null == VI) {
                return true;
            }
            if (!(((String) VI).equals((String) PI))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Construct a hashmap of usage price plans where key is <service template code> or <service template code>_<customer account id> if customer account is specified in a usage.
     * Values is another map with data type as a key and list of usage price plans as values. price plan
     * 
     * @return Hashmap of usage price plans
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Map<String, List<UsagePricePlanItem>>> getPricePlans() {
        Map<String, Map<String, List<UsagePricePlanItem>>> result = new HashMap<String, Map<String, List<UsagePricePlanItem>>>();
        EntityManager em = MeveoPersistence.getEntityManager();
        List<UsagePricePlanItem> allPricePlans = (List<UsagePricePlanItem>) em.createQuery("from UsagePricePlanItem").getResultList();
        if (allPricePlans != null & allPricePlans.size() > 0) {
            for (UsagePricePlanItem pricePlan : allPricePlans) {
                String key = pricePlan.getServiceTemplate().getCode();
                if (pricePlan.getCustomerAccount() != null) {
                    key = key + "_" + pricePlan.getCustomerAccount().getId();
                }

                if (!result.containsKey(key)) {
                    result.put(key, new HashMap<String, List<UsagePricePlanItem>>());
                }
                Map<String, List<UsagePricePlanItem>> providerPricePlans = result.get(key);
                if (!providerPricePlans.containsKey(pricePlan.getType().toString())) {
                    providerPricePlans.put(pricePlan.getType().toString(), new ArrayList<UsagePricePlanItem>());
                    logger.debug("Added pricePlan for Service template with code=" + pricePlan.getServiceTemplate().getCode() + " type=" + pricePlan.getType().toString());
                }
                providerPricePlans.get(pricePlan.getType().toString()).add(pricePlan);
            }
        }
        return result;
    }
}
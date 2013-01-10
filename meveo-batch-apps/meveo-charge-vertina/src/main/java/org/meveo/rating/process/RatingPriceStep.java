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
package org.meveo.rating.process;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.meveo.commons.utils.DateUtils;
import org.meveo.config.MeveoConfig;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.StepExecution;
import org.meveo.model.catalog.DiscountPlanMatrix;
import org.meveo.model.catalog.PricePlanMatrix;
import org.meveo.rating.ticket.RatingTicket;
import org.meveo.vertina.constants.VertinaConstants;

/**
 * Rates a price for a transaction (or re-rate it)
 * 
 * @author smichea
 * @created 20.03.2011
 * 
 */
public class RatingPriceStep extends AbstractProcessStep<RatingTicket> {

	private static final Logger logger = Logger.getLogger(RatingPriceStep.class);

	private static final String WILCARD = "*";

 	
	public RatingPriceStep(AbstractProcessStep<RatingTicket> nextStep, MeveoConfig config) {
		super(nextStep, config);
	}

	/**
	 * @see org.meveo.core.process.step.AbstractProcessStep#execute(org.meveo.core.process.step.StepExecution)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected boolean execute(StepExecution<RatingTicket> stepExecution) {

		RatingTicket ticket = stepExecution.getTicket();
		logger.debug("Getting pricePlan for "+ticket);

		try {
			PricePlanMatrix ratePlan = null;

			HashMap<String,HashMap<String,List<PricePlanMatrix>>> listPricePlanInstance = (HashMap<String,HashMap<String,List<PricePlanMatrix>>>) stepExecution.getTaskExecution().getExecutionContextParameter(
					VertinaConstants.LIST_PRICE_PLAN_KEY);
			if (ticket.amountWithoutTax == null) {
				if(listPricePlanInstance.get(ticket.providerCode)!=null){
					if(listPricePlanInstance.get(ticket.providerCode).get(ticket.chargeCode)!=null){
						ratePlan = ratePrice(listPricePlanInstance.get(ticket.providerCode).get(ticket.chargeCode),ticket);
					}
				}
			}
			stepExecution.addParameter(VertinaConstants.RATE_PLAN, ratePlan);
		} catch (Exception e) {
			logger.error("Error loading rating plan", e);
			setNotAccepted(stepExecution, "UNEXPECTED_RATING_PRICE_ERROR");
			return false;
		}

		try {
			DiscountPlanMatrix discountPlan = null;

			HashMap<String,HashMap<String,List<DiscountPlanMatrix>>> listDiscountPlanInstance = (HashMap<String,HashMap<String,List<DiscountPlanMatrix>>>) stepExecution.getTaskExecution().getExecutionContextParameter(
					VertinaConstants.LIST_DISCOUNT_PLAN_KEY);
			if (ticket.amountWithoutTax == null) {
				if(listDiscountPlanInstance.get(ticket.providerCode)!=null){
					if(listDiscountPlanInstance.get(ticket.providerCode).get(ticket.chargeCode)!=null){
						discountPlan = discountPlan(listDiscountPlanInstance.get(ticket.providerCode).get(ticket.chargeCode),ticket);
					}
				}
			}
			stepExecution.addParameter(VertinaConstants.DISCOUNT_PLAN, discountPlan);
			return true;
		} catch (Exception e) {
			logger.error("Error loading discount plan", e);
			setNotAccepted(stepExecution, "UNEXPECTED_DISCOUNT_PRICE_ERROR");
			return false;
		}
	}

	/**
	 * Loading price plan matrix from provided parameters.
	 */
	private PricePlanMatrix ratePrice(List<PricePlanMatrix> listPricePlan,RatingTicket ticket) {
		logger.debug("ratePrice on "+listPricePlan.size());
		// FIXME: the price plan properties could be null !
		for (PricePlanMatrix pricePlan : listPricePlan) {
				boolean subscriptionDateInPricePlanPeriod = ticket.subscriptionDate == null
				|| ((pricePlan.getStartSubscriptionDate() == null || ticket.subscriptionDate.after(pricePlan.getStartSubscriptionDate()) || ticket.subscriptionDate
						.equals(pricePlan.getStartSubscriptionDate())) && (pricePlan.getEndSubscriptionDate() == null || ticket.subscriptionDate.before(pricePlan
								.getEndSubscriptionDate())));
				if(subscriptionDateInPricePlanPeriod){
					int subscriptionAge = 0;
					if (ticket.subscriptionDate!=null && ticket.applicationDate != null) {
						//logger.info("subscriptionDate=" + subscriptionDate + "->" + DateUtils.addDaysToDate(subscriptionDate, -1));
						subscriptionAge = DateUtils.monthsBetween(ticket.applicationDate, DateUtils.addDaysToDate(ticket.subscriptionDate, -1));
					}
					logger.debug("subscriptionAge=" + subscriptionAge);
					boolean subscriptionMinAgeOK = pricePlan.getMinSubscriptionAgeInMonth() == null || subscriptionAge >= pricePlan.getMinSubscriptionAgeInMonth();
					logger.debug("subscriptionMinAgeOK(" + pricePlan.getMinSubscriptionAgeInMonth() + ")=" + subscriptionMinAgeOK);
					if(subscriptionMinAgeOK){
						boolean subscriptionMaxAgeOK = pricePlan.getMaxSubscriptionAgeInMonth() == null || subscriptionAge < pricePlan.getMaxSubscriptionAgeInMonth();
						logger.debug("subscriptionMaxAgeOK(" + pricePlan.getMaxSubscriptionAgeInMonth() + ")=" + subscriptionMaxAgeOK);

						if(subscriptionMaxAgeOK){
						  boolean applicationDateInPricePlanPeriod = (pricePlan.getStartRatingDate() == null || ticket.applicationDate.after(pricePlan.getStartRatingDate()) || ticket.applicationDate
								.equals(pricePlan.getStartRatingDate())) && (pricePlan.getEndRatingDate() == null || ticket.applicationDate.before(pricePlan.getEndRatingDate()));
						  logger.debug("applicationDateInPricePlanPeriod(" + pricePlan.getStartRatingDate() + " - " + pricePlan.getEndRatingDate() + ")="
								+ applicationDateInPricePlanPeriod);
						  if(applicationDateInPricePlanPeriod){
							boolean criteria1SameInPricePlan = WILCARD.equals(pricePlan.getCriteria1Value())
									|| (pricePlan.getCriteria1Value()!=null && pricePlan.getCriteria1Value().equals(ticket.criteria1))
								|| ((pricePlan.getCriteria1Value() == null || "".equals(pricePlan
													.getCriteria1Value())) && ticket.criteria1 == null);
							logger.debug("criteria1SameInPricePlan("
									+ pricePlan.getCriteria1Value() + ")="
									+ criteria1SameInPricePlan);
							if (criteria1SameInPricePlan) {
								boolean criteria2SameInPricePlan = WILCARD.equals(pricePlan.getCriteria2Value())
										|| (pricePlan.getCriteria2Value()!=null && pricePlan.getCriteria2Value().equals(ticket.criteria2))
									|| ((pricePlan.getCriteria2Value() == null || "".equals(pricePlan
														.getCriteria2Value())) && ticket.criteria2 == null);
								logger.debug("criteria2SameInPricePlan("
										+ pricePlan.getCriteria2Value() + ")="
										+ criteria2SameInPricePlan);
								if (criteria2SameInPricePlan) {
									boolean criteria3SameInPricePlan = WILCARD.equals(pricePlan.getCriteria3Value())
											|| (pricePlan.getCriteria3Value()!=null && pricePlan.getCriteria3Value().equals(ticket.criteria3))
									|| ((pricePlan.getCriteria3Value() == null || "".equals(pricePlan
															.getCriteria3Value())) && ticket.criteria3 == null);
									logger.debug("criteria3SameInPricePlan("
											+ pricePlan.getCriteria3Value()
											+ ")=" + criteria3SameInPricePlan);
									if (criteria3SameInPricePlan) {
										return pricePlan;
									}
								}
							}
						  }
						}
					}
				}
			}
		return null;
	}

	/**
	 * Loading price plan matrix from provided parameters.
	 */
	private DiscountPlanMatrix discountPlan(List<DiscountPlanMatrix> listDiscountPlan,RatingTicket ticket) {
		for (DiscountPlanMatrix discountPlan : listDiscountPlan) {
				
				boolean subscriptionDateInPricePlanPeriod = ticket.subscriptionDate == null
				|| ((discountPlan.getStartSubscriptionDate() == null || ticket.subscriptionDate.after(discountPlan.getStartSubscriptionDate()) || ticket.subscriptionDate
						.equals(discountPlan.getStartSubscriptionDate())) && (discountPlan.getEndSubscriptionDate() == null || ticket.subscriptionDate.before(discountPlan
								.getEndSubscriptionDate())));
				if(subscriptionDateInPricePlanPeriod){
					int subscriptionAge = 0;
					if (ticket.subscriptionDate!=null && ticket.applicationDate != null) {
						//logger.info("subscriptionDate=" + subscriptionDate + "->" + DateUtils.addDaysToDate(subscriptionDate, -1));
						subscriptionAge = DateUtils.monthsBetween(ticket.applicationDate, DateUtils.addDaysToDate(ticket.subscriptionDate, -1));
					}
					logger.debug("subscriptionAge=" + subscriptionAge);
					boolean subscriptionAgeOK = discountPlan.getNbPeriod() == null || subscriptionAge <= discountPlan.getNbPeriod();
					logger.debug("subscriptionAgeOK(" + discountPlan.getNbPeriod() + ")=" + subscriptionAgeOK );

					if(subscriptionAgeOK){
							return discountPlan;
					}
				}
			}
		return null;
	}

	//used for tests
	public PricePlanMatrix ratePrice(List<PricePlanMatrix> pricePlans,
String chargeCode, Date applicationDate,Date subscriptionDate,String criteria1,
String criteria2,String criteria3) {
		RatingTicket ticket = new RatingTicket(null, null);
		ticket.chargeCode=chargeCode;
		ticket.applicationDate=applicationDate;
		ticket.subscriptionDate=subscriptionDate;
		ticket.criteria1=criteria1;
		ticket.criteria2=criteria2;
		ticket.criteria3=criteria3;
		return ratePrice(pricePlans,ticket);
	}

}
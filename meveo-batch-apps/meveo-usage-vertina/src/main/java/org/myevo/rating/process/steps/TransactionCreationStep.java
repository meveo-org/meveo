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

import java.math.BigDecimal;

import org.meveo.config.MeveoConfig;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.StepExecution;
import org.meveo.model.billing.RatedTransaction;
import org.meveo.model.billing.RatedTransactionStatusEnum;
import org.meveo.model.billing.Subscription;
import org.meveo.model.catalog.OneShotChargeTemplate;
import org.meveo.model.catalog.PriceCode;
import org.meveo.vertina.constants.VertinaConstants;
import org.myevo.rating.model.EDR;
import org.myevo.rating.ticket.EDRTicket;

/**
 * Create transactions. For data type EDRs, One transaction for upload and another transaction for download traffic.
 * 
 * @author Andrius Karpavicius
 * 
 */
public class TransactionCreationStep extends AbstractProcessStep<EDRTicket> {

    // private static final Logger logger = Logger.getLogger(TransactionCreationStep.class);

    public TransactionCreationStep(AbstractProcessStep<EDRTicket> nextStep, MeveoConfig config) {
        super(nextStep, config);
    }

    @Override
    protected boolean execute(StepExecution<EDRTicket> stepExecution) {
        /*
         * if (!((Boolean) stepExecution.getTaskExecution().getExecutionContextParameter(Constants.ACCEPTED))) return false;
         */
        EDR edr = stepExecution.getTicket().getEdr();
        PriceCode priceCode = stepExecution.getTicket().getUsagePricePlanItem().getPriceCode();
        Subscription subscription = stepExecution.getTicket().getSubscription();

        if (edr.getUploadVolume() != null && edr.getUploadVolume() > 0L && priceCode.getChargeTemplateOut() != null) {

            OneShotChargeTemplate charge = priceCode.getChargeTemplateOut();

            RatedTransaction transaction = new RatedTransaction();
            transaction.setStatus(RatedTransactionStatusEnum.OPEN);
            transaction.setWallet(subscription.getUserAccount().getWallet());
            transaction.setProvider(subscription.getProvider());
            transaction.setInvoiceSubCategory(charge.getInvoiceSubCategory());

            transaction.setUsageCode(charge.getCode());
            transaction.setSubUsageCode1("DATA");
            transaction.setDescription(charge.getDescription());
            transaction.setSubscription(subscription);
            transaction.setGroupingId(edr.getRoaming(), true);
            transaction.setUsageDate(edr.getConsumptionDate());
            transaction.setUsageAmount(edr.getUploadVolume().intValue());
            transaction.setUsageQuantity(transaction.getUsageAmount());
            transaction.setUnitPrice1(priceCode.getChargeOutPrice1());
            transaction.setUnitPrice2(priceCode.getChargeOutPrice2());
            transaction.setTax(charge.getInvoiceSubCategory().getTax());
            transaction.setTaxCode(charge.getInvoiceSubCategory().getTax().getCode());
            transaction.setTaxPercent(charge.getInvoiceSubCategory().getTax().getPercent());
            transaction.setAmount1(transaction.getUnitPrice1().multiply(new BigDecimal(transaction.getUsageAmount())));
            transaction.setAmount1WithoutTax(transaction.getAmount1());
            transaction.setAmount1Tax(transaction.getAmount1WithoutTax().multiply(transaction.getTaxPercent()));
            transaction.setAmount1WithTax(transaction.getAmount1WithoutTax().add(transaction.getAmount1Tax()));
            if (transaction.getUnitPrice2() != null && transaction.getUnitPrice2().compareTo(BigDecimal.ZERO) > 0) {
                transaction.setAmount2(transaction.getUnitPrice2().multiply(new BigDecimal(transaction.getUsageAmount())));
                transaction.setAmount2WithoutTax(transaction.getAmount2());
                transaction.setAmount2Tax(transaction.getAmount2WithoutTax().multiply(transaction.getTaxPercent()));
                transaction.setAmount2WithTax(transaction.getAmount2WithoutTax().add(transaction.getAmount2Tax()));
            }

            putToTaskExecutionListContextParameter(VertinaConstants.LIST_OF_TRANSACTIONS_KEY, transaction, stepExecution.getTaskExecution());
        }

        if (edr.getDownloadVolume() != null && edr.getDownloadVolume() > 0L && priceCode.getChargeTemplateIn() != null) {

            OneShotChargeTemplate charge = priceCode.getChargeTemplateIn();

            RatedTransaction transaction = new RatedTransaction();
            transaction.setStatus(RatedTransactionStatusEnum.OPEN);
            transaction.setWallet(subscription.getUserAccount().getWallet());
            transaction.setProvider(subscription.getProvider());
            transaction.setInvoiceSubCategory(priceCode.getChargeTemplateIn().getInvoiceSubCategory());

            transaction.setUsageCode(charge.getCode());
            transaction.setSubUsageCode1("DATA");
            transaction.setDescription(charge.getDescription());
            transaction.setSubscription(subscription);
            transaction.setGroupingId(edr.getRoaming(), false);
            transaction.setUsageDate(edr.getConsumptionDate());
            transaction.setUsageAmount(edr.getDownloadVolume().intValue());
            transaction.setUsageQuantity(transaction.getUsageAmount());
            transaction.setUnitPrice1(priceCode.getChargeInPrice1());
            transaction.setUnitPrice2(priceCode.getChargeInPrice2());
            transaction.setTax(charge.getInvoiceSubCategory().getTax());
            transaction.setTaxCode(charge.getInvoiceSubCategory().getTax().getCode());
            transaction.setTaxPercent(charge.getInvoiceSubCategory().getTax().getPercent());
            transaction.setAmount1(transaction.getUnitPrice1().multiply(new BigDecimal(transaction.getUsageAmount())));
            transaction.setAmount1WithoutTax(transaction.getAmount1());
            transaction.setAmount1Tax(transaction.getAmount1WithoutTax().multiply(transaction.getTaxPercent()));
            transaction.setAmount1WithTax(transaction.getAmount1WithoutTax().add(transaction.getAmount1Tax()));
            if (transaction.getUnitPrice2() != null && transaction.getUnitPrice2().compareTo(BigDecimal.ZERO) > 0) {
                transaction.setAmount2(transaction.getUnitPrice2().multiply(new BigDecimal(transaction.getUsageAmount())));
                transaction.setAmount2WithoutTax(transaction.getAmount2());
                transaction.setAmount2Tax(transaction.getAmount2WithoutTax().multiply(transaction.getTaxPercent()));
                transaction.setAmount2WithTax(transaction.getAmount2WithoutTax().add(transaction.getAmount2Tax()));
            }
            putToTaskExecutionListContextParameter(VertinaConstants.LIST_OF_TRANSACTIONS_KEY, transaction, stepExecution.getTaskExecution());
        }

        return true;
    }
}

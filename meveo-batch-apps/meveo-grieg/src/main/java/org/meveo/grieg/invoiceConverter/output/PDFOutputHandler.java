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
package org.meveo.grieg.invoiceConverter.output;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.grieg.GriegConfig;
import org.meveo.commons.services.communication.CampaignService;
import org.meveo.commons.services.communication.MessageService;
import org.meveo.commons.utils.FileUtils;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.outputhandler.OutputHandler;
import org.meveo.core.process.step.Constants;
import org.meveo.grieg.output.CustomizedFieldEnum;
import org.meveo.model.admin.User;
import org.meveo.model.billing.BillingAccount;
import org.meveo.model.billing.Invoice;
import org.meveo.model.communication.Campaign;
import org.meveo.model.communication.CampaignStatusEnum;
import org.meveo.model.communication.MediaEnum;
import org.meveo.model.crm.Provider;
import org.meveo.persistence.MeveoPersistence;

import com.google.inject.Inject;

/**
 * Output handler for PDF files. Move PDF file to the output dir. In case of electronic billing, send a link to selfcare in email.
 * 
 * @author Andrius Karpavicius
 * @created Oct 24, 2011
 * 
 */
public class PDFOutputHandler implements OutputHandler {

    private static final Logger logger = Logger.getLogger(PDFOutputHandler.class);

    @Inject
    protected GriegConfig config;

    private Campaign campaign;
    private MessageService messageService;

    @SuppressWarnings("unchecked")
    @Override
    public void handleOutput(TaskExecution taskExecution) {

        PDFsToDBOutputHandler pdFsToDBOutputHandler = new PDFsToDBOutputHandler();
        pdFsToDBOutputHandler.handleOutput(taskExecution);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMMM yyyy");

        // Email or printshop handling
        Object outputObject = taskExecution.getOutputObject();
        logger.info("Producing PDF outputs. output=" + outputObject);
        if (outputObject != null) {
            if (!(outputObject instanceof List)) {
                logger.error(String.format("In TaskExecution context wrong type of argument was put %s parameter. "
                        + "If you want to use MultipleFileOutputHandler it must be List<String> with list of output file names in temp directory", Constants.OUTPUT_OBJECT));
                throw new IllegalStateException("Wrong outputObject type");
            }
            List<FileInfoHolder> files = (List<FileInfoHolder>) outputObject;
            String outputFilesDirectory = config.getOutputFilesDirectory();

            for (FileInfoHolder pdfFile : files) {
                BillingAccount billingAccount = pdfFile.getBillingAccount();
                Invoice invoice = pdfFile.getInvoice();

                Boolean isElectronicBilling = billingAccount.getElectronicBilling();
                if (isElectronicBilling) {
                    handlePDFFileForElBiling(pdfFile, billingAccount, invoice, taskExecution.getInputObject().getName(), sdf);

                } else {
                    handlePDFFile(pdfFile, billingAccount, invoice, outputFilesDirectory);

                }
            }
            if (campaign != null) {
                scheduleCampaign();
            }
        }
    }

    private void handlePDFFileForElBiling(FileInfoHolder pdfFile, BillingAccount billingAccount, Invoice invoice, String inputObjectName, SimpleDateFormat sdf) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        String name = billingAccount.getName().getTitle().getCode() + " " + billingAccount.getName().getFirstName() + " " + billingAccount.getName().getLastName();
        parameters.put(CustomizedFieldEnum.CUSTOMER_NAME.name(), name);
        parameters.put(CustomizedFieldEnum.AMOUNT_WITH_TAX.name(), (invoice.getAmountWithTax() + "").replace('.', ','));
        parameters.put(CustomizedFieldEnum.INVOICE_DATE.name(), sdf.format(invoice.getInvoiceDate()));
        parameters.put(CustomizedFieldEnum.INVOICE_NUMBER.name(), invoice.getInvoiceNumber());
        parameters.put(CustomizedFieldEnum.CUSTOMER_ACCOUNT_CODE.name(), billingAccount.getCustomerAccount().getCode());
        logger.info("prepare to send email name=" + name);
        try {
            parameters.put(CustomizedFieldEnum.RECIPIENT_ADDRESS.name(), billingAccount.getEmail());
            logger.info("will send to " + billingAccount.getEmail());
            sendEmail(pdfFile.getBillingAccount().getProvider(), inputObjectName, pdfFile.getBillingTemplate(), parameters);
            logger.info("email sent");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("error", e);
        }

    }

    protected void handlePDFFile(FileInfoHolder pdfFile, BillingAccount billingAccount, Invoice invoice, String outputFilesDirectory) {

        File pdfFileFile = new File(pdfFile.getFileName());
        String finalPDFFileName = null;
        if (billingAccount.getInvoicePrefix() != null) {
            finalPDFFileName = new StringBuilder(billingAccount.getInvoicePrefix()).append("_").append(pdfFileFile.getName()).toString();
            pdfFileFile = FileUtils.renameFile(pdfFileFile, finalPDFFileName);
        }

        logger.info(String.format("Moving PDF file from to %s dir", pdfFileFile.getAbsolutePath(), outputFilesDirectory));
        FileUtils.moveFile(outputFilesDirectory, pdfFileFile, pdfFileFile.getName());

    }

    private void sendEmail(Provider provider, String campaignName, String templateCode, HashMap<String, String> parameters) {
        Campaign campaign = getCampaign(provider, campaignName);
        logger.info("add message " + templateCode + " to the campaign:" + campaign.getCode());
        getMessageService().addMessageToAttachedCampaign(campaign, MediaEnum.EMAIL, templateCode, parameters);
    }

    private MessageService getMessageService() {
        if (messageService == null) {
            messageService = new MessageService(MeveoPersistence.getEntityManager());
        }
        return messageService;
    }

    private synchronized Campaign getCampaign(Provider provider, String campaignName) {
        if (campaign == null) {
            CampaignService campaignService = new CampaignService(MeveoPersistence.getEntityManager());
            // String campaignCode = "INV_"+provider.getCode()+sdf.format(new
            // Date());
            try {
                campaign = campaignService.findByCode(campaignName, provider.getCode());
            } catch (Exception e) {
            }
            if (campaign == null) {
                campaign = new Campaign();
                campaign.setProvider(provider);
                campaign.setMedia(MediaEnum.EMAIL);
                campaign.setCode(campaignName);
                logger.info("create the campaign:" + campaign.getCode());
                campaignService.create(campaign, getUser(new Long(1)), provider);
            } else if (campaign.getStatus() != null) {
                throw new RuntimeException("Campaign " + campaign.getCode() + " has already been scheduled (its status is " + campaign.getStatus() + ")");
            }
        }
        return campaign;
    }

    private synchronized void scheduleCampaign() {
        if (campaign != null) {
            logger.info("schedule the campaign:" + campaign.getCode());
            campaign.setStatus(CampaignStatusEnum.SCHEDULED);
            campaign.setScheduleDate(new Date());
            CampaignService campaignService = new CampaignService(MeveoPersistence.getEntityManager());
            campaignService.update(campaign, getUser(new Long(1)), campaign.getProvider());
        }
    }

    public User getUser(Long userId) {
        EntityManager em = MeveoPersistence.getEntityManager();
        User userBayad = null;
        userBayad = (User) em.createQuery("from " + User.class.getSimpleName() + " where id =:id").setParameter("id", userId).getSingleResult();
        return userBayad;
    }
}

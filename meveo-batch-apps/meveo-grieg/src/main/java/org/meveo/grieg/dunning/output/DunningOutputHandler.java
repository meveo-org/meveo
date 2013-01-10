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
package org.meveo.grieg.dunning.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import org.meveo.grieg.dunning.ticket.DunningTicket;
import org.meveo.grieg.output.CustomizedFieldEnum;
import org.meveo.grieg.output.FileNameGenerator;
import org.meveo.model.admin.User;
import org.meveo.model.communication.Campaign;
import org.meveo.model.communication.CampaignStatusEnum;
import org.meveo.model.communication.MediaEnum;
import org.meveo.model.communication.PriorityEnum;
import org.meveo.model.crm.Provider;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.DunningActionTypeEnum;
import org.meveo.persistence.MeveoPersistence;

import com.google.inject.Inject;

/**
 * Output handler for maileva. If EMAIL action dunning type - add compain
 * commcenter (wich later will be handled by batch job). If LETTER action
 * dunning type - then create zip archive in output dir which contains pdf file
 * from invoice in db and maileva.jps file.
 * 
 * @author Ignas Lelys
 * @created Jan 11, 2011
 * 
 */
public class DunningOutputHandler implements OutputHandler {

    private static final String MAILEVA_FILENAME = "maileva.jps";
    private static final String MAILEVA_FILENAME_OUT = "maileva.pjs";

    private static final Logger logger = Logger.getLogger(DunningOutputHandler.class);

    private static String newline = System.getProperty("line.separator");

    @Inject
    private GriegConfig config;

    private Campaign campaign;
    private MessageService messageService;

    @SuppressWarnings("unchecked")
    @Override
    public void handleOutput(TaskExecution taskExecution) {

        Object outputObject = taskExecution.getOutputObject();
        logger.info("Producing maileva outputs. output=" + outputObject);
        if (outputObject != null) {
            if (!(outputObject instanceof List)) {
                logger.error(String.format("In TaskExecution context wrong type of argument was put %s parameter. ", Constants.OUTPUT_OBJECT));
                throw new IllegalStateException("Wrong outputObject type");
            }
            List<DunningAction> dunningActions = (List<DunningAction>) outputObject;
            String outputFilesDirectory = config.getOutputFilesDirectory();
            String resDirectory = config.getResourcesFilesDirectory();
            String tempDirectory = config.getTempFilesDirectory();
            for (DunningAction dunningAction : dunningActions) {

                if (DunningActionTypeEnum.EMAIL.name().equals(dunningAction.getDunningTicket().getActionType())) {
                	String customerAccountName = getNotNull(dunningAction.getDunningTicket().getTitle())+" "+
                			                     getNotNull(dunningAction.getDunningTicket().getFirstName())+" "+
                					             getNotNull(dunningAction.getDunningTicket().getLastName());
                    HashMap<String, String> parameters = new HashMap<String, String>();                   
                    parameters.put(CustomizedFieldEnum.CUSTOMER_ACCOUNT_TITLE.name(),getNotNull( dunningAction.getDunningTicket().getTitle() ));
                    parameters.put(CustomizedFieldEnum.AMOUNT_WITH_TAX.name(), getNotNull(dunningAction.getDunningTicket().getAmountWithTax() + ""));
                    parameters.put(CustomizedFieldEnum.LETTER_DATE.name(), getNotNull(dunningAction.getDunningTicket().getProcessDate()+ ""));
                    parameters.put(CustomizedFieldEnum.INVOICE_DATE.name(), getNotNull(dunningAction.getDunningTicket().getInvoiceDate() + ""));
                    parameters.put(CustomizedFieldEnum.INVOICE_NUMBER.name(), getNotNull(dunningAction.getDunningTicket().getInvoiceNumber() + ""));
                    parameters.put(CustomizedFieldEnum.AMOUNT_EXIGIBLE.name(),getNotNull( dunningAction.getDunningTicket().getSold()));
                    parameters.put(CustomizedFieldEnum.CUSTOMER_ACCOUNT_NAME.name(), getNotNull(customerAccountName));
                    parameters.put(CustomizedFieldEnum.CUSTOMER_ACCOUNT_CODE.name(), getNotNull(dunningAction.getDunningTicket().getCustomerAccountCode()));
                    parameters.put(CustomizedFieldEnum.CUSTOMER_ACCOUNT_DESCRIPTION.name(), getNotNull(dunningAction.getDunningTicket().getCustomerAccountDescription()));
                    parameters.put(CustomizedFieldEnum.CUSTOMER_ACCOUNT_ADDRESS_1.name(),getNotNull( dunningAction.getDunningTicket().getAddress1()));
                    parameters.put(CustomizedFieldEnum.CUSTOMER_ACCOUNT_ADDRESS_2.name(),getNotNull( dunningAction.getDunningTicket().getAddress2()));
                    parameters.put(CustomizedFieldEnum.CUSTOMER_ACCOUNT_ADDRESS_3.name(),getNotNull(dunningAction.getDunningTicket().getAddress3()));                                                       
                    parameters.put(CustomizedFieldEnum.CUSTOMER_ACCOUNT_ZIP_CITY.name(),getNotNull(dunningAction.getDunningTicket().getZipCode())+" "+getNotNull(dunningAction.getDunningTicket().getCity()));
                    
                    logger.info("prepare to send email name=" + dunningAction.getDunningTicket().getMail());
                    try {
                        parameters.put(CustomizedFieldEnum.RECIPIENT_ADDRESS.name(), dunningAction.getDunningTicket().getMail());
                        logger.info("will send to " + dunningAction.getDunningTicket().getMail());
                        Provider provider = MeveoPersistence.getEntityManager()
                                .find(CustomerAccount.class, dunningAction.getDunningTicket().getIdCustomerAccount()).getProvider();
                        parameters.put("CC_0",getDefaultFromMail(provider.getId()));
                        parameters.put("CC_1",dunningAction.getDunningTicket().getMailCC());
                        sendEmail(provider, taskExecution.getInputObject().getName(), dunningAction.getDunningTicket().getTemplate(), parameters);
                        logger.info("email sent");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        logger.error("error", e);
                    }

                } else {
                    String mailevaFileName = new StringBuilder(resDirectory).append(File.separator).append("dunning").append(File.separator)
                            .append(File.separator).append(MAILEVA_FILENAME).toString();
                    String modifiedMailevaFileName = new StringBuilder(tempDirectory).append(File.separator).append(MAILEVA_FILENAME_OUT).toString();
                    try {
                        replaceELInTemplate(mailevaFileName, modifiedMailevaFileName, dunningAction.getDunningTicket());
                    } catch (IOException e) {
                        logger.error("error", e); // TODO
                    }
                    String zipName = getZipName(dunningAction.getDunningTicket().getCustomerAccountCode(),
                            FileNameGenerator.formatInvoiceDate(new Date()));
                    String fullZipFileName = new StringBuilder(tempDirectory).append(File.separator).append(zipName).toString();
                    logger.info(String.format("Producing zip file '%s'", fullZipFileName));
                    // add to archive original pdf file and modified maileva.jps
                    // and save zip file in temp dir
                    FileUtils.createZipArchive(fullZipFileName, dunningAction.getFileName(), modifiedMailevaFileName);

                    logger.info(String.format("Moving zip file from %s dir to %s dir", tempDirectory, outputFilesDirectory));
                    // after zip file was created move it to output dir
                    FileUtils.moveFile(outputFilesDirectory, new File(fullZipFileName), zipName);
                }
            }
            if (campaign != null) {
                scheduleCampaign();
            }
        }
    }

    private void sendEmail(Provider provider, String campaignName, String templateCode, HashMap<String, String> parameters) {
        Campaign campaign = getCampaign(provider, campaignName);
        logger.info("add message " + templateCode + " to the campaign:" + campaign.getCode());
        getMessageService().addMessageToAttachedCampaign(campaign, MediaEnum.EMAIL, templateCode, parameters,PriorityEnum.URGENT);
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
            }
        }
        return campaign;
    }

    private void scheduleCampaign() {
        if (campaign != null) {
            logger.info("schedule the campaign:" + campaign.getCode());
            campaign.setStatus(CampaignStatusEnum.SCHEDULED);
            campaign.setScheduleDate(new Date());
            CampaignService campaignService = new CampaignService(MeveoPersistence.getEntityManager());
            campaignService.update(campaign, getUser(new Long(1)), campaign.getProvider());
        }
    }

    private void replaceELInTemplate(String mailevaTemplateFileName, String mailevaFileName, DunningTicket dunningTicket) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(mailevaFileName));
        try {
            String content = FileUtils.getFileAsString(mailevaTemplateFileName);
            content = content.replace("#{customerAccount.id}", dunningTicket.getIdCustomerAccount().toString());            
            content = content.replace("#{customerAccount.name.firstName}", dunningTicket.getFirstName());
            content = content.replace("#{customerAccount.name.lastName}", dunningTicket.getLastName());
            content = content.replace("#{customerAccount.address.address1}", dunningTicket.getAddress1());
            content = content.replace("#{customerAccount.address.address2}", dunningTicket.getAddress2());
            content = content.replace("#{customerAccount.address.address3}", dunningTicket.getAddress3());
            content = content.replace("#{customerAccount.address.postalCode}", dunningTicket.getZipCode());
            content = content.replace("#{customerAccount.address.city}", dunningTicket.getCity());
            content = content.replace("#{customerAccount.address.country}", dunningTicket.getCountry());
            content = content.replace("#{customerAccount.address.country.code}", "FR");
            writer.write(content);
            writer.write(newline);

        } finally {
            writer.flush();
            writer.close();
        }
    }

    private String getZipName(String customerAccountCode, String date) {
        return new StringBuilder(date).append("_").append(customerAccountCode).append(".zcou").toString();
    }

    public User getUser(Long userId) {
        EntityManager em = MeveoPersistence.getEntityManager();
        User userBayad = null;
        userBayad = (User) em.createQuery("from " + User.class.getSimpleName() + " where id =:id").setParameter("id", userId).getSingleResult();
        return userBayad;
    }
    

	public static String getDefaultFromMail(Long providerID) {
		String from = null;
		try {
			EntityManager em = MeveoPersistence.getEntityManager();
			from = (String) em.createQuery("select ems.defaultFromEmail from EmailSenderConfig ems where ems.provider.id=:providerId")
					.setParameter("providerId", providerID).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return from;
	}
    
private String getNotNull(String str){
	if(str == null){
		return "";
	}
	return str;
}
}

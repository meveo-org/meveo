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
package org.grieg.communication.inputLoader;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.grieg.communication.InvalidTemplateException;
import org.grieg.communication.ticket.MessageTicket;
import org.grieg.constants.GriegConstants;
import org.meveo.core.inputhandler.AbstractInputHandler;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.inputloader.Input;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.core.process.Processor;
import org.meveo.core.process.step.Constants;
import org.meveo.model.communication.Campaign;
import org.meveo.model.communication.MediaEnum;
import org.meveo.model.communication.Message;
import org.meveo.model.communication.MessageSenderConfig;
import org.meveo.model.communication.MessageStatusEnum;
import org.meveo.model.communication.MessageTemplate;
import org.meveo.model.crm.Provider;
import org.meveo.persistence.MeveoPersistence;

import com.google.inject.Inject;

public class MessageInputHandler extends AbstractInputHandler<MessageTicket> {

	 private static final Logger logger = Logger.getLogger(MessageInputHandler.class);

	 @Inject
	 public MessageInputHandler(Processor<MessageTicket> processor,
			OutputProducer outputProducer) {
		super(processor, outputProducer);
	}

	@Override
	public TaskExecution<MessageTicket> executeInputHandling(Input input,
			TaskExecution<MessageTicket> taskExecution) throws Exception {
        //logger.debug("executeInputHandling");
        int loadedMessages = 0;
        int acceptedMessages = 0;
        int rejectedMessages = 0;
        MessageInputObject inputObject = (MessageInputObject)input.getInputObject();
		Campaign campaign=inputObject.getCampaign();
		taskExecution.addExecutionContextParameter(GriegConstants.MEDIA_TEMPLATES,getMediaTemplates(campaign.getProvider()));
		taskExecution.addExecutionContextParameter(GriegConstants.MEDIA_CONFIG,getMediaConfig(campaign.getProvider()));
		List<Message> messages = inputObject.getMessages();
		if(messages!=null){
			 logger.debug(String.format("Found %s Messages", messages.size()));
	         for(Message message:messages){
	        	 loadedMessages++;
	        	 processMessageTicket(message);
	        	 MessageTicket messageTicket= new MessageTicket(message);
	        	 Map<String, Object> ticketContextParameters = processor.process(messageTicket, taskExecution);
	             if ((Boolean) ticketContextParameters.get(Constants.ACCEPTED)) {
	                    acceptedMessages++;
	                    acceptMessageTicket(message);
	              } else {
	                    rejectedMessages++;
	                    String status = (String) ticketContextParameters.get(Constants.STATUS);
	                    logger.info(String.format("Rejecting message with id = %s with reason '%s'", message.getId(), status));
	                    rejectMessageTicket(message, status);
	              }
	        	 
	         }
		}else {
            logger.info("No Message found.");
        }

        taskExecution.setParsedTicketsCount(loadedMessages);
        taskExecution.setProcessedTicketsCount(acceptedMessages);
        taskExecution.setRejectedTicketsCount(rejectedMessages);
        return taskExecution;
	}


	private void processMessageTicket(Message message) {
		EntityManager em = MeveoPersistence.getEntityManager();
		message.setStatus(MessageStatusEnum.PROCESSING);
		em.merge(message);		
	}

	private void acceptMessageTicket(Message message) {
		EntityManager em = MeveoPersistence.getEntityManager();
		message.setStatus(MessageStatusEnum.TREATED);
		em.merge(message);		
	}

	private void rejectMessageTicket(Message message, String status) {
		EntityManager em = MeveoPersistence.getEntityManager();
		message.setStatus(MessageStatusEnum.REJECTED);
		message.setRejectionReason(status);
		em.merge(message);
		
	}

	@SuppressWarnings("unchecked")
	private HashMap<MediaEnum, HashMap<String , MessageTemplate>> getMediaTemplates(Provider provider) throws InvalidTemplateException {
		logger.debug("getMediaTemplates (provider="+provider==null?null:provider.getCode()+"");
		HashMap<MediaEnum, HashMap<String , MessageTemplate>> result=new HashMap<MediaEnum, HashMap<String,MessageTemplate>>();
		EntityManager em = MeveoPersistence.getEntityManager();
		List<MessageTemplate> allTemplates = (List<MessageTemplate>)em.createQuery("from "+MessageTemplate.class.getSimpleName()+" where provider=:provider and disabled=false")
		.setParameter("provider", provider).getResultList();
		if(allTemplates!=null && allTemplates.size()>0){
			 logger.debug("found "+allTemplates.size()+" templates");
				for(MessageTemplate template :  allTemplates){
				HashMap<String , MessageTemplate> mediaTemplates = null;
				if(result.containsKey(template.getMedia())){
					mediaTemplates = result.get(template.getMedia());
				} else {
					mediaTemplates = new HashMap<String, MessageTemplate>();
					logger.debug("put  "+template.getMedia()+","+ mediaTemplates);
					result.put(template.getMedia(), mediaTemplates);
				}
				if(mediaTemplates.containsKey(template.getCode())){
					logger.error("Duplicate Template "+template.getCode());
					throw new InvalidTemplateException("DUPLICATE_TEMPLATE");
				} else {
					if((template.getStartDate()==null || template.getStartDate().before(new Date()))
				      && (template.getEndDate()==null || template.getEndDate().after(new Date()))){
						mediaTemplates.put(template.getCode(), template);
					} else {
						logger.info("Not active Template "+template.getCode()+" ("+template.getId()+")");
					}
				}
			}
		}
        return result;
	}
	
	@SuppressWarnings("unchecked")
	private HashMap<MediaEnum, List<MessageSenderConfig>> getMediaConfig(Provider provider) {
		HashMap<MediaEnum, List<MessageSenderConfig>> result = new HashMap<MediaEnum, List<MessageSenderConfig>>();
		EntityManager em = MeveoPersistence.getEntityManager();
		List<MessageSenderConfig> allConfig = (List<MessageSenderConfig>)em.createQuery("from "+MessageSenderConfig.class.getSimpleName()+" where provider=:provider and disabled=false")
		.setParameter("provider", provider).getResultList();
		if(allConfig!=null && allConfig.size()>0){
			for(MessageSenderConfig config :  allConfig){
				if(result.containsKey(config.getMedia())){
					result.get(config.getMedia()).add(config);
				} else {
					List<MessageSenderConfig> mediaConfigs = new ArrayList<MessageSenderConfig>();
					mediaConfigs.add(config);
					result.put(config.getMedia(), mediaConfigs);
				}
			}
		}
		return result;
	}

}

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
package org.meveo.commons.services.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.meveo.commons.services.PersistenceService;
import org.meveo.model.admin.User;
import org.meveo.model.communication.Campaign;
import org.meveo.model.communication.MediaEnum;
import org.meveo.model.communication.Message;
import org.meveo.model.communication.MessageStatusEnum;
import org.meveo.model.communication.MessageVariableValue;
import org.meveo.model.communication.PriorityEnum;
import org.meveo.model.crm.Provider;

public class MessageService extends PersistenceService<Message> {

	public MessageService(EntityManager em) {
		super(em);
	}

	private VariableValueService variableValueService=null;
	//public HashMap<MediaEnum,HashMap<String,MessageTemplate>> templates= new HashMap<MediaEnum, HashMap<String,MessageTemplate>>();
	
	public void addMessageToAttachedCampaign( Campaign campaign,MediaEnum media,String templateCode,HashMap<String,String> parameters,PriorityEnum priority){
		//TODO: add here verification of template and variables existence
		//if(!templates.containsKey(media)){
		//	templates.put(media, new HashMap<String,MessageTemplate>());
		//}
		//HashMap<String,MessageTemplate> mediaTemplates = templates.get(media);
		User creator=campaign.getAuditable().getCreator();
		Provider provider = campaign.getProvider();
		Message message = new Message();
		message.setCampaign(campaign);
		//TODO: manage contacts and their policy
		//message.setContact(contact);
		message.setMedia(media);
		message.setProvider(campaign.getProvider());
		message.setTemplateCode(templateCode);
		create(message, creator, provider);
		List<MessageVariableValue> variableValues = new ArrayList<MessageVariableValue>();
		if(parameters!=null){
			for(String parameter:parameters.keySet()){
				MessageVariableValue variableValue = new MessageVariableValue();
				variableValue.setCode(parameter);
				variableValue.setMessage(message);
				variableValue.setProvider(campaign.getProvider());
				variableValue.setValue(parameters.get(parameter));
				getVariableValueService().create(variableValue, creator, provider);
			}
		}
		message.setParameters(variableValues);
		message.setStatus(MessageStatusEnum.WAITING);
		message.setPriority(priority);
		em.merge(message);

	}
	
	public void addMessageToAttachedCampaign( Campaign campaign,MediaEnum media,String templateCode,HashMap<String,String> parameters){
		addMessageToAttachedCampaign(campaign, media, templateCode, parameters, PriorityEnum.NORMAL);
	}
	
	private VariableValueService getVariableValueService(){
		if(variableValueService==null){
			variableValueService=new VariableValueService(em);
		}
		return variableValueService;
	}
}

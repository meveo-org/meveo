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

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.grieg.GriegConfig;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.inputloader.AbstractInputLoader;
import org.meveo.core.inputloader.Input;
import org.meveo.core.inputloader.InputNotLoadedException;
import org.meveo.model.communication.Campaign;
import org.meveo.model.communication.CampaignStatusEnum;
import org.meveo.persistence.MeveoPersistence;

import com.google.inject.Inject;

public class MessageInputLoader extends AbstractInputLoader {

    private static final Logger logger = Logger.getLogger(MessageInputLoader.class);

	@Inject
	GriegConfig config;
	
	public synchronized Input loadInput() {
		Input result=null;
		 try {
	            //logger.debug("Load campaigns");
	            EntityManager em = MeveoPersistence.getEntityManager();
	            @SuppressWarnings("unchecked")
	            List<Campaign> campaigns = (List<Campaign>) em.createQuery(
	            		"from "+Campaign.class.getSimpleName()+" where scheduleDate<:date and" +
	            				" status=:status and processingThreadId is null order by priority DESC").setParameter("date", new Date())
	            				.setParameter("status", CampaignStatusEnum.SCHEDULED).getResultList();
	            if(campaigns!=null && campaigns.size()>0){
	            	Campaign campaign=campaigns.get(0);
	            	campaign.setStartDate(new Date());
	            	campaign.setStatus(CampaignStatusEnum.RUNNING);
	            	em.getTransaction().begin();
	            	em.merge(campaign);
	            	em.flush();
	            	em.getTransaction().commit();
		            if(campaign.getMessages()!=null){
			            	String inputName = "Communicator_"+campaign.getId()+"_"+campaign.getMessages().size();	
			            	MessageInputObject inputObject = new MessageInputObject(campaign);
			                result = new Input(inputName, inputObject);
		            } else {
		            	logger.error("No message found in campaign "+campaign.getId());
		            }
	
	            } else {
	            	//logger.debug("No campaign to process");
	            }
		 }  catch (Exception e) {
	            logger.error("Error when retrieving campaigns", e);
	            throw new InputNotLoadedException("Error when retrieving campaigns");
	        }
		 return result;
	}

	/**
	 * @see org.meveo.core.inputloader.InputLoader#handleInputAfterProcessing(org.meveo.core.inputloader.Input, org.meveo.core.inputhandler.TaskExecution)
	 */
	@SuppressWarnings("rawtypes")
    public void handleInputAfterProcessing(Input input,
			TaskExecution taskExecution) {
        EntityManager em = MeveoPersistence.getEntityManager();
        if(input.getInputObject()!=null){
        	Campaign campaign=((MessageInputObject)input.getInputObject()).getCampaign();
        	if(campaign!=null){
		        campaign.setEndDate(new Date());
		        campaign.setStatus(CampaignStatusEnum.TERMINATED);
		        em.merge(campaign);
		    }
        }
	}

	public void handleInputAfterFailure(Input input, Throwable e) {
        EntityManager em = MeveoPersistence.getEntityManager();
        if(input.getInputObject()!=null){
        	Campaign campaign=((MessageInputObject)input.getInputObject()).getCampaign();
        	if(campaign!=null){
        		campaign.setEndDate(new Date());
        		campaign.setStatus(CampaignStatusEnum.CANCELED);
        		em.getTransaction().begin();
                em.merge(campaign);
                em.flush();
                em.getTransaction().commit();
             }
        }
		
	}

}

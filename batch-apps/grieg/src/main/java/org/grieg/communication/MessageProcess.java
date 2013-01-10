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
package org.grieg.communication;

import java.util.HashMap;
import java.util.List;

import org.grieg.GriegConfig;
import org.grieg.communication.email.EmailSender;
import org.grieg.communication.ticket.MessageTicket;
import org.grieg.constants.GriegConstants;
import org.meveo.config.MeveoConfig;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.StepExecution;
import org.meveo.model.communication.MediaEnum;
import org.meveo.model.communication.Message;
import org.meveo.model.communication.MessageSenderConfig;
import org.meveo.model.communication.MessageTemplate;
import org.meveo.model.communication.MessageVariableValue;

public class MessageProcess extends AbstractProcessStep<MessageTicket> {

	EmailSender emailSender = new EmailSender((GriegConfig) config);

	public MessageProcess(AbstractProcessStep<MessageTicket> nextStep,
			MeveoConfig config) {
		super(nextStep, config);
	}

	@Override
	protected boolean execute(StepExecution<MessageTicket> stepExecution) {
		boolean result = false;
		MessageTicket ticket = stepExecution.getTicket();
		TaskExecution<MessageTicket> taskExecution = stepExecution
				.getTaskExecution();
		Message message = ticket.getMessage();
		if (message.getMedia() == null) {
			// TODO:get Media from Campaign
			logger.error("Media is null in message");
			setNotAccepted(stepExecution, "MEDIA_IS_NULL");
			return false;
		}
		@SuppressWarnings("unchecked")
		HashMap<MediaEnum, HashMap<String, MessageTemplate>> mediaTemplates = (HashMap<MediaEnum, HashMap<String, MessageTemplate>>) taskExecution
				.getExecutionContextParameter(GriegConstants.MEDIA_TEMPLATES);
		if (!mediaTemplates.containsKey(message.getMedia())) {
			logger.error("Provider have no template for media "
					+ message.getMedia());
			setNotAccepted(stepExecution, "NO_TEMPLATE_FOUND");
			return false;
		}
		HashMap<String, MessageTemplate> templates = mediaTemplates.get(message
				.getMedia());
		if (message.getTemplateCode() == null) {
			logger.error("Template is null in message");
			setNotAccepted(stepExecution, "TEMPLATE_IS_NULL");
			return false;
		}
		if (!templates.containsKey(message.getTemplateCode())) {
			logger.error("Provider have no template for media "
					+ message.getMedia() + " annd code "
					+ message.getTemplateCode());
			setNotAccepted(stepExecution, "NO_TEMPLATE_FOUND");
			return false;
		}
		MessageTemplate template = templates.get(message.getTemplateCode());

		@SuppressWarnings("unchecked")
		HashMap<MediaEnum, List<MessageSenderConfig>> mediaConfig = (HashMap<MediaEnum, List<MessageSenderConfig>>) taskExecution
				.getExecutionContextParameter(GriegConstants.MEDIA_CONFIG);
		if (!mediaConfig.containsKey(message.getMedia())
				|| mediaConfig.get(message.getMedia()).size() == 0) {
			logger.error("Provider have no config for media "
					+ message.getMedia());
			setNotAccepted(stepExecution, "NO_CONFIG_FOUND");
			return false;
		}

		HashMap<String, MessageVariableValue> parameters = new HashMap<String, MessageVariableValue>();
		if (message.getParameters() != null) {
			for (MessageVariableValue value : message.getParameters()) {
				logger.info("parameters.put(" + value.getCode() + "," + value
						+ ")");
				parameters.put(value.getCode(), value);
			}
		} else {
			logger.warn("message has no parameters");
		}
		List<MessageSenderConfig> configs = mediaConfig.get(message.getMedia());
		if (message.getMedia() == MediaEnum.EMAIL) {
			emailSender.setConfigList(configs);
			try {
				emailSender.sendMessage(template, parameters, message.getPriority());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error while sending email :" + e);
				setNotAccepted(stepExecution, e.getMessage());
				return false;
			}

		} else {
			// TODO:get Media fallback from Campaign
			logger.error("Media is null in message");
			setNotAccepted(stepExecution, "MEDIA_NOT_IMPLEMENTED");
			return false;
		}
		return result;
	}

}

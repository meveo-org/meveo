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

import org.apache.log4j.Logger;
import org.grieg.services.MessageTemplateService;
import org.meveo.model.communication.Message;
import org.meveo.model.communication.MessageSenderConfig;
import org.meveo.model.communication.MessageTemplate;
import org.meveo.model.communication.MessageVariableValue;
import org.meveo.model.communication.PriorityEnum;
import org.meveo.persistence.MeveoPersistence;

public abstract class MessageSender {
    protected static final Logger log = Logger.getLogger(MessageSender.class);

    public abstract void setConfigList(List<MessageSenderConfig> config);

    public abstract void sendMessage(MessageTemplate messageTemplate, HashMap<String, MessageVariableValue> messageParameters, PriorityEnum messagePriority) throws InvalidTemplateException,
            InvalidRecipientException, InvalidProviderException;

    private MessageTemplateService templateService;

    protected String replaceParameters(String message, HashMap<String, MessageVariableValue> parameters, MessageTemplate template) throws InvalidTemplateException,
            InvalidProviderException {
        String result = message;
        if (parameters != null) {
            for (String key : parameters.keySet()) {
                MessageVariableValue value = parameters.get(key);
                Message messageEntity = value.getMessage();
                // if(variable==null){
                // throw new
                // InvalidTemplateException("UNDEFINED_TEMPLATE_VARIABLE");
                // }
                
                if (template == null) {
                	if (messageEntity.getProvider() == null) {
                        throw new InvalidProviderException("UNDEFINED_PROVIDER");
                    }

                    template = getTemplate(messageEntity.getTemplateCode(), messageEntity.getProvider().getCode());
                }
                if (template == null) {
                    throw new InvalidTemplateException("UNDEFINED_TEMPLATE");
                }
                String tagStart = template.getTagStartDelimiter() != null ? template.getTagStartDelimiter() : "";
                String tagEnd = template.getTagEndDelimiter() != null ? template.getTagEndDelimiter() : "";
                log.debug("Replace "+tagStart+value.getCode()+tagEnd+" by "+value.getValue());
                
                if (result.indexOf(tagStart + value.getCode() + tagEnd) > -1) {
                    result = result.replaceAll(tagStart + value.getCode() + tagEnd, value.getValue());
                } else {
                	log.debug("Tag "+tagStart+value.getCode()+tagEnd+" not found in message ");
                    
                }

            }
        }
        log.debug("result :"+result);
        return result;
    }

    private MessageTemplate getTemplate(String code, String providerCode) {
        log.info("getTemplate(code=" + code + ",providerCode=" + providerCode + ")");
        MessageTemplate result = null;
        if (templateService == null) {
            templateService = new MessageTemplateService(MeveoPersistence.getEntityManager());
        }
        result = templateService.findByCode(code, providerCode);
        return result;
    }
}

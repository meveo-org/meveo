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
package org.grieg.communication.email;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.grieg.GriegConfig;
import org.grieg.communication.InvalidProviderException;
import org.grieg.communication.InvalidRecipientException;
import org.grieg.communication.InvalidTemplateException;
import org.grieg.communication.MessageSender;
import org.meveo.model.communication.MessageSenderConfig;
import org.meveo.model.communication.MessageTemplate;
import org.meveo.model.communication.MessageVariableValue;
import org.meveo.model.communication.PriorityEnum;
import org.meveo.model.communication.email.EmailSenderConfig;
import org.meveo.model.communication.email.EmailTemplate;

public class EmailSender extends MessageSender {

	public static String RECIPIENT_ADDRESS = "RECIPIENT_ADDRESS";
	public static String TO_ = "TO_";
	public static String CC_ = "CC_";
	public static String BCC_ = "BCC_";
	public static String ATTACHED_FILE_ = "ATT_FILE_";

	public static int nbEmailSentByProcess = 0;

	GriegConfig config;

	List<MessageSenderConfig> messageSenderConfig;

	public EmailSender(GriegConfig config) {
		super();
		this.config = config;
	}

	@Override
	public void setConfigList(List<MessageSenderConfig> messageSenderConfig) {
		log.info("set config=" + messageSenderConfig);
		this.messageSenderConfig = messageSenderConfig;
	}

	@Override
	public void sendMessage(MessageTemplate messageTemplate,
			HashMap<String, MessageVariableValue> messageParameters,
			PriorityEnum messagePriority) throws InvalidTemplateException,
			InvalidRecipientException, InvalidProviderException {
		log.info("sendMessage(" + messageParameters + ")");
		if (messageParameters == null
				|| (!messageParameters.containsKey(RECIPIENT_ADDRESS) && !messageParameters
						.containsKey(TO_ + "0"))) {
			throw new InvalidRecipientException("RECIPIENT_ADDRESS_NOT_FOUND");
		}

		if (messageSenderConfig == null) {
			throw new InvalidProviderException("CONFIGURATION_NOT_FOUND");
		}
		int roudRobinNb = (nbEmailSentByProcess++) % messageSenderConfig.size();
		log.debug("round robin number:" + roudRobinNb);
		EmailSenderConfig senderConfig = (EmailSenderConfig) messageSenderConfig
				.get(roudRobinNb);
		log.debug("senderConfig=" + senderConfig);

		EmailTemplate template = (EmailTemplate) messageTemplate;

		Properties props = new Properties();
		props.put("mail.from", senderConfig.getDefaultFromEmail());
		if (senderConfig.getLogin() != null
				&& senderConfig.getLogin().trim().length() > 0) {
			if (Boolean.TRUE == senderConfig.isUseSSL()) {
				props.put("mail.smtps.auth", "true");
			} else {
				props.put("mail.smtp.auth", "true");
			}
		}
		props.put("mail.smtp.host", senderConfig.getSMTPHost());
		if (senderConfig.getSMTPPort() != null) {
			props.put("mail.smtp.port", senderConfig.getSMTPPort() + "");
		}
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(true);

		MimeMessage msg = new MimeMessage(session);
		log.info("filling message");
		try {
			msg.addHeader("X-Priority", messagePriority.getMailValue()
					.toString());
		} catch (MessagingException e) {
			e.printStackTrace();
			throw new InvalidProviderException("ERROR_EMAIL_TRANSPORT");
		}
		Transport tr = null;
		try {
			msg.setFrom(new InternetAddress(senderConfig.getDefaultFromEmail()));
			if (messageParameters.containsKey(RECIPIENT_ADDRESS)) {
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
						messageParameters.get(RECIPIENT_ADDRESS).getValue()));
			}
			boolean noAddressFound = false;
			for (int i = 0; !noAddressFound; i++) {
				noAddressFound = true;
				if (messageParameters.containsKey(TO_ + i)) {
					msg.addRecipient(Message.RecipientType.TO,
							new InternetAddress(messageParameters.get(TO_ + i)
									.getValue()));
					noAddressFound = false;
				} else if (messageParameters.containsKey(CC_ + i)) {
					msg.addRecipient(Message.RecipientType.CC,
							new InternetAddress(messageParameters.get(CC_ + i)
									.getValue()));
					noAddressFound = false;
				} else if (messageParameters.containsKey(BCC_ + i)) {
					msg.addRecipient(Message.RecipientType.BCC,
							new InternetAddress(messageParameters.get(BCC_ + i)
									.getValue()));
					noAddressFound = false;
				}
			}

			InternetAddress[] replytoAddress = { new InternetAddress(
					senderConfig.getDefaultReplyEmail()) };
			msg.setReplyTo(replytoAddress);

			String subject = template.getSubject();
			log.debug("subject:" + subject);
			if (subject.indexOf(messageTemplate.getTagStartDelimiter()) > -1) {
				subject = replaceParameters(subject, messageParameters,template);
				log.debug("replaced subject:" + subject);
			}
			msg.setSubject(subject);
			msg.setSentDate(new Date());

			String content = template.getHtmlContent();
			String contentType = "text/html";
			log.debug("html content:" + content);
			if (content == null || content.trim().length() == 0) {
				content = template.getTextContent();
				contentType = "text/plain";
				log.debug("text content:" + content);
			}
			if (content.indexOf(messageTemplate.getTagStartDelimiter()) > -1) {
				content = replaceParameters(content, messageParameters,template);
				log.debug("replaced content:" + content);
			}

			if (messageParameters.containsKey(ATTACHED_FILE_ + 0)) {
				// create the message part
				MimeBodyPart messageBodyPart = new MimeBodyPart();

				// fill message
				messageBodyPart.setText(content);
				messageBodyPart.setContent(content, contentType);

				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);

				for (int i = 0; messageParameters.containsKey(ATTACHED_FILE_
						+ i); i++) {
					// FIXME: get the directory from

					String filename = config.getAttachedFilesDirectory()
							+ File.separator
							+ messageTemplate.getProvider().getCode()
							+ File.separator;

					filename += messageParameters.get(ATTACHED_FILE_ + i)
							.getValue();

					log.debug("attach file :" + filename);
					// add attachment
					MimeBodyPart fileBodyPart = new MimeBodyPart();
					DataSource source = new FileDataSource(filename);
					fileBodyPart.setDataHandler(new DataHandler(source));
					String attachedFilename = null;
					if (filename.contains("/")) {
						int filenameStart = filename.lastIndexOf('/');
						attachedFilename = filename.substring(
								filenameStart + 1, filename.length());
					} else {
						attachedFilename = messageParameters.get(
								ATTACHED_FILE_ + i).getValue();
					}
					fileBodyPart.setFileName(attachedFilename);
					multipart.addBodyPart(fileBodyPart);
					messageParameters.remove(ATTACHED_FILE_ + i);
				}
				// Put parts in message
				msg.setContent(multipart);
			} else {
				msg.setContent(content, contentType);
			}

			if (Boolean.TRUE == senderConfig.isUseSSL()) {
				tr = session.getTransport("smtps");
			} else {
				tr = session.getTransport("smtp");
			}

			if (senderConfig.getLogin() != null
					&& senderConfig.getLogin().trim().length() > 0) {
				tr.connect(senderConfig.getSMTPHost(), senderConfig.getLogin(),
						senderConfig.getPassword());
			} else {
				tr.connect();
			}
			msg.saveChanges(); // don't forget this
			tr.sendMessage(msg, msg.getAllRecipients());
			tr.close();

		} catch (MessagingException e) {
			e.printStackTrace();
			throw new InvalidProviderException("ERROR_EMAIL_TRANSPORT");
		} finally {
			if (tr != null) {
				try {
					tr.close();
				} catch (MessagingException e) {
					e.printStackTrace();
					throw new InvalidProviderException("ERROR_EMAIL_TRANSPORT");
				}
			}
		}
		// sleep
		if (senderConfig.getSenderPolicy() != null
				&& senderConfig.getSenderPolicy().getDelayMinBetween2messages() > 0) {
			try {
				Thread.sleep(senderConfig.getSenderPolicy()
						.getDelayMinBetween2messages());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

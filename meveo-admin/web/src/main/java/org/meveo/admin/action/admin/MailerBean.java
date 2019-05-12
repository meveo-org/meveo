package org.meveo.admin.action.admin;

import org.meveo.admin.action.BaseBean;
import org.meveo.commons.utils.MailerConfiguration;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.base.local.IPersistenceService;
import org.slf4j.Logger;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.Serializable;
import java.util.Properties;

/**
 * Created by Hien Bach
 */
@Named
@ConversationScoped
public class MailerBean extends BaseBean<MailerConfiguration> {

    public MailerConfiguration mailerConfiguration;

    private static final long serialVersionUID = -4570971790276879220L;

    public void sendEmail(String to, String from, String subject, String text) {
        try {
            Properties properties = mailerConfiguration.getInstance().getProperties();
            Session emailSession = Session.getDefaultInstance(properties);

            Message emailMessage = new MimeMessage(emailSession);
            emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            emailMessage.setFrom(new InternetAddress(from));
            emailMessage.setSubject(subject);
            emailMessage.setText(text);

            emailSession.setDebug(true);

            Transport.send(emailMessage);
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected IPersistenceService<MailerConfiguration> getPersistenceService() {
        return null;
    }
}

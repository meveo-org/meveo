package org.meveo.admin.action.admin;

import org.meveo.commons.utils.MailerConfiguration;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;
import javax.mail.*;
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
public class MailerBean implements Serializable {

    MailerConfiguration mailerConfiguration = new MailerConfiguration();

    private static final long serialVersionUID = -4570971790276879220L;

    public void sendEmail(String to, String from, String cc, String subject, String text) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", mailerConfiguration.getHost());
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", mailerConfiguration.getTransportLayerSecurity());
            props.put("mail.smtp.port", mailerConfiguration.getPort());

            Session emailSession = Session.getInstance(props,
                    new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(mailerConfiguration.getUserName(), mailerConfiguration.getPassword());
                        }
                    });

            Message emailMessage = new MimeMessage(emailSession);
            emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            emailMessage.setFrom(new InternetAddress(from));
            emailMessage.setSubject(subject);
            emailMessage.setText(text);

            Transport.send(emailMessage);
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

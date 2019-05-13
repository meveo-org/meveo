package org.meveo.commons.utils;

import javax.ejb.Stateless;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

/**
 * 
 * @author Hien Bach
 * @lastModifiedVersion 5.0
 * 
 */
@Stateless
public class MailerSessionFactory {

    public MailerConfiguration mailerConfiguration = new MailerConfiguration();

    /**
     * Return an session of Session
     * 
     * @return Session session
     */
    public Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", mailerConfiguration.getHost());
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", mailerConfiguration.getTransportLayerSecurity());
        props.put("mail.smtp.port", mailerConfiguration.getPort());

        Session emailSession = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mailerConfiguration.getUserName(), mailerConfiguration.getPassword());
                    }
                });

        return emailSession;
    }
}

package org.meveo.commons.utils;

import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

/**
 * 
 * @author Hien Bach
 * 
 */
@ApplicationScoped
public class MailerSessionFactory {
	
    @Inject
	private MailerConfigurationService mailerConfigurationService;

    /**
     * Return an session of Session
     * 
     * @return Session session
     */
    public Session getSession() {
        return getSession(
        			mailerConfigurationService.getHost(),
        			mailerConfigurationService.getPort(),
        			mailerConfigurationService.getTransportLayerSecurity(),
        			mailerConfigurationService.getUserName(),
        			mailerConfigurationService.getPassword()
        		);
    }
    
    public Session getSession(String host, Integer port, boolean ttls, String username, String password) {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        
        boolean authentication = !StringUtils.isBlank(username)&& !StringUtils.isBlank(password);
        
        if(authentication) {
            props.put("mail.smtp.auth", "true");
        }else {
            props.put("mail.smtp.auth", "false");
        }
        
        props.put("mail.smtp.starttls.enable", ttls);
        props.put("mail.smtp.port", port);

        if(authentication) {
        	return Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(mailerConfigurationService.getUserName(), mailerConfigurationService.getPassword());
                        }
                    });
        }else {
        	return Session.getInstance(props);
        }

    }
}

package org.meveo.admin.action.admin;

import org.meveo.admin.util.ResourceBundle;
import org.meveo.commons.utils.MailerConfiguration;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.model.ParamProperty;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.CellEditEvent;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.Serializable;
import java.util.*;

/**
 * Created by Hien Bach
 */
@Named
@ConversationScoped
public class MailerBean implements Serializable {

    private static final long serialVersionUID = -4570971790276879220L;

    @Inject
    private MailerConfiguration mailerConfiguration;

    @Inject
    protected Conversation conversation;

    @Inject
    private transient ResourceBundle bundle;

    private String host;

    private String port;

    private String userName;

    private String password;

    private boolean transportLayerSecurity;

    @PostConstruct
    private void init() {
        host = mailerConfiguration.getHost();
        port = mailerConfiguration.getPort();
        userName = mailerConfiguration.getUserName();
        password = mailerConfiguration.getPassword();
        if (mailerConfiguration.getTransportLayerSecurity() != null && "true".equals(mailerConfiguration.getTransportLayerSecurity())) {
            transportLayerSecurity = true;
        } else {
            transportLayerSecurity = false;
        }
    }

    private void beginConversation() {
        if (conversation.isTransient()) {
            conversation.begin();
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isTransportLayerSecurity() {
        return transportLayerSecurity;
    }

    public void setTransportLayerSecurity(boolean transportLayerSecurity) {
        this.transportLayerSecurity = transportLayerSecurity;
    }

    public void preRenderView() {
        beginConversation();
    }

    public MailerConfiguration getMailerConfiguration() {
        return mailerConfiguration;
    }

    public void setMailerConfiguration(MailerConfiguration mailerConfiguration) {
        this.mailerConfiguration = mailerConfiguration;
    }

    public void sendEmail() {
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
            emailMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse(mailerConfiguration.getReceivers()));
            emailMessage.setFrom(new InternetAddress(mailerConfiguration.getSender()));
            emailMessage.setSubject(mailerConfiguration.getSubject());
            emailMessage.setText(mailerConfiguration.getBody());

            Transport.send(emailMessage);
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        mailerConfiguration.setHost(host);
        mailerConfiguration.setPort(port);
        mailerConfiguration.setUserName(userName);
        mailerConfiguration.setPassword(password);
        if (transportLayerSecurity) {
            mailerConfiguration.setTransportLayerSecurity("true");
        } else {
            mailerConfiguration.setTransportLayerSecurity("false");
        }
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("properties.save.successful"), bundle.getString("properties.save.successful"));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
}

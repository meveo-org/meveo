package org.meveo.admin.action.admin;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.meveo.admin.util.ResourceBundle;
import org.meveo.commons.utils.MailerConfigurationService;
import org.meveo.commons.utils.MailerSessionFactory;

/**
 * Created by Hien Bach
 */
@Named
@ConversationScoped
public class MailerBean implements Serializable {

    private static final long serialVersionUID = -4570971790276879220L;

    @Inject
    private MailerConfigurationService mailerConfigurationService;
    
    @Inject
    private MailerSessionFactory maillerSessionFactory;

    @Inject
    protected Conversation conversation;

    @Inject
    private transient ResourceBundle bundle;
    
    private String sender;

    private String receivers;

    private String cc;

    private String subject;

    private String body;

    private String host;

    private Integer port;

    private String userName;

    private String password;

    private boolean transportLayerSecurity;

    @PostConstruct
    private void init() {
        host = mailerConfigurationService.getHost();
        port = mailerConfigurationService.getPort();
        userName = mailerConfigurationService.getUserName();
        password = mailerConfigurationService.getPassword();
        transportLayerSecurity = mailerConfigurationService.getTransportLayerSecurity();
    }

    private void beginConversation() {
        if (conversation.isTransient()) {
            conversation.begin();
        }
    }
    
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceivers() {
        return receivers;
    }

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
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

    public void sendEmail() {
        try {
            Session emailSession = maillerSessionFactory.getSession(host, port, transportLayerSecurity, userName, password);

            Message emailMessage = new MimeMessage(emailSession);
            emailMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse(getReceivers()));
            emailMessage.setFrom(new InternetAddress(getSender()));
            emailMessage.setSubject(getSubject());
            emailMessage.setText(getBody());

            Transport.send(emailMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        mailerConfigurationService.setHost(host);
        mailerConfigurationService.setPort(port);
        mailerConfigurationService.setUserName(userName);
        mailerConfigurationService.setPassword(password);
        mailerConfigurationService.setTransportLayerSecurity(transportLayerSecurity);
        
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("properties.save.successful"), bundle.getString("properties.save.successful"));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        mailerConfigurationService.saveConfiguration();
    }
}

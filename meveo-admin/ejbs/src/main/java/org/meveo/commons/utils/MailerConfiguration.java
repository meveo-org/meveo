package org.meveo.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Hien Bach
 */
public class MailerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MailerConfiguration.class);

    /**
     * sender
     */
    private String sender;

    /**
     * receiver
     */
    private String receiver;

    /**
     * cc
     */
    private String cc;

    /**
     * subject
     */
    private String subject;

    /**
     * body
     */
    private String body;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
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
        return ParamBean.getInstance().getProperty("mail.smtp.host", "smtp.gmail.com");
    }

    public void setHost(String host) {
        ParamBean.getInstance().setProperty("mail.smtp.host", host);
    }

    public String getPort() {
        return ParamBean.getInstance().getProperty("mail.smtp.port", "465");
    }

    public void setPort(String port) {
        ParamBean.getInstance().setProperty("mail.smtp.port", port);
    }

    public String getUserName() {
        return ParamBean.getInstance().getProperty("mail.smtp.username", "true");
    }

    public void setUserName(String userName) {
        ParamBean.getInstance().setProperty("mail.smtp.username", userName);
    }

    public String getPassword()  {
         return ParamBean.getInstance().getProperty("mail.smtp.password", "123456");
    }

    public void setPassword(String password) {
        ParamBean.getInstance().setProperty("mail.smtp.password", password);
    }

    public String getTransportLayerSecurity() {
        return ParamBean.getInstance().getProperty("mail.smtp.starttls.enable", "true");
    }

    public void setTransportLayerSecurity(String transportLayerSecurity) {
        ParamBean.getInstance().setProperty("mail.smtp.starttls.enable", transportLayerSecurity);
    }
}

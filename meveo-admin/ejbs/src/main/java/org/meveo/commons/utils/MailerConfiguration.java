package org.meveo.commons.utils;

import java.io.Serializable;

/**
 * Created by Hien Bach
 */
public class MailerConfiguration implements Serializable {

    /**
     * sender
     */
    private String sender;

    /**
     * receiver
     */
    private String receivers;

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
        return ParamBean.getInstance().getProperty("mail.smtp.host", "localhost");
    }

    public void setHost(String host) {
        ParamBean.getInstance().setProperty("mail.smtp.host", host);
    }

    public String getPort() {
        return ParamBean.getInstance().getProperty("mail.smtp.port", "1025");
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

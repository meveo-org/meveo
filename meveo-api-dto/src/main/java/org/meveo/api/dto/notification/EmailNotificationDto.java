package org.meveo.api.dto.notification;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.meveo.model.notification.EmailNotification;
import org.meveo.model.notification.Notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Part of the notification package that handles email.
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "EmailNotification")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("EmailNotificationDto")
public class EmailNotificationDto extends NotificationDto {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -4101784852715599124L;

    /** The email from. */
    @XmlElement(required = true)
    @ApiModelProperty(required = true, value = "The email from")
    private String emailFrom;
    
    /** The email to el. */
    @ApiModelProperty("The email to el")
    private String emailToEl;

    /** The subject. */
    @XmlElement(required = true)
    @ApiModelProperty(required = true, value = "The subject")
    private String subject;

    /** The body. */
    @ApiModelProperty("The body")
    private String body;
    
    /** The html body. */
    @ApiModelProperty("The html body")
    private String htmlBody;

    /** The send to mail. */
    @XmlElement(name = "sendToMail")
    @ApiModelProperty("List of the send to mail")
    private List<String> sendToMail = new ArrayList<String>();

    /**
     * Instantiates a new email notification dto.
     */
    public EmailNotificationDto() {
    	super();
    }

    /**
     * Instantiates a new email notification dto.
     *
     * @param emailNotification the EmailNotification entity
     */
    public EmailNotificationDto(EmailNotification emailNotification) {
        super((Notification) emailNotification);
        emailFrom = emailNotification.getEmailFrom();
        emailToEl = emailNotification.getEmailToEl();
        subject = emailNotification.getSubject();
        body = emailNotification.getSubject();
        htmlBody = emailNotification.getHtmlBody();
    }

    /**
     * Gets the email from.
     *
     * @return the email from
     */
    public String getEmailFrom() {
        return emailFrom;
    }

    /**
     * Sets the email from.
     *
     * @param emailFrom the new email from
     */
    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    /**
     * Gets the email to el.
     *
     * @return the email to el
     */
    public String getEmailToEl() {
        return emailToEl;
    }

    /**
     * Sets the email to el.
     *
     * @param emailToEl the new email to el
     */
    public void setEmailToEl(String emailToEl) {
        this.emailToEl = emailToEl;
    }

    /**
     * Gets the subject.
     *
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the subject.
     *
     * @param subject the new subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Gets the body.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets the body.
     *
     * @param body the new body
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Gets the html body.
     *
     * @return the html body
     */
    public String getHtmlBody() {
        return htmlBody;
    }

    /**
     * Sets the html body.
     *
     * @param htmlBody the new html body
     */
    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }

    /**
     * Gets the send to mail.
     *
     * @return the send to mail
     */
    public List<String> getSendToMail() {
        return sendToMail;
    }

    /**
     * Sets the send to mail.
     *
     * @param sendToMail the new send to mail
     */
    public void setSendToMail(List<String> sendToMail) {
        this.sendToMail = sendToMail;
    }


    @Override
    public String toString() {
        return "EmailNotificationDto [emailFrom=" + emailFrom + ", emailToEl=" + emailToEl + ", , emails=" + sendToMail + " subject=" + subject + ", body=" + body + ", htmlBody="
                + htmlBody + "]";
    }

}
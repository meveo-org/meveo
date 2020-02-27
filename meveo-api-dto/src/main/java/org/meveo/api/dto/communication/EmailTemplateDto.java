package org.meveo.api.dto.communication;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.model.communication.email.EmailTemplate;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The Class EmailTemplateDto.
 *
 * @author Tyshan Shi(tyshan@manaty.net)
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since Jun 3, 2016 4:49:13 AM
 */
@XmlRootElement(name = "EmailTemplate")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel("EmailTemplateDto")
public class EmailTemplateDto extends MessageTemplateDto {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1739876218558380262L;
    
    /** The subject. */
    @XmlElement(required = true)
    @ApiModelProperty(required = true, value = "Subject of the email")
    private String subject;
    
    /** The html content. */
    @ApiModelProperty("Html content of the email")
    private String htmlContent;
    
    /** The text content. */
    @ApiModelProperty("Text content of the email")
    private String textContent;

    /**
     * Instantiates a new email template dto.
     */
    public EmailTemplateDto() {
        super();
    }

    /**
     * Instantiates a new email template dto.
     *
     * @param emailTemplate the email template
     */
    public EmailTemplateDto(EmailTemplate emailTemplate) {
        super(emailTemplate);
        this.subject = emailTemplate.getSubject();
        this.htmlContent = emailTemplate.getHtmlContent();
        this.textContent = emailTemplate.getTextContent();
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
     * Gets the html content.
     *
     * @return the html content
     */
    public String getHtmlContent() {
        return htmlContent;
    }

    /**
     * Sets the html content.
     *
     * @param htmlContent the new html content
     */
    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    /**
     * Gets the text content.
     *
     * @return the text content
     */
    public String getTextContent() {
        return textContent;
    }

    /**
     * Sets the text content.
     *
     * @param textContent the new text content
     */
    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }
}
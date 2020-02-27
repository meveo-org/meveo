package org.meveo.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Hien Bach
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "MailerConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class MailerConfigurationDto {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The host */
    @XmlAttribute(required = true)
    @ApiModelProperty(required = true, value = "The host")
    private String host;

    /** The port */
    @XmlAttribute(required = true)
    @ApiModelProperty(required = true, value = "The port")
    private Integer port;

    /** The userName */
    @XmlAttribute(required = true)
    @ApiModelProperty(required = true, value = "The user name")
    private String userName;

    /** The password */
    @XmlAttribute(required = true)
    @ApiModelProperty(required = true, value = "The password")
    private String password;

    /** The tls */
    @XmlAttribute(required = true)
    @ApiModelProperty(required = true, value = "The tls")
    private boolean tls;

    public MailerConfigurationDto(){

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

    public boolean getTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }
}

package org.meveo.api.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Hien Bach
 */
@XmlRootElement(name = "MailerConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class MailerConfigurationDto {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The host */
    @XmlAttribute(required = true)
    private String host;

    /** The port */
    @XmlAttribute(required = true)
    private Integer port;

    /** The userName */
    @XmlAttribute(required = true)
    private String userName;

    /** The password */
    @XmlAttribute(required = true)
    private String password;

    /** The tls */
    @XmlAttribute(required = true)
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

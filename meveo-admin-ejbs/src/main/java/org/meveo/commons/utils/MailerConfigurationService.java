package org.meveo.commons.utils;

import java.io.Serializable;

/**
 * Created by Hien Bach
 */
public class MailerConfigurationService implements Serializable {

	private static final long serialVersionUID = 9026636252731956179L;

	public String getHost() {
        return ParamBean.getInstance().getProperty("mail.smtp.host", null);
    }

    public void setHost(String host) {
        ParamBean.getInstance().setProperty("mail.smtp.host", host);
    }

    public Integer getPort() {
    	String port = ParamBean.getInstance().getProperty("mail.smtp.port", null);
        return !StringUtils.isBlank(port) ? Integer.parseInt(port) : null;
    }

    public void setPort(Integer port) {
        ParamBean.getInstance().setProperty("mail.smtp.port", String.valueOf(port));
    }

    public String getUserName() {
        return ParamBean.getInstance().getProperty("mail.smtp.username", null);
    }

    public void setUserName(String userName) {
        ParamBean.getInstance().setProperty("mail.smtp.username", userName);
    }

    public String getPassword()  {
         return ParamBean.getInstance().getProperty("mail.smtp.password", null);
    }

    public void setPassword(String password) {
        ParamBean.getInstance().setProperty("mail.smtp.password", password);
    }

    public boolean getTransportLayerSecurity() {
        return Boolean.parseBoolean(ParamBean.getInstance().getProperty("mail.smtp.starttls.enable", "true"));
    }

    public void setTransportLayerSecurity(boolean transportLayerSecurity) {
        ParamBean.getInstance().setProperty("mail.smtp.starttls.enable", String.valueOf(transportLayerSecurity));
    }
    
    public void saveConfiguration() {
    	ParamBean.getInstance().saveProperties();
    }
}

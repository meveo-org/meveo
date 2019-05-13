package org.meveo.api.rest.impl;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.MailerConfigurationDto;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.MailerConfigurationRs;
import org.meveo.commons.utils.MailerConfiguration;

import javax.enterprise.context.RequestScoped;
import javax.interceptor.Interceptors;

/**
 * @author Hien Bach
 **/
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class MailerConfigurationRsImpl extends BaseRs implements MailerConfigurationRs {

    @Override
    public ActionStatus createOrUpdate(MailerConfigurationDto mailerConfigurationDto) {

        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {

            MailerConfiguration mailerConfiguration = new MailerConfiguration();
            mailerConfiguration.setHost(mailerConfigurationDto.getHost());
            mailerConfiguration.setPort(mailerConfigurationDto.getPort());
            mailerConfiguration.setUserName(mailerConfigurationDto.getUserName());
            mailerConfiguration.setPassword(mailerConfigurationDto.getPassword());
            mailerConfiguration.setTransportLayerSecurity(mailerConfigurationDto.getTls());
        } catch (Exception e) {

            processException(e, result);
        }

        return result;
    }

}

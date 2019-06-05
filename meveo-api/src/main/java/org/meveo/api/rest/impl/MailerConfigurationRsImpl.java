package org.meveo.api.rest.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.MailerConfigurationDto;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.MailerConfigurationRs;
import org.meveo.commons.utils.MailerConfigurationService;

/**
 * @author Hien Bach
 **/
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class MailerConfigurationRsImpl extends BaseRs implements MailerConfigurationRs {

	@Inject
	private MailerConfigurationService mailerConfigurationService;
	
    @Override
    public ActionStatus createOrUpdate(MailerConfigurationDto mailerConfigurationDto) {

        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            mailerConfigurationService.setHost(mailerConfigurationDto.getHost());
            mailerConfigurationService.setPort(mailerConfigurationDto.getPort());
            mailerConfigurationService.setUserName(mailerConfigurationDto.getUserName());
            mailerConfigurationService.setPassword(mailerConfigurationDto.getPassword());
            mailerConfigurationService.setTransportLayerSecurity(mailerConfigurationDto.getTls());
            
            mailerConfigurationService.saveConfiguration();
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

}

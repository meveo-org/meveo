package org.meveo.api.rest.config.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.config.MavenConfigurationDto;
import org.meveo.api.dto.config.MavenConfigurationResponseDto;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.service.config.impl.MavenConfigurationService;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.5.0
 */
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Path("/mavenConfiguration")
public class MavenConfigurationRs extends BaseRs {

	@Inject
	private MavenConfigurationService mavenConfigurationService;

	/**
	 * Create or update the maven configuration.
	 * 
	 * @param postData maven configuration values
	 * @return status of the request
	 */
	@POST
	@Path("/")
	public ActionStatus createOrUpdate(MavenConfigurationDto postData) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			mavenConfigurationService.setMavenRepositories(postData.getMavenRepositories());
			mavenConfigurationService.saveConfiguration();

		} catch (Exception e) {
			processException(e, result);
		}

		return result;
	}

	@GET
	@Path("/")
	public MavenConfigurationResponseDto getConfiguration() {

		MavenConfigurationResponseDto result = new MavenConfigurationResponseDto();
		result.getMavenConfiguration().setMavenRepositories(mavenConfigurationService.getMavenRepositories());
		
		return result;
	}
}

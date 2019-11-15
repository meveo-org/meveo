package org.meveo.admin.action.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.validator.routines.UrlValidator;
import org.meveo.admin.util.ResourceBundle;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.dto.config.MavenConfigurationDto;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.service.config.impl.MavenConfigurationService;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.5.0
 */
@Named
@ConversationScoped
public class MavenConfigurationBean implements Serializable {

	private static final long serialVersionUID = -6715787921470318028L;

	@Inject
	private transient ResourceBundle bundle;

	@Inject
	private Conversation conversation;

	@Inject
	private MavenConfigurationService mavenConfigurationService;

	private MavenConfigurationDto mavenConfiguration;

	@PostConstruct
	private void init() {
		mavenConfiguration = mavenConfigurationService.loadConfig();
	}

	public void preRenderView() {
		beginConversation();
	}

	public void beginConversation() {
		
		if (conversation.isTransient()) {
			conversation.begin();
		}
	}

	public MavenConfigurationDto getMavenConfiguration() {

		if (mavenConfiguration == null) {
			mavenConfiguration = new MavenConfigurationDto();
		}

		return mavenConfiguration;
	}

	public void setMavenConfiguration(MavenConfigurationDto mavenConfiguration) {
		this.mavenConfiguration = mavenConfiguration;
	}

	public String getMavenRepositories() {
		return String.join("\r\n", mavenConfiguration.getMavenRepositories());
	}

	public void setMavenRepositories(String mavenRepositories) {
		
		if (!StringUtils.isBlank(mavenRepositories)) {
			mavenConfiguration.setMavenRepositories(Arrays.asList(mavenRepositories.split("\r\n")));
		}
	}

	@ActionMethod
	public void save() throws BusinessApiException {

		UrlValidator urlValidator = new UrlValidator();
		List<String> urls = new ArrayList<>(mavenConfiguration.getMavenRepositories());
		urls.removeIf(StringUtils::isBlank);
		for (String url : urls) {
			if (!urlValidator.isValid(url)) {
				throw new BusinessApiException("Invalid URL format : " + url);
			}
		}
		
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("properties.save.successful"), bundle.getString("properties.save.successful"));
		FacesContext.getCurrentInstance().addMessage(null, msg);

		mavenConfigurationService.saveConfiguration(mavenConfiguration);
	}
}

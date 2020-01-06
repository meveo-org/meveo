package org.meveo.admin.action.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Any;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.ResourceBundle;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.dto.config.MavenConfigurationDto;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.storage.RemoteRepository;
import org.meveo.service.config.impl.MavenConfigurationService;
import org.meveo.service.storage.RemoteRepositoryService;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.6.0
 * @since 6.5.0
 */
@Named
@ConversationScoped
public class MavenConfigurationBean implements Serializable {

	private static final long serialVersionUID = -6715787921470318028L;

	/** Logger. */
	protected Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	private transient ResourceBundle bundle;

	@Inject
	private Conversation conversation;

	@Inject
	private MavenConfigurationService mavenConfigurationService;

	@Inject
	private RemoteRepositoryService remoteRepositoryService;

	private MavenConfigurationDto mavenConfiguration;

	private List<RemoteRepository> remoteRepositories;
	private RemoteRepository remoteRepository;

	private String groupId;
	private String artifactId;
	private String version;
	private String classifier;

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

		if (CollectionUtils.isNotEmpty(remoteRepositories)) {
			List<RemoteRepository> repos = new ArrayList<>();
			for (RemoteRepository remote : remoteRepositories) {
				if (remote != null) {
					repos.add(remote);
				}
				getRemoteRepositories().clear();
				getRemoteRepositories().addAll(repos);
			}
		}

		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("properties.save.successful"), bundle.getString("properties.save.successful"));
		FacesContext.getCurrentInstance().addMessage(null, msg);

		mavenConfigurationService.saveConfiguration(mavenConfiguration);
	}

	public void uploadAnArtifact(FileUploadEvent event) {
		UploadedFile file = event.getFile();
		String fileName = file.getFileName();
		try {
			// write the inputStream to a FileOutputStream
			InputStream inputStream = file.getInputstream();
			String filePath = mavenConfigurationService.createDirectory(this.groupId, this.artifactId, this.version, this.classifier);
			filePath = filePath + File.separator + mavenConfigurationService.buildArtifactName(this.artifactId, this.version, this.classifier);
			try (OutputStream out = new FileOutputStream(new File(filePath))) {
				int read = 0;
				byte[] bytes = new byte[1024];

				while ((read = inputStream.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
			}

			log.debug("New file created!");
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("maven.configuration.upload.successful", fileName),
					bundle.getString("maven.configuration.upload.successful", fileName));
			FacesContext.getCurrentInstance().addMessage(null, msg);
			groupId = null;
			artifactId = null;
			version = null;
			classifier = null;

		} catch (IOException e) {
			log.error("Failed saving file. ", e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("message.upload.fail", e.getMessage()),
					bundle.getString("message.upload.fail", e.getMessage()));
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public List<RemoteRepository> getRemoteRepositories() {
		if (remoteRepositories == null) {
			if (remoteRepositoryService.list() != null) {
				remoteRepositories = new ArrayList<>(remoteRepositoryService.list());
				return remoteRepositories;
			} else {
				return new ArrayList<>();
			}
		}
		return remoteRepositories;
	}

	public void setRemoteRepositories(List<RemoteRepository> remoteRepositories) {
		this.remoteRepositories = remoteRepositories;
	}

	public void saveRemoteRepository() throws BusinessException {
		if (CollectionUtils.isEmpty(remoteRepositories)) {
			remoteRepositoryService.create(remoteRepository);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("save.successful"), bundle.getString("save.successful"));
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} else {
			remoteRepositoryService.update(remoteRepository);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("save.successful"), bundle.getString("save.successful"));
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public void removeMavenRepository(RemoteRepository selectRemoteRepository) throws BusinessException {
		remoteRepositories.remove(selectRemoteRepository);
	}

	public void addMavenRepository() {
		remoteRepository = new RemoteRepository();
		remoteRepositories.add(remoteRepository);
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public RemoteRepository getRemoteRepository() {
		return remoteRepository;
	}

	public void setRemoteRepository(RemoteRepository remoteRepository) {
		this.remoteRepository = remoteRepository;
	}
}

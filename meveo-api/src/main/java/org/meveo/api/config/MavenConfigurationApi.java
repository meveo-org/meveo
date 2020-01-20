package org.meveo.api.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseApi;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.storage.RemoteRepositoryDto;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.storage.RemoteRepository;
import org.meveo.service.config.impl.MavenConfigurationService;
import org.meveo.service.storage.RemoteRepositoryService;

/**
 * @author Hien
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.6.0
 * @since 6.5.0
 */
public class
MavenConfigurationApi extends BaseApi {

	@Inject
	private MavenConfigurationService mavenConfigurationService;

	@Inject
	private RemoteRepositoryService remoteRepositoryService;

	/**
	 * Upload an artifact in the maven configuration
	 *
	 * @param inputStream JAR file
	 * @param groupId     GroupId
	 * @param artifactId  ArtifactId
	 * @param version     Version
	 * @param classifier  Classifier
	 * @param filename    Name of the jar file
	 */
	public void uploadAnArtifact(InputStream inputStream, String groupId, String artifactId, String version, String classifier, String filename) throws BusinessApiException {
		String filePath = mavenConfigurationService.createDirectory(groupId, artifactId, version, classifier);
		filePath = filePath + File.separator + mavenConfigurationService.buildArtifactName(artifactId, version, classifier);

		try {
			OutputStream outputStream = new FileOutputStream(new File(filePath));
			int read = 0;
			byte[] data = new byte[1024];

			while ((read = inputStream.read(data)) != -1) {
				outputStream.write(data, 0, read);
			}

			inputStream.close();
			outputStream.flush();
			outputStream.close();

		} catch (Exception e) {
			throw new BusinessApiException("Error uploading file: " + filename + ". " + e.getMessage());
		}
	}

	public RemoteRepository create(RemoteRepositoryDto postData) throws BusinessException, MeveoApiException {
		validate(postData);

		if (remoteRepositoryService.findByCode(postData.getCode()) != null) {
			throw new EntityAlreadyExistsException(RemoteRepository.class, postData.getCode());
		}

		RemoteRepository entity = fromDTO(postData, null);
		remoteRepositoryService.create(entity);

		return entity;
	}

	public RemoteRepository update(RemoteRepositoryDto postData) throws BusinessException, MeveoApiException {
		validate(postData);

		RemoteRepository entity = remoteRepositoryService.findByCode(postData.getCode());
		if (entity == null) {
			throw new org.meveo.exceptions.EntityDoesNotExistsException(RemoteRepository.class, postData.getCode());
		}

		entity = fromDTO(postData, entity);
		return remoteRepositoryService.update(entity);
	}

	public RemoteRepository createOrUpdate(RemoteRepositoryDto postData) throws BusinessException, MeveoApiException {
		RemoteRepository entity = remoteRepositoryService.findByCode(postData.getCode());
		if (entity == null) {
			return create(postData);

		} else {
			return update(postData);
		}
	}

	public List<RemoteRepositoryDto> list() {
		List<RemoteRepositoryDto> result = new ArrayList<>();
		List<RemoteRepository> remoteRepositories = remoteRepositoryService.list();
		if (remoteRepositories != null) {
			for (RemoteRepository remoteRepository : remoteRepositories) {
				result.add(new RemoteRepositoryDto(remoteRepository));
			}
		}
		return result;
	}

	public void remove(String remoteRepositoryCode) throws MeveoApiException, BusinessException {
		if (!StringUtils.isBlank(remoteRepositoryCode)) {
			RemoteRepository remote = remoteRepositoryService.findByCode(remoteRepositoryCode);

			if (remote == null) {
				throw new EntityDoesNotExistsException(RemoteRepository.class, remoteRepositoryCode);
			}

			remoteRepositoryService.remove(remote);
		} else {
			missingParameters.add("code");

			handleMissingParameters();
		}
	}

	protected RemoteRepository fromDTO(RemoteRepositoryDto dto, RemoteRepository repositoryToUpdate) {
		RemoteRepository remoteRepository = new RemoteRepository();
		if (repositoryToUpdate != null) {
			remoteRepository = repositoryToUpdate;
		} else {
			remoteRepository.setCode(dto.getCode());
			remoteRepository.setUrl(dto.getUrl());
		}

		return remoteRepository;
	}


}
/**
 * 
 */
package org.meveo.service.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.BinaryProvider;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.storage.BinaryStorageConfiguration;
import org.meveo.model.storage.IStorageConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.PersistenceActionResult;
import org.meveo.persistence.StorageImpl;
import org.meveo.persistence.StorageQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemImpl implements StorageImpl {
	
	@Inject
	private FileSystemService fileSystemService;
	
	private static Logger log = LoggerFactory.getLogger(FileSystemImpl.class);
	
	@Override
	public boolean exists(IStorageConfiguration repository, CustomEntityTemplate cet, String uuid) {
		return false;
	}

	@Override
	public String findEntityIdByValues(Repository repository, IStorageConfiguration conf, CustomEntityInstance cei) {
		return null;
	}

	@Override
	public Map<String, Object> findById(IStorageConfiguration repository, CustomEntityTemplate cet, String uuid, Map<String, CustomFieldTemplate> cfts, Collection<String> fetchFields, boolean withEntityReferences) {
		Map<String, Object> result = new HashMap<>();
		
		cfts.values()
			.stream()
			.filter(cft -> cft.getFieldType() == CustomFieldTypeEnum.BINARY)
			.filter(cft -> fetchFields.contains(cft.getCode()))
			.forEach(cft -> {
				try {
					List<BinaryProvider> binaries = fileSystemService.findBinariesStaticPath((BinaryStorageConfiguration) repository, cet.getCode(), uuid, cft)
							.stream()
							.filter(File::exists)
							.map(BinaryProvider::new)
							.collect(Collectors.toList());
					
					result.put(cft.getCode(), binaries);
				} catch (BusinessApiException e) {
					log.error("Failed to fetch binaries", e);
				}
			});
		
		return result;
	}

	@Override
	public List<Map<String, Object>> find(StorageQuery query) throws EntityDoesNotExistsException {
		// Never eagerly load files
		return new ArrayList<>();
	}

	@Override
	public PersistenceActionResult createOrUpdate(Repository repository, IStorageConfiguration storageConf, CustomEntityInstance cei, Map<String, CustomFieldTemplate> customFieldTemplates, String foundUuid) throws BusinessException {
		final String uuid = StringUtils.isBlank(foundUuid) ? cei.getUuid() : foundUuid;
		
		String rootPath = repository != null && repository.getBinaryStorageConfiguration() != null ? repository.getBinaryStorageConfiguration().getRootPath() : "";

		customFieldTemplates.values()
		.stream()
		.filter(cft -> cft.getFieldType() == CustomFieldTypeEnum.BINARY)
		.forEach(cft -> {
			var cfValue = cei.getCfValues().getCfValue(cft.getCode());
			Set<String> fileNames = cfValue.getFileNames();
			
			cfValue.getBinaries().forEach(binary -> {
				BinaryStoragePathParam params = new BinaryStoragePathParam();
				params.setShowOnExplorer(cft.isSaveOnExplorer());
				params.setRootPath(rootPath);
				params.setCetCode(cei.getCetCode());
				params.setUuid(uuid);
				params.setCftCode(cft.getCode());
				params.setFilePath(cft.getFilePath());
				// params.setContentType(file.getContentType());
				params.setFilename(binary.getFileName());
				params.setInputStream(binary.getBinary());
				// params.setFileSizeInBytes(file.getSize());
				params.setFileExtensions(cft.getFileExtensions());
				params.setContentTypes(cft.getContentTypes());
				params.setMaxFileSizeAllowedInKb(cft.getMaxFileSizeAllowedInKb());
				
				try {
					fileSystemService.persists(params);
				} catch (IOException e) {
					log.error("Failed to persist binary", e);
				}
			});
			
			// Remove objects not present on the updated list
			try {
				fileSystemService.findBinariesStaticPath(repository.getBinaryStorageConfiguration(), cei.getCetCode(), uuid, cft)
						.stream()
						.filter(file -> !fileNames.contains(file.getName()))
						.forEach(file -> {
							log.debug("Delete file {} from file system", file);
							file.delete();
						});
			} catch (BusinessApiException e) {
				log.error("Failed to read file system binaries", e);
			}
		});
		
		return new PersistenceActionResult(uuid);
	}

	@Override
	public PersistenceActionResult addCRTByUuids(IStorageConfiguration repository, CustomRelationshipTemplate crt, Map<String, Object> relationValues, String sourceUuid, String targetUuid) throws BusinessException {
		return null;
	}

	@Override
	public void update(Repository repository, IStorageConfiguration conf, CustomEntityInstance cei) throws BusinessException {
		createOrUpdate(repository, conf, cei, cei.getFieldTemplates(), cei.getUuid());
	}

	@Override
	public void setBinaries(IStorageConfiguration repository, CustomEntityTemplate cet, CustomFieldTemplate cft, String uuid, List<File> binaries) throws BusinessException {
	}

	@Override
	public void remove(IStorageConfiguration repository, CustomEntityTemplate cet, String uuid) throws BusinessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Integer count(IStorageConfiguration repository, CustomEntityTemplate cet, PaginationConfiguration paginationConfiguration) {
		return null;
	}

	@Override
	public void cetCreated(CustomEntityTemplate cet) {
	}

	@Override
	public void crtCreated(CustomRelationshipTemplate crt) throws BusinessException {
	}

	@Override
	public void cftCreated(CustomModelObject template, CustomFieldTemplate cft) {
	}

	@Override
	public void cetUpdated(CustomEntityTemplate oldCet, CustomEntityTemplate cet) {
	}

	@Override
	public void crtUpdated(CustomRelationshipTemplate cet) throws BusinessException {
	}

	@Override
	public void cftUpdated(CustomModelObject template, CustomFieldTemplate oldCft, CustomFieldTemplate cft) {
	}

	@Override
	public void removeCft(CustomModelObject template, CustomFieldTemplate cft) {
	}

	@Override
	public void removeCet(CustomEntityTemplate cet) {
	}

	@Override
	public void removeCrt(CustomRelationshipTemplate crt) {
	}

	@Override
	public void init() {
	}

	@Override
	public <T> T beginTransaction(IStorageConfiguration repository, int stackedCalls) {
		return null;
	}

	@Override
	public void commitTransaction(IStorageConfiguration repository) {
	}

	@Override
	public void rollbackTransaction(int stackedCalls) {
	}

	@Override
	public void destroy() {
	}
}

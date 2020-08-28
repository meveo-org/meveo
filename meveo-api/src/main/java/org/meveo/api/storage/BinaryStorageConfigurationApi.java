package org.meveo.api.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.storage.BinaryStorageConfiguration;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.storage.BinaryStorageConfigurationService;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
@Stateless
public class BinaryStorageConfigurationApi extends BaseCrudApi<BinaryStorageConfiguration, BinaryStorageConfigurationDto> {

	public BinaryStorageConfigurationApi() {
		super(BinaryStorageConfiguration.class, BinaryStorageConfigurationDto.class);
	}

	@Inject
	private BinaryStorageConfigurationService binaryStorageConfigurationService;

	public BinaryStorageConfiguration create(BinaryStorageConfigurationDto postData) throws BusinessException, MeveoApiException {
		validate(postData);

		if (binaryStorageConfigurationService.findByCode(postData.getCode()) != null) {
			throw new EntityAlreadyExistsException(BinaryStorageConfiguration.class, postData.getCode());
		}

		BinaryStorageConfiguration entity = fromDto(postData);

		binaryStorageConfigurationService.create(entity);
		
		return entity;
	}

	public void update(BinaryStorageConfigurationDto postData) throws BusinessException, MeveoApiException {
		validate(postData);

		BinaryStorageConfiguration entity = binaryStorageConfigurationService.findByCode(postData.getCode());
		if (entity == null) {
			throw new EntityDoesNotExistsException(BinaryStorageConfiguration.class, postData.getCode());
		}

		entity.setRootPath(postData.getRootPath());

		binaryStorageConfigurationService.update(entity);
	}

	public BinaryStorageConfiguration createOrUpdate(BinaryStorageConfigurationDto postData) throws BusinessException, MeveoApiException {
		BinaryStorageConfiguration entity = binaryStorageConfigurationService.findByCode(postData.getCode());
		if (entity == null) {
			return create(postData);

		} else {
			update(postData);
			return entity;
		}

	}

	public BinaryStorageConfigurationDto find(String code) throws EntityDoesNotExistsException {
		BinaryStorageConfiguration entity = binaryStorageConfigurationService.findByCode(code);

		if (entity == null) {
			throw new EntityDoesNotExistsException(BinaryStorageConfiguration.class, code);
		}

		BinaryStorageConfigurationDto dto = new BinaryStorageConfigurationDto(entity);

		return dto;
	}

	public List<BinaryStorageConfigurationDto> findAll() {
		List<BinaryStorageConfiguration> binaryStorageConfigurations = binaryStorageConfigurationService.list();

		return binaryStorageConfigurations != null ? binaryStorageConfigurations.stream().map(e -> new BinaryStorageConfigurationDto(e)).collect(Collectors.toList())
				: new ArrayList<BinaryStorageConfigurationDto>();
	}

	public void remove(String code) throws BusinessException {
		BinaryStorageConfiguration entity = binaryStorageConfigurationService.findByCode(code);
		if (entity == null) {
			throw new EntityDoesNotExistsException(BinaryStorageConfiguration.class, code);
		}

		binaryStorageConfigurationService.remove(entity);
	}

	@Override
	public BinaryStorageConfigurationDto toDto(BinaryStorageConfiguration entity) {
		return new BinaryStorageConfigurationDto(entity);
	}

	@Override
	public BinaryStorageConfiguration fromDto(BinaryStorageConfigurationDto dto) throws MeveoApiException {
		BinaryStorageConfiguration entity = new BinaryStorageConfiguration();
		entity.setCode(dto.getCode());
		entity.setRootPath(dto.getRootPath());
		return entity;
	}

	@Override
	public IPersistenceService<BinaryStorageConfiguration> getPersistenceService() {
		return binaryStorageConfigurationService;
	}

	@Override
	public boolean exists(BinaryStorageConfigurationDto dto) {
		try {
			return find(dto.getCode()) != null;
		} catch (EntityDoesNotExistsException e) {
			return false;
		}
	}

	@Override
	public void remove(BinaryStorageConfigurationDto dto) throws MeveoApiException, BusinessException {
		this.remove(dto.getCode());
	}

}

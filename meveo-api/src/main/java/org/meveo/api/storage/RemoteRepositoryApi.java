package org.meveo.api.storage;

import javax.ejb.Stateless;
import javax.inject.Inject;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.storage.RemoteRepository;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.storage.RemoteRepositoryService;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.15
 */
@Stateless
public class RemoteRepositoryApi extends BaseCrudApi<RemoteRepository, RemoteRepositoryDto> {

  @Inject
  private RemoteRepositoryService remoteRepositoryService;

  public RemoteRepositoryApi() {
    super(RemoteRepository.class, RemoteRepositoryDto.class);
  }

  @Override
  public RemoteRepositoryDto find(String code) throws MissingParameterException, EntityDoesNotExistsException {

    if (StringUtils.isBlank(code)) {
      missingParameters.add("code");
      handleMissingParameters();
    }

    RemoteRepositoryDto result = new RemoteRepositoryDto();
    RemoteRepository timerEntity = remoteRepositoryService.findByCode(code);
    if (timerEntity == null) {
      throw new EntityDoesNotExistsException(RemoteRepository.class, code);
    }
    result = new RemoteRepositoryDto(timerEntity);

    return result;
  }

  @Override
  public RemoteRepository createOrUpdate(RemoteRepositoryDto dto) throws MeveoApiException, BusinessException {

    RemoteRepository cec = remoteRepositoryService.findByCode(dto.getCode());
    if (cec == null) {
      return create(dto);
    } else {
      return update(dto);
    }
  }

  private RemoteRepository update(RemoteRepositoryDto dto)
      throws EntityAlreadyExistsException, MissingParameterException, BusinessException, EntityDoesNotExistsException {

    if (org.apache.commons.lang3.StringUtils.isBlank(dto.getCode())) {
      missingParameters.add("code");
    }
    if (org.apache.commons.lang3.StringUtils.isBlank(dto.getUrl())) {
      missingParameters.add("url");
    }
    handleMissingParameters();

    RemoteRepository entity = remoteRepositoryService.findByCode(dto.getCode());
    if (entity == null) {
      throw new EntityDoesNotExistsException(RemoteRepository.class, dto.getCode());
    }

    entity = fromDto(dto, entity);
    remoteRepositoryService.update(entity);

    return entity;
  }

  private RemoteRepository create(RemoteRepositoryDto dto)
      throws MissingParameterException, EntityAlreadyExistsException, BusinessException {

    if (org.apache.commons.lang3.StringUtils.isBlank(dto.getCode())) {
      missingParameters.add("code");
    }
    if (org.apache.commons.lang3.StringUtils.isBlank(dto.getUrl())) {
      missingParameters.add("url");
    }
    handleMissingParameters();

    if (remoteRepositoryService.findByCode(dto.getCode()) != null) {
      throw new EntityAlreadyExistsException(RemoteRepository.class, dto.getCode());
    }

    RemoteRepository entity = fromDto(dto);
    remoteRepositoryService.create(entity);

    return entity;
  }

  public RemoteRepository fromDto(RemoteRepositoryDto source) {
    return fromDto(source, null);
  }

  public RemoteRepository fromDto(RemoteRepositoryDto source, RemoteRepository target) {

    if(target == null) {
      target = new RemoteRepository();
      target.setCode(source.getCode());
    }

    target.setDescription(source.getDescription());
    target.setUrl(source.getUrl());

    return target;
  }

  @Override
  public RemoteRepositoryDto toDto(RemoteRepository entity) throws MeveoApiException {

    RemoteRepositoryDto dto = new RemoteRepositoryDto();
    dto.setCode(entity.getCode());
    dto.setDescription(entity.getDescription());
    dto.setUrl(entity.getUrl());

    return dto;
  }

  @Override
  public IPersistenceService<RemoteRepository> getPersistenceService() {
    return remoteRepositoryService;
  }
}

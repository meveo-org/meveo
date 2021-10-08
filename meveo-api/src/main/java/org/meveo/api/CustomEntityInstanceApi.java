package org.meveo.api;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.dto.CustomEntityInstanceDto;
import org.meveo.api.dto.CustomFieldDto;
import org.meveo.api.dto.module.MeveoModuleItemDto;
import org.meveo.api.exception.ActionForbiddenException;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.elresolver.ELException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.CEIUtils;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.typereferences.GenericTypeReferences;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.custom.CustomEntityTemplateService;

/**
 * @author Andrius Karpavicius
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.13
 **/
@Stateless
public class CustomEntityInstanceApi extends BaseCrudApi<CustomEntityInstance, CustomEntityInstanceDto> {

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

	@Inject
	private CustomEntityInstanceService customEntityInstanceService;

	@Inject
	private CustomFieldTemplateService customFieldTemplateService;

	public CustomEntityInstanceApi() {
		super(CustomEntityInstance.class, CustomEntityInstanceDto.class);
	}
	
	public List<CustomEntityInstanceDto> readCeis(File ceiDirectory) {
		List<CustomEntityInstanceDto> dtos = new ArrayList<>();
		
		for (File ceiByCetDir : ceiDirectory.listFiles()) {
			if(!ceiByCetDir.isDirectory()) {
				continue;
			}
				
			for (File ceiFile : ceiByCetDir.listFiles()) {
				if (!ceiFile.getName().endsWith(".json")) {
					continue;
				}
			
				try  {
					String fileToString = org.apache.commons.io.FileUtils.readFileToString(ceiFile, StandardCharsets.UTF_8);
					Map<String, Object> data = JacksonUtil.fromString(fileToString, GenericTypeReferences.MAP_STRING_OBJECT);
					data.put("cetCode", ceiByCetDir.getName());
					
					CustomEntityInstance cei = CEIUtils.pojoToCei(data);
					cei.setCode((String) data.getOrDefault(data.get("code"), data.get("uuid")));
					dtos.add(toDto(cei));
				} catch (IOException e) {
					log.error("Failed to read CEI data", e);
				}
			}
		}
		
		return dtos;
	}

	public void create(CustomEntityInstanceDto dto) throws MeveoApiException, BusinessException {

		if (StringUtils.isBlank(dto.getCode())) {
			missingParameters.add("code");
		}
		if (StringUtils.isBlank(dto.getCetCode())) {
			missingParameters.add("cetCode");
		}

		handleMissingParameters();

		CustomEntityTemplate cet = customEntityTemplateService.findByCode(dto.getCetCode());
		if (cet == null) {
			throw new EntityDoesNotExistsException(CustomEntityTemplate.class, dto.getCetCode());
		}

		if (!currentUser.hasRole(CustomEntityTemplate.getModifyPermission(dto.getCetCode()))
				&& !currentUser.hasRole("ModifyAllCE")) {
			throw new ActionForbiddenException("User does not have permission '"
					+ CustomEntityTemplate.getModifyPermission(dto.getCetCode()) + "'");
		}

		if (customEntityInstanceService.findByCodeByCet(dto.getCetCode(), dto.getCode()) != null) {
			throw new EntityAlreadyExistsException(CustomEntityInstance.class, dto.getCode());
		}

		CustomEntityInstance cei = CustomEntityInstanceDto.fromDTO(dto, null);

		// populate customFields
		try {
			populateCustomFields(dto.getCustomFields(), cei, true);
		} catch (MissingParameterException | InvalidParameterException e) {
			log.error("Failed to associate custom field instance to an entity: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("Failed to associate custom field instance to an entity", e);
			throw e;
		}

		customEntityInstanceService.create(cei);
	}

	public void update(CustomEntityInstanceDto dto) throws MeveoApiException, BusinessException {

		if (StringUtils.isBlank(dto.getCode())) {
			missingParameters.add("code");
		}
		if (StringUtils.isBlank(dto.getCetCode())) {
			missingParameters.add("cetCode");
		}

		handleMissingParameters();

		CustomEntityTemplate cet = customEntityTemplateService.findByCode(dto.getCetCode());
		if (cet == null) {
			throw new EntityDoesNotExistsException(CustomEntityTemplate.class, dto.getCetCode());
		}

		if (!currentUser.hasRole(CustomEntityTemplate.getModifyPermission(dto.getCetCode()))
				&& !currentUser.hasRole("ModifyAllCE")) {
			throw new ActionForbiddenException("User does not have permission '"
					+ CustomEntityTemplate.getModifyPermission(dto.getCetCode()) + "'");
		}

		CustomEntityInstance cei = customEntityInstanceService.findByCodeByCet(dto.getCetCode(), dto.getCode());
		if (cei == null) {
			throw new EntityDoesNotExistsException(CustomEntityInstance.class, dto.getCode());
		}

		cei = CustomEntityInstanceDto.fromDTO(dto, cei);

		// populate customFields
		try {
			populateCustomFields(dto.getCustomFields(), cei, false);
		} catch (MissingParameterException e) {
			log.error("Failed to associate custom field instance to an entity: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("Failed to associate custom field instance to an entity", e);
			throw e;
		}

		cei = customEntityInstanceService.update(cei);
	}

	public void remove(String cetCode, String code) throws MeveoApiException, BusinessException {
		if (StringUtils.isBlank(code)) {
			missingParameters.add("code");
		}
		if (StringUtils.isBlank(cetCode)) {
			missingParameters.add("customEntityTemplateCode");
		}

		handleMissingParameters();

		if (!currentUser.hasRole(CustomEntityTemplate.getModifyPermission(cetCode))
				&& !currentUser.hasRole("ModifyAllCE")) {
			throw new ActionForbiddenException(
					"User does not have permission '" + CustomEntityTemplate.getModifyPermission(cetCode) + "'");
		}

		CustomEntityInstance cei = customEntityInstanceService.findByCodeByCet(cetCode, code);
		if (cei != null) {
			customEntityInstanceService.remove(cei);
		} else {
			throw new EntityDoesNotExistsException(CustomEntityInstance.class, code);
		}
	}

	public CustomEntityInstanceDto find(String cetCode, String code) throws MeveoApiException {
		if (StringUtils.isBlank(code)) {
			missingParameters.add("code");
		}
		if (StringUtils.isBlank(cetCode)) {
			missingParameters.add("customEntityTemplateCode");
		}

		handleMissingParameters();

		if (!currentUser.hasRole(CustomEntityTemplate.getReadPermission(cetCode))) {
			throw new ActionForbiddenException(
					"User does not have permission '" + CustomEntityTemplate.getReadPermission(cetCode) + "'");
		}

		CustomEntityInstance cei = customEntityInstanceService.findByCodeByCet(cetCode, code);

		if (cei == null) {
			throw new EntityDoesNotExistsException(CustomEntityTemplate.class, code);
		}
		return CustomEntityInstanceDto.toDTO(cei, entityToDtoConverter.getCustomFieldsDTO(cei, true));
	}

	public List<CustomEntityInstanceDto> list(String cetCode) throws MeveoApiException {
		if (StringUtils.isBlank(cetCode)) {
			missingParameters.add("customEntityTemplateCode");
		}

		handleMissingParameters();

		if (!currentUser.hasRole(CustomEntityTemplate.getReadPermission(cetCode))) {
			throw new ActionForbiddenException(
					"User does not have permission '" + CustomEntityTemplate.getReadPermission(cetCode) + "'");
		}

		Map<String, Object> filter = new HashMap<>();
		filter.put("cetCode", cetCode);
		PaginationConfiguration config = new PaginationConfiguration(filter);

		List<CustomEntityInstance> customEntityInstances = customEntityInstanceService.list(config);
		List<CustomEntityInstanceDto> customEntityInstanceDtos = new ArrayList<>();

		for (CustomEntityInstance instance : customEntityInstances) {
			customEntityInstanceDtos.add(
					CustomEntityInstanceDto.toDTO(instance, entityToDtoConverter.getCustomFieldsDTO(instance, true)));
		}

		return customEntityInstanceDtos;
	}

	@Override
	public CustomEntityInstance createOrUpdate(CustomEntityInstanceDto dto)
			throws MeveoApiException, BusinessException {

		CustomEntityInstance cei = customEntityInstanceService.findByCodeByCet(dto.getCetCode(), dto.getCode());
		if (cei == null) {
			create(dto);
		} else {
			update(dto);
		}

		return customEntityInstanceService.findByCodeByCet(dto.getCetCode(), dto.getCode());
	}

	/**
	 * Validate CustomEntityInstance DTO without saving it
	 * 
	 * @param ceiDto CustomEntityInstance DTO to validate
	 * @throws MeveoApiException meveo api exception.
	 */
	public void validateEntityInstanceDto(CustomEntityInstanceDto ceiDto) throws MeveoApiException {

		if (StringUtils.isBlank(ceiDto.getCode())) {
			missingParameters.add("code");
		}
		if (StringUtils.isBlank(ceiDto.getCetCode())) {
			missingParameters.add("cetCode");
		}
		handleMissingParameters();

		CustomEntityInstance cei = customEntityInstanceService.findByCodeByCet(ceiDto.getCetCode(), ceiDto.getCode());
		boolean isNew = cei == null;
		if (cei == null) {
			cei = new CustomEntityInstance();
			cei.setCetCode(ceiDto.getCetCode());
		}

		Map<String, CustomFieldTemplate> customFieldTemplates = customFieldTemplateService.findByAppliesTo(cei);

		validateAndConvertCustomFields(customFieldTemplates,
				ceiDto.getCustomFields() != null ? ceiDto.getCustomFields().getCustomField()
						: new ArrayList<CustomFieldDto>(),
				true, isNew, cei);
	}

	@Override
	public CustomEntityInstanceDto find(String code) throws MeveoApiException {
		CustomEntityInstance cei = customEntityInstanceService.findByCode(code);
		if (cei == null) {
			throw new EntityDoesNotExistsException(CustomEntityInstance.class.getSimpleName(), code);
		}
		return CustomEntityInstanceDto.toDTO(cei, entityToDtoConverter.getCustomFieldsDTO(cei, true));
	}

	@Override
	public CustomEntityInstanceDto toDto(CustomEntityInstance entity) {
		return CustomEntityInstanceDto.toDTO(entity, entityToDtoConverter.getCustomFieldsDTO(entity, true));
	}

	@Override
	public CustomEntityInstance fromDto(CustomEntityInstanceDto dto) throws MeveoApiException {
		return CustomEntityInstanceDto.fromDTO(dto, null);
	}

	@Override
	public IPersistenceService<CustomEntityInstance> getPersistenceService() {
		return customEntityInstanceService;
	}

	@Override
	public boolean exists(CustomEntityInstanceDto dto) {
		try {
			return find(dto.getCode()) != null;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void remove(CustomEntityInstanceDto dto) throws MeveoApiException, BusinessException {
		this.remove(dto.getCetCode(), dto.getCode());
	}

	public List<String> statesOfCEI(String cetCode, String cftCode, String uuid) throws EntityDoesNotExistsException, ELException, BusinessException {
		List<String> statesOfCEI = customEntityInstanceService.statesOfCEI(cetCode, cftCode, uuid);
		return statesOfCEI;
	}
}
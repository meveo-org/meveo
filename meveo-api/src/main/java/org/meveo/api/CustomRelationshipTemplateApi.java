package org.meveo.api;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.CustomFieldTemplateDto;
import org.meveo.api.dto.CustomRelationshipTemplateDto;
import org.meveo.api.exception.ActionForbiddenException;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.service.admin.impl.ModuleInstallationContext;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.service.storage.RepositoryService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * @author Rachid AITYAAZZA
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.12
 **/
@Stateless
public class CustomRelationshipTemplateApi extends BaseCrudApi<CustomRelationshipTemplate, CustomRelationshipTemplateDto> {

    /**
     * Instantiates a new CustomRelationshipTemplateApi
     */
    public CustomRelationshipTemplateApi() {
        super(CustomRelationshipTemplate.class, CustomRelationshipTemplateDto.class);
    }

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private CustomFieldTemplateApi customFieldTemplateApi;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    private RepositoryService repositoryService;
    
    @Inject
    private ModuleInstallationContext moduleInstallationContext;

    private void completeCrtData(CustomRelationshipTemplate crt, CustomRelationshipTemplateDto dto)
            throws EntityDoesNotExistsException {
        if (dto.getStartNodeCode() != null) {
            CustomEntityTemplate startNode = customEntityTemplateService.findByCode(dto.getStartNodeCode());
            if (startNode == null) {
                throw new EntityDoesNotExistsException(CustomEntityTemplate.class, dto.getStartNodeCode());
            }
            crt.setStartNode(startNode);
        }
        if (dto.getEndNodeCode() != null) {
            CustomEntityTemplate endNode = customEntityTemplateService.findByCode(dto.getEndNodeCode());
            if (endNode == null) {
                throw new EntityDoesNotExistsException(CustomEntityTemplate.class, dto.getEndNodeCode());
            }
            crt.setEndNode(endNode);
        }
        Gson gson = new GsonBuilder().create();
        Type itemType = new TypeToken<List<String>>() {
        }.getType();
        if (dto.getStartNodeKeys() != null && dto.getStartNodeKeys().size() > 0) {
            String startNodeKeyCodes = gson.toJson(dto.getStartNodeKeys(), itemType);
            crt.setStartNodeKey(startNodeKeyCodes);
        }
        if (dto.getEndNodeKeys() != null && dto.getEndNodeKeys().size() > 0) {
            String endNodeKeyCodes = gson.toJson(dto.getEndNodeKeys(), itemType);
            crt.setEndNodeKey(endNodeKeyCodes);
        }
        crt.setAudited(dto.isAudited());
    }

    public void createCustomRelationshipTemplate(CustomRelationshipTemplateDto dto)
            throws MeveoApiException, BusinessException {
        if (StringUtils.isBlank(dto.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(dto.getName())) {
            missingParameters.add("name");
        }

        handleMissingParameters();

        if (customRelationshipTemplateService.findByCode(dto.getCode()) != null) {
            throw new EntityAlreadyExistsException(CustomRelationshipTemplate.class, dto.getCode());
        }

        CustomRelationshipTemplate crt = fromDto(dto);
        completeCrtData(crt, dto);
        
        // Override repositories on module installation
        if (moduleInstallationContext.isActive()) {
        	crt.setRepositories(new ArrayList<>());
        	crt.getRepositories().addAll(moduleInstallationContext.getRepositories());
        }

        customRelationshipTemplateService.create(crt);
        
        // CFTs will be handled by module installation
        if (!moduleInstallationContext.isActive()) {
        	synchronizeCustomFields(crt.getAppliesTo(), dto.getFields());
        }

    }

    public void updateCustomRelationshipTemplate(CustomRelationshipTemplateDto dto)
            throws MeveoApiException, BusinessException {
        if (StringUtils.isBlank(dto.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(dto.getName())) {
            missingParameters.add("name");
        }

        handleMissingParameters();

        CustomRelationshipTemplate crt = customRelationshipTemplateService
                .findByCode(dto.getCode());
        if (crt == null) {
            throw new EntityDoesNotExistsException(CustomRelationshipTemplate.class, dto.getCode());
        }

        crt = CustomRelationshipTemplateDto.fromDTO(dto, crt);

        completeCrtData(crt, dto);

        crt = customRelationshipTemplateService.update(crt);

        customRelationshipTemplateService.synchronizeStorages(crt);

        // CFTs will be handled by module installation
        if (!moduleInstallationContext.isActive()) {
        	synchronizeCustomFields(crt.getAppliesTo(), dto.getFields());
        }
    }

    public void removeCustomRelationshipTemplate(String code) throws MeveoApiException, BusinessException {
        if (StringUtils.isBlank(code)) {
            missingParameters.add("customEntityTemplateCode");
        }

        handleMissingParameters();

        CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(code);
        if (crt == null) {
            throw new EntityDoesNotExistsException(CustomRelationshipTemplate.class, code);
        }

        // Related custom field templates will be removed along with CET
        customRelationshipTemplateService.remove(crt);
    }

    public CustomRelationshipTemplateDto findCustomRelationshipTemplate(String code) throws MeveoApiException {
        if (StringUtils.isBlank(code)) {
            missingParameters.add("customEntityTemplateCode");
        }

        handleMissingParameters();

        CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(code);

        if (crt == null) {
            throw new EntityDoesNotExistsException(CustomRelationshipTemplate.class, code);
        }

        Map<String, CustomFieldTemplate> cetFields = customFieldTemplateService.findByAppliesTo(crt.getAppliesTo());

        return CustomRelationshipTemplateDto.toDTO(crt, cetFields.values());
    }

    public void createOrUpdateCustomRelationshipTemplate(CustomRelationshipTemplateDto postData)
            throws MeveoApiException, BusinessException {
        CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(postData.getCode());
        if (crt == null) {
            createCustomRelationshipTemplate(postData);
        } else {
            updateCustomRelationshipTemplate(postData);
        }
    }


    public List<CustomRelationshipTemplateDto> listCustomRelationshipTemplates(String code) {
        List<CustomRelationshipTemplate> crts;
        if (StringUtils.isBlank(code)) {
            crts = customRelationshipTemplateService.list();
        } else {
            crts = customRelationshipTemplateService.findByCodeLike(code);
        }

        List<CustomRelationshipTemplateDto> crtDtos = new ArrayList<>();

        return crtDtos;
    }


    private void synchronizeCustomFields(String appliesTo, List<CustomFieldTemplateDto> fields)
            throws MeveoApiException, BusinessException {

        Map<String, CustomFieldTemplate> cetFields = customFieldTemplateService.findByAppliesTo(appliesTo);

        // Create, update or remove fields as necessary
        List<CustomFieldTemplate> cftsToRemove = new ArrayList<>();
        if (fields != null && !fields.isEmpty()) {

            for (CustomFieldTemplate cft : cetFields.values()) {
                boolean found = false;
                for (CustomFieldTemplateDto cftDto : fields) {
                    if (cftDto.getCode().equals(cft.getCode())) {
                        found = true;
                        break;
                    }
                }

                // Old field is no longer needed. Remove by id, as CFT might come detached from cache
                if (!found && cft != null) {
                    cftsToRemove.add(cft);
                }
            }
            // Update or create custom field templates
            for (CustomFieldTemplateDto cftDto : fields) {
                customFieldTemplateApi.createOrUpdate(cftDto, appliesTo);
            }

        } else {
            cftsToRemove.addAll(cetFields.values());
        }
        
        for (CustomFieldTemplate cft : cftsToRemove) {
            customFieldTemplateService.remove(cft.getId());
        }


    }

    @Override
    public CustomRelationshipTemplateDto find(String code) throws MeveoApiException {
        return findCustomRelationshipTemplate(code);
    }

    @Override
    public CustomRelationshipTemplate createOrUpdate(CustomRelationshipTemplateDto dtoData)
            throws MeveoApiException, BusinessException {
        createOrUpdateCustomRelationshipTemplate(dtoData);
        return customRelationshipTemplateService.findByCode(dtoData.getCode());
    }

    @Override
    public CustomRelationshipTemplateDto toDto(CustomRelationshipTemplate entity) {
        Collection<CustomFieldTemplate> cfts = customFieldTemplateService
                .findByAppliesTo(entity.getAppliesTo()).values();
        return CustomRelationshipTemplateDto.toDTO(entity, cfts);
    }

    @Override
    public CustomRelationshipTemplate fromDto(CustomRelationshipTemplateDto dto) throws MeveoApiException {
        var crt = CustomRelationshipTemplateDto.fromDTO(dto, null);
        
        // Parse repositories where to create the CRT data
        if (crt.getRepositories() == null || crt.getRepositories().isEmpty()) {
	        if (dto.getRepositories() == null || dto.getRepositories().isEmpty()) {
	        	crt.setRepositories(List.of(repositoryService.findDefaultRepository()));
	        } else {
	        	crt.setRepositories(new ArrayList<>());
	        	dto.getRepositories().forEach(repository -> {
					var storageRepo = repositoryService.findByCode(repository);
					if (storageRepo != null) {
						crt.getRepositories().add(storageRepo);
					} else {
						throw new IllegalArgumentException("Repository " + repository + " does not exists");
					}
				});
	        }
        }
        return crt;
    }

    @Override
    public IPersistenceService<CustomRelationshipTemplate> getPersistenceService() {
        return customRelationshipTemplateService;
    }

    @Override
    public boolean exists(CustomRelationshipTemplateDto dto) {
        return customRelationshipTemplateService.findByCode(dto.getCode()) != null;
    }

    @Override
    public void remove(CustomRelationshipTemplateDto dto) throws MeveoApiException, BusinessException {
        this.removeCustomRelationshipTemplate(dto.getCode());
    }


}
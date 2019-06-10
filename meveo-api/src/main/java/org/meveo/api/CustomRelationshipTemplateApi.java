package org.meveo.api;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.CustomFieldTemplateDto;
import org.meveo.api.dto.CustomRelationshipTemplateDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * @author Rachid AITYAAZZA
 **/
@Stateless
public class CustomRelationshipTemplateApi extends BaseApi {

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;
    
    @Inject
    private CustomEntityTemplateService customEntityTemplateService;


    @Inject
    private CustomFieldTemplateApi customFieldTemplateApi;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;


    private void completeCrtData(CustomRelationshipTemplate crt,CustomRelationshipTemplateDto dto) throws EntityDoesNotExistsException{
    	if(dto.getStartNodeCode()!=null){
    		CustomEntityTemplate startNode=customEntityTemplateService.findByCode(dto.getStartNodeCode());
            if (startNode == null) {
                throw new EntityDoesNotExistsException(CustomEntityTemplate.class, dto.getStartNodeCode());
            }
            crt.setStartNode(startNode);
    	}
    	if(dto.getEndNodeCode()!=null){
    		 CustomEntityTemplate endNode=customEntityTemplateService.findByCode(dto.getEndNodeCode());
    	        if (endNode == null) {
    	            throw new EntityDoesNotExistsException(CustomEntityTemplate.class, dto.getEndNodeCode());
    	        }
    	     crt.setEndNode(endNode);
    	}
    	Gson gson = new GsonBuilder().create();
        Type itemType = new TypeToken<List<String>>() {}.getType();
    	if(dto.getStartNodeKeys()!=null && dto.getStartNodeKeys().size()>0){
    		String startNodeKeyCodes = gson.toJson(dto.getStartNodeKeys(), itemType);
            crt.setStartNodeKey(startNodeKeyCodes);
    	}
    	if(dto.getEndNodeKeys()!=null && dto.getEndNodeKeys().size()>0){
    		String endNodeKeyCodes = gson.toJson(dto.getEndNodeKeys(), itemType);
            crt.setEndNodeKey(endNodeKeyCodes);
    	}
	    
        
    }

    public void createCustomRelationshipTemplate(CustomRelationshipTemplateDto dto) throws MeveoApiException, BusinessException {

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

        CustomRelationshipTemplate crt = CustomRelationshipTemplateDto.fromDTO(dto, null);
        completeCrtData(crt, dto);
        
        customRelationshipTemplateService.create(crt);


    }

    public void updateCustomRelationshipTemplate(CustomRelationshipTemplateDto dto) throws MeveoApiException, BusinessException {

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

        synchronizeCustomFields(crt.getAppliesTo(), dto.getFields());
    }

    public void removeCustomRelationshipTemplate(String code) throws EntityDoesNotExistsException, MissingParameterException, BusinessException {
        if (StringUtils.isBlank(code)) {
            missingParameters.add("customEntityTemplateCode");
        }

        handleMissingParameters();

        CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(code);
        if (crt != null) {
            // Related custom field templates will be removed along with CET
            customRelationshipTemplateService.remove(crt);
        } else {
            throw new EntityDoesNotExistsException(CustomRelationshipTemplate.class, code);
        }
    }

    public CustomRelationshipTemplateDto findCustomRelationshipTemplate(String code) throws EntityDoesNotExistsException, MissingParameterException {
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

    public void createOrUpdateCustomRelationshipTemplate(CustomRelationshipTemplateDto postData) throws MeveoApiException, BusinessException {
        CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(postData.getCode());
        if (crt == null) {
            createCustomRelationshipTemplate(postData);
        } else {
            updateCustomRelationshipTemplate(postData);
        }
    }



    public List<CustomRelationshipTemplateDto> listCustomRelationshipTemplates(String code) {

        List<CustomRelationshipTemplate> cets;
        if (StringUtils.isBlank(code)) {
            cets = customRelationshipTemplateService.list();
        } else {
            cets = customRelationshipTemplateService.findByCodeLike(code);
        }

        List<CustomRelationshipTemplateDto> cetDtos = new ArrayList<>();

        for (CustomRelationshipTemplate crt : cets) {

            Map<String, CustomFieldTemplate> cetFields = customFieldTemplateService.findByAppliesTo(crt.getAppliesTo());

            cetDtos.add(CustomRelationshipTemplateDto.toDTO(crt, cetFields.values()));
        }

        return cetDtos;
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
                if (!found) {
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

   
}
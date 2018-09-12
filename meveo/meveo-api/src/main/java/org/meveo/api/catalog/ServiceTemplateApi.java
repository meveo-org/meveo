package org.meveo.api.catalog;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.exception.*;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.catalog.BusinessServiceModel;
import org.meveo.model.catalog.Calendar;
import org.meveo.model.catalog.ServiceTemplate;
import org.meveo.model.crm.custom.CustomFieldInheritanceEnum;
import org.meveo.service.catalog.impl.BusinessServiceModelService;
import org.meveo.service.catalog.impl.CalendarService;
import org.meveo.service.catalog.impl.ServiceTemplateService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Edward P. Legaspi
 * @author akadid abdelmounaim
 * @lastModifiedVersion 5.0.1
 **/
@Stateless
public class ServiceTemplateApi extends BaseCrudApi<ServiceTemplate, ServiceTemplateDto> {

    @Inject
    private ServiceTemplateService serviceTemplateService;

    @Inject
    private BusinessServiceModelService businessServiceModelService;

    @Inject
    private CalendarService calendarService;


    public ServiceTemplate create(ServiceTemplateDto postData) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");            
        }
        
        handleMissingParametersAndValidate(postData);        

        // check if code already exists
        if (serviceTemplateService.findByCode(postData.getCode()) != null) {
            throw new EntityAlreadyExistsException(ServiceTemplateService.class, postData.getCode());
        }

        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setCode(postData.getCode());
        serviceTemplate.setDescription(postData.getDescription());
        serviceTemplate.setLongDescription(postData.getLongDescription());
        serviceTemplate.setMinimumAmountEl(postData.getMinimumAmountEl());
        serviceTemplate.setMinimumLabelEl(postData.getMinimumLabelEl());
        try {
			saveImage(serviceTemplate, postData.getImagePath(), postData.getImageBase64());
		} catch (IOException e1) {
			log.error("Invalid image data={}", e1.getMessage());
			throw new InvalidImageData();
		}

        // populate customFields
        try {
            populateCustomFields(postData.getCustomFields(), serviceTemplate, true);
        } catch (MissingParameterException | InvalidParameterException e) {
            log.error("Failed to associate custom field instance to an entity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to associate custom field instance to an entity", e);
            throw e;
        }

        serviceTemplateService.create(serviceTemplate);

        return serviceTemplate;
    }

    public ServiceTemplate update(ServiceTemplateDto postData) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        
        handleMissingParametersAndValidate(postData);
        
        // check if code already exists
        ServiceTemplate serviceTemplate = serviceTemplateService.findByCode(postData.getCode());
        if (serviceTemplate == null) {
            throw new EntityDoesNotExistsException(ServiceTemplateService.class, postData.getCode());
        }
        serviceTemplate.setCode(StringUtils.isBlank(postData.getUpdatedCode())?postData.getCode():postData.getUpdatedCode());
        serviceTemplate.setDescription(postData.getDescription());
        serviceTemplate.setLongDescription(postData.getLongDescription());
        serviceTemplate.setMinimumAmountEl(postData.getMinimumAmountEl());
        serviceTemplate.setMinimumLabelEl(postData.getMinimumLabelEl());

        Calendar invoicingCalendar = null;
        if (postData.getInvoicingCalendar() != null) {
            invoicingCalendar = calendarService.findByCode(postData.getInvoicingCalendar());
            if (invoicingCalendar == null) {
                throw new EntityDoesNotExistsException(Calendar.class, postData.getInvoicingCalendar());
            }
        }
        serviceTemplate.setInvoicingCalendar(invoicingCalendar);
        
        BusinessServiceModel businessService = null;
		if (!StringUtils.isBlank(postData.getSomCode())) {
			businessService = businessServiceModelService.findByCode(postData.getSomCode());
			if (businessService == null) {
				throw new EntityDoesNotExistsException(BusinessServiceModel.class, postData.getSomCode());
			}
		}
		serviceTemplate.setBusinessServiceModel(businessService);

        try {
			saveImage(serviceTemplate, postData.getImagePath(), postData.getImageBase64());
		} catch (IOException e1) {
			log.error("Invalid image data={}", e1.getMessage());
			throw new InvalidImageData();
		}

        // populate customFields
        try {
            populateCustomFields(postData.getCustomFields(), serviceTemplate, false);
        } catch (MissingParameterException | InvalidParameterException e) {
            log.error("Failed to associate custom field instance to an entity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to associate custom field instance to an entity", e);
            throw e;
        }

        serviceTemplate = serviceTemplateService.update(serviceTemplate);
        return serviceTemplate;
    }

    /* (non-Javadoc)
     * @see org.meveo.api.ApiService#find(java.lang.String)
     */
    @Override
    public ServiceTemplateDto find(String serviceTemplateCode) throws MeveoApiException {
        return find(serviceTemplateCode, CustomFieldInheritanceEnum.INHERIT_NO_MERGE);
    }

    public ServiceTemplateDto find(String serviceTemplateCode, CustomFieldInheritanceEnum inheritCF) throws MeveoApiException {

        if (StringUtils.isBlank(serviceTemplateCode)) {
            missingParameters.add("serviceTemplateCode");
            handleMissingParameters();
        }

        ServiceTemplate serviceTemplate = serviceTemplateService.findByCode(serviceTemplateCode);
        if (serviceTemplate == null) {
            throw new EntityDoesNotExistsException(ServiceTemplate.class, serviceTemplateCode);
        }
        ServiceTemplateDto result = new ServiceTemplateDto(serviceTemplate, entityToDtoConverter.getCustomFieldsDTO(serviceTemplate, inheritCF));
        return result;
    }
    

    public void remove(String serviceTemplateCode) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(serviceTemplateCode)) {
            missingParameters.add("serviceTemplateCode");
            handleMissingParameters();
        }

        ServiceTemplate serviceTemplate = serviceTemplateService.findByCode(serviceTemplateCode);
        if (serviceTemplate == null) {
            throw new EntityDoesNotExistsException(ServiceTemplate.class, serviceTemplateCode);
        }


    }

    public ServiceTemplate createOrUpdate(ServiceTemplateDto postData) throws MeveoApiException, BusinessException {    	
        if (serviceTemplateService.findByCode(postData.getCode()) == null) {
            return create(postData);
        } else {
            return update(postData);
        }
    }
}
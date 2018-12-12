package org.meveo.api.rest.custom.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.CustomRelationshipTemplateApi;
import org.meveo.api.MeveoApiErrorCodeEnum;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.CustomRelationshipTemplateDto;
import org.meveo.api.dto.response.CustomRelationshipTemplateResponseDto;
import org.meveo.api.dto.response.CustomRelationshipTemplatesResponseDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.custom.CustomRelationshipTemplateRs;
import org.meveo.api.rest.impl.BaseRs;

/**
 * @author Rachid AITYAAZZA
 **/
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class CustomRelationshipTemplateRsImpl extends BaseRs implements CustomRelationshipTemplateRs {

    @Inject
    private CustomRelationshipTemplateApi customRelationshipTemplateApi;

    
    
    @Override
	public ActionStatus createCustomRelationshipTemplate(
			CustomRelationshipTemplateDto dto) {
    	 ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

         try {
             customRelationshipTemplateApi.createCustomRelationshipTemplate(dto);

         } catch (MeveoApiException e) {
             result.setErrorCode(e.getErrorCode());
             result.setStatus(ActionStatusEnum.FAIL);
             result.setMessage(e.getMessage());
         } catch (Exception e) {
             log.error("Failed to execute API", e);
             result.setErrorCode(e instanceof BusinessException ? MeveoApiErrorCodeEnum.BUSINESS_API_EXCEPTION : MeveoApiErrorCodeEnum.GENERIC_API_EXCEPTION);
             result.setStatus(ActionStatusEnum.FAIL);
             result.setMessage(e.getMessage());
         }

         return result;
	}

	@Override
	public ActionStatus updateCustomRelationshipTemplate(
			CustomRelationshipTemplateDto dto) {
		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            customRelationshipTemplateApi.updateCustomRelationshipTemplate(dto);
        } catch (MeveoApiException e) {
            result.setErrorCode(e.getErrorCode());
            result.setStatus(ActionStatusEnum.FAIL);
            result.setMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to execute API", e);
            result.setErrorCode(e instanceof BusinessException ? MeveoApiErrorCodeEnum.BUSINESS_API_EXCEPTION : MeveoApiErrorCodeEnum.GENERIC_API_EXCEPTION);
            result.setStatus(ActionStatusEnum.FAIL);
            result.setMessage(e.getMessage());
        }

        return result;
	}

	@Override
	public ActionStatus removeCustomRelationshipTemplate(String customCustomRelationshipTemplateCode) {
	       ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

	        try {
	            customRelationshipTemplateApi
                        .removeCustomRelationshipTemplate(customCustomRelationshipTemplateCode);
	        } catch (MeveoApiException e) {
	            result.setErrorCode(e.getErrorCode());
	            result.setStatus(ActionStatusEnum.FAIL);
	            result.setMessage(e.getMessage());
	        } catch (Exception e) {
	            log.error("Failed to execute API", e);
	            result.setErrorCode(e instanceof BusinessException ? MeveoApiErrorCodeEnum.BUSINESS_API_EXCEPTION : MeveoApiErrorCodeEnum.GENERIC_API_EXCEPTION);
	            result.setStatus(ActionStatusEnum.FAIL);
	            result.setMessage(e.getMessage());
	        }

	        return result;
	}

	@Override
	public CustomRelationshipTemplateResponseDto findCustomRelationshipTemplate(String customCustomRelationshipTemplateCode) {
		CustomRelationshipTemplateResponseDto result = new CustomRelationshipTemplateResponseDto();

	        try {
	            result.setCustomRelationshipTemplate(customRelationshipTemplateApi.findCustomRelationshipTemplate(customCustomRelationshipTemplateCode));
	        } catch (MeveoApiException e) {
	            result.getActionStatus().setErrorCode(e.getErrorCode());
	            result.getActionStatus().setStatus(ActionStatusEnum.FAIL);
	            result.getActionStatus().setMessage(e.getMessage());
	        } catch (Exception e) {
	            log.error("Failed to execute API", e);
	            result.getActionStatus().setErrorCode(MeveoApiErrorCodeEnum.GENERIC_API_EXCEPTION);
	            result.getActionStatus().setStatus(ActionStatusEnum.FAIL);
	            result.getActionStatus().setMessage(e.getMessage());
	        }

	        return result;
	 }

	@Override
	public CustomRelationshipTemplatesResponseDto listCustomRelationshipTemplates(
			String customCustomRelationshipTemplateCode) {
		CustomRelationshipTemplatesResponseDto result = new CustomRelationshipTemplatesResponseDto();

	        try {
	            result.setCustomRelationshipTemplates(customRelationshipTemplateApi.listCustomRelationshipTemplates(customCustomRelationshipTemplateCode));

	       } catch (Exception e) {
	            log.error("Failed to execute API", e);
	            result.getActionStatus().setErrorCode(MeveoApiErrorCodeEnum.GENERIC_API_EXCEPTION);
	            result.getActionStatus().setStatus(ActionStatusEnum.FAIL);
	            result.getActionStatus().setMessage(e.getMessage());
	        }

	        return result;
	}

	@Override
	public ActionStatus createOrUpdateCustomRelationshipTemplate(
			CustomRelationshipTemplateDto dto) {
		  ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

	        try {
	            customRelationshipTemplateApi.createOrUpdateCustomRelationshipTemplate(dto);
	        } catch (MeveoApiException e) {
	            result.setErrorCode(e.getErrorCode());
	            result.setStatus(ActionStatusEnum.FAIL);
	            result.setMessage(e.getMessage());
	        } catch (Exception e) {
	            log.error("Failed to execute API", e);
	            result.setErrorCode(e instanceof BusinessException ? MeveoApiErrorCodeEnum.BUSINESS_API_EXCEPTION : MeveoApiErrorCodeEnum.GENERIC_API_EXCEPTION);
	            result.setStatus(ActionStatusEnum.FAIL);
	            result.setMessage(e.getMessage());
	        }

	        return result;
	}
    


	
}
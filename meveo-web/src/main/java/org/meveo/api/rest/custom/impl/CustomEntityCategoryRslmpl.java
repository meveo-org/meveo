package org.meveo.api.rest.custom.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.api.custom.CustomEntityCategoryApi;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.CustomEntityCategoryDto;
import org.meveo.api.dto.response.CustomEntityCategoriesResponseDto;
import org.meveo.api.dto.response.CustomEntityCategoryResponseDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.custom.CustomEntityCategoryRs;
import org.meveo.api.rest.impl.BaseRs;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.4.0
 */
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class CustomEntityCategoryRslmpl extends BaseRs implements CustomEntityCategoryRs {

    @Inject
    private CustomEntityCategoryApi customEntityCategoryApi;

    @Override
    public ActionStatus createOrUpdateEntityCategory(CustomEntityCategoryDto dto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            customEntityCategoryApi.createOrUpdate(dto);
            
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus removeEntityCategory(String customEntityCategoryCode, boolean deleteRelatedTemplates) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            customEntityCategoryApi.removeCustomEntityCategory(customEntityCategoryCode, deleteRelatedTemplates);
            
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

	@Override
	public ActionStatus create(CustomEntityCategoryDto postData) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            customEntityCategoryApi.create(postData);
            
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
	}

	@Override
	public ActionStatus update(CustomEntityCategoryDto postData) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            customEntityCategoryApi.update(postData);
            
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
	}

	@Override
	public CustomEntityCategoryResponseDto find(String code) {

		CustomEntityCategoryResponseDto result = new CustomEntityCategoryResponseDto();

        try {
            result.setCustomEntityCategory(customEntityCategoryApi.find(code));;
            
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
	}

	@Override
	public CustomEntityCategoriesResponseDto list(PagingAndFiltering pagingAndFiltering) {

		CustomEntityCategoriesResponseDto result = new CustomEntityCategoriesResponseDto();

        try {
        	result = customEntityCategoryApi.list(pagingAndFiltering);
            
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
	}

}

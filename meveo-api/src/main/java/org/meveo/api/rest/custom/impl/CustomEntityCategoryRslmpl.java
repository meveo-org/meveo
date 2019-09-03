package org.meveo.api.rest.custom.impl;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.CustomEntityCategoryDto;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.CustomEntityCategoryApi;
import org.meveo.api.rest.custom.CustomEntityCategoryRs;
import org.meveo.api.rest.impl.BaseRs;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class CustomEntityCategoryRslmpl extends BaseRs implements CustomEntityCategoryRs {

    @Inject
    private CustomEntityCategoryApi customEntityCategoryApi;

    @Override
    public ActionStatus createOrUpdateEntityCategory(CustomEntityCategoryDto dto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            customEntityCategoryApi.createOrUpdateEntityCategory(dto);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus removeEntityCategory(String customEntityCategoryCode) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            customEntityCategoryApi.removeCustomEntityCategory(customEntityCategoryCode);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

}

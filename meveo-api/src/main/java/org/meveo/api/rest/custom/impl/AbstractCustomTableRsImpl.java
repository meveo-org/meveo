package org.meveo.api.rest.custom.impl;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ValidationException;
import org.meveo.api.custom.ICustomTableApi;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.custom.CustomTableDataDto;
import org.meveo.api.dto.custom.CustomTableDataResponseDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.custom.ICustomTableRs;

//@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public abstract class AbstractCustomTableRsImpl <T extends CustomTableDataDto> implements ICustomTableRs<T> {

    @Inject @Any
    private ICustomTableApi<T> customTableApi;

    public void append(T dto) throws MeveoApiException, BusinessException {
        customTableApi.create(dto);
    }

    public void update(T dto) throws MeveoApiException, BusinessException {
        customTableApi.update(dto);
    }

    public void remove(T dto) throws MeveoApiException, BusinessException {
        customTableApi.remove(dto);
    }

    public CustomTableDataResponseDto list(String customTableCode, PagingAndFiltering pagingAndFiltering) throws MissingParameterException, EntityDoesNotExistsException, InvalidParameterException, ValidationException {
        return customTableApi.list(customTableCode, pagingAndFiltering);
    }

    public void createOrUpdate(T dto) throws MeveoApiException, BusinessException {
        customTableApi.createOrUpdate(dto);
    }

	@Override
	public ActionStatus index() {
		return null;
	}

}
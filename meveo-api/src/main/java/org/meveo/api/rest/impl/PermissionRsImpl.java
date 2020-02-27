package org.meveo.api.rest.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.api.PermissionApi;
import org.meveo.api.dto.response.PermissionResponseDto;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.PermissionRs;

@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class PermissionRsImpl extends BaseRs implements PermissionRs {

    @Inject
    private PermissionApi permissionApi;
    
    @Override
    public PermissionResponseDto list() {
        PermissionResponseDto result = new PermissionResponseDto();
        try {
            result.setPermissionsDto(permissionApi.list());
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

	public void addToWhiteList(String permission, String id, String role) {
		permissionApi.addToWhiteList(permission, id, role);
	}

	public void addToBlackList(String permission, String id, String role) {
		permissionApi.addToBlackList(permission, id, role);
	}

	public void removeFromWhiteList(String permission, String id, String role) {
		permissionApi.removeEntityPermission(permission, id, role);
	}

	public void removeFromBlackList(String permission, String id, String role) {
		permissionApi.removeEntityPermission(permission, id, role);
	}

}

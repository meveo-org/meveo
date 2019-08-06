package org.meveo.api.rest.storage.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.rest.storage.FileSystemRs;
import org.meveo.api.storage.FileSystemApi;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 */
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class FileSystemRsImpl extends BaseRs implements FileSystemRs {

	@Context
	private HttpServletResponse httpServletResponse;

	@Inject
	private FileSystemApi fileSystemApi;

	@Override
	public ActionStatus findBinary(Boolean showOnExplorer, String repositoryCode, String cetCode, String uuid, String cftCode) {
		ActionStatus result = new ActionStatus();

		try {
			fileSystemApi.findBinary(showOnExplorer, repositoryCode, cetCode, uuid, cftCode, httpServletResponse);

		} catch (Exception e) {
			processException(e, result);
		}

		return result;
	}

}

package org.meveo.api.rest.storage.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.core.Response;

import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.rest.storage.FileSystemRs;
import org.meveo.api.storage.FileSystemApi;
import org.meveo.exceptions.EntityDoesNotExistsException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 */
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class FileSystemRsImpl extends BaseRs implements FileSystemRs {


	@Inject
	private FileSystemApi fileSystemApi;

	@Override
	public Response findBinary(Integer index, String repositoryCode, String cetCode, String uuid, String cftCode) throws IOException, EntityDoesNotExistsException, BusinessApiException, org.meveo.api.exception.EntityDoesNotExistsException {
			File file = fileSystemApi.findBinary(repositoryCode, cetCode, uuid, cftCode, index);

			if(file == null){
				return Response.status(404).build();
			}

			return Response.ok(file, Files.probeContentType(file.toPath())).build();
	}

}

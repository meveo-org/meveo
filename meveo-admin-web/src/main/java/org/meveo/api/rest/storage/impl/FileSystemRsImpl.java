package org.meveo.api.rest.storage.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.rest.storage.FileSystemRs;
import org.meveo.api.storage.BinaryPersistenceApi;
import org.meveo.api.storage.FileSystemApi;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.customEntities.BinaryProvider;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 */
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class FileSystemRsImpl extends BaseRs implements FileSystemRs {

	@Inject
	private FileSystemApi fileSystemApi;
	
	@Inject
	private BinaryPersistenceApi binaryPersistenceApi;

	@Override
	public Response findBinary(Integer index, String fileName, String repositoryCode, String cetCode, String uuid, String cftCode) throws IOException, EntityDoesNotExistsException, BusinessApiException, org.meveo.api.exception.EntityDoesNotExistsException {
			List<BinaryProvider> binaries = binaryPersistenceApi.getBinaries(repositoryCode, cetCode, uuid, cftCode);
			if (binaries != null && !binaries.isEmpty()) {
				BinaryProvider binaryProvider;
				if (StringUtils.isNotBlank(fileName)) {
					binaryProvider = binaries.stream()
							.filter(binary -> binary.getFileName().equals(fileName))
							.findFirst()
							.orElseThrow(() -> new EntityDoesNotExistsException("File with name " + fileName + " does not exists"));
				} else {
					if (index == null) {
						index = 0;
					}
					binaryProvider = binaries.get(index);
				}
				
				if (binaryProvider != null) {
					return Response.ok(binaryProvider.getBinary(), binaryProvider.getContentType()).build();
				}
			}
		
			File file = fileSystemApi.findBinary(repositoryCode, cetCode, uuid, cftCode, index);

			if(file == null){
				return Response.status(404).build();
			}

			return Response.ok(file, Files.probeContentType(file.toPath())).build();
	}

}

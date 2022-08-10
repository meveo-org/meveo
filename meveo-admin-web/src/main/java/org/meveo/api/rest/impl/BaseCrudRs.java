/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.api.rest.impl;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.rest.IBaseBaseCrudRs;
import org.meveo.model.IEntity;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 
 * @author clement.bareth
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 *
 * @param <T>
 * @param <D>
 */
public abstract class BaseCrudRs<T extends IEntity, D extends BaseEntityDto> extends BaseRs implements IBaseBaseCrudRs {
	
	@Inject
	private Logger logger;
	
	public abstract BaseCrudApi<T, D> getBaseCrudApi();
	
	@Override
	public void importData(MultipartFormDataInput input, boolean overwrite) throws IOException {
		try {
			for (InputPart inputPart : input.getFormDataMap().get("file")) {
				InputStream file = inputPart.getBody(InputStream.class, null);
				switch (inputPart.getMediaType().getType() + "/" + inputPart.getMediaType().getSubtype()) {
					case MediaType.APPLICATION_XML:
					case MediaType.TEXT_XML:
						getBaseCrudApi().importXML(file, overwrite);
						break;
						
					case MediaType.APPLICATION_JSON:
						getBaseCrudApi().importJSON(file, overwrite);
						break;
						
					case "text/csv":
					case "application/vnd.ms-excel":
						getBaseCrudApi().importCSV(file, overwrite);
						break;
						
					default:
						throw new BadRequestException("Unsupported file type : " + inputPart.getMediaType().toString());

				}
			}
		
		} catch (BusinessException e) {
			throw new ServerErrorException(500, e);
		}
		
	}
	
	@Override
	public void importCSV(File csv, boolean overwrite) {
		try {
			getBaseCrudApi().importCSV(new FileInputStream(csv), overwrite);
		} catch (IOException | BusinessException e) {
			logger.error("Cannot import CSV file", e);
			throw new ServerErrorException(500, e);
		}
		
	}

	@Override
	public void importXML(File csv, boolean overwrite) {
		try {
			getBaseCrudApi().importXML(new FileInputStream(csv), overwrite);
		} catch (IOException | BusinessException e) {
			logger.error("Cannot import CSV file", e);
			throw new ServerErrorException(500, e);
		}
		
	}

	@Override
	public void importJSON(File csv, boolean overwrite) {
		try {
			getBaseCrudApi().importJSON(new FileInputStream(csv), overwrite);
		} catch (IOException | BusinessException e) {
			logger.error("Cannot import CSV file", e);
			throw new ServerErrorException(500, e);
		}
		
	}

	@Override
	public File exportCSV(PagingAndFiltering config) {
		try {
			return getBaseCrudApi().exportCSV(config);
		} catch (MeveoApiException e) {
			throw new ServerErrorException(500, e);
		}
	}

	@Override
	public File exportJSON(PagingAndFiltering config) {
		try {
			return getBaseCrudApi().exportJSON(config);
		} catch (MeveoApiException e) {
			throw new ServerErrorException(500, e);
		}
	}

	@Override
	public File exportXML(PagingAndFiltering config) {
		try {
			return getBaseCrudApi().exportXML(config);
		} catch (MeveoApiException e) {
			throw new ServerErrorException(500, e);
		}
	}

	
}

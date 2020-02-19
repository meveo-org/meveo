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
package org.meveo.api;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.export.ExportFormat;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.IEntity;
import org.meveo.model.module.MeveoModule;
import org.meveo.service.base.local.IPersistenceService;
import org.primefaces.model.SortOrder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

/**
 * Base API service for CRUD operations on entity
 * 
 * @author Andrius Karpavicius
 * 
 * @param <E> Entity class
 * @param <T> Dto class
 */
public abstract class BaseCrudApi<E extends IEntity, T extends BaseEntityDto> extends BaseApi implements ApiService<E, T> {

	private Class<T> dtoClass;
	private Class<E> jpaClass;
	private Set<File> fileImport = new HashSet<>();;

	public BaseCrudApi(Class<E> jpaClass, Class<T> dtoClass) {
		super();
		this.dtoClass = dtoClass;
		this.jpaClass = jpaClass;
	}

	/**
	 * Function used to construct a dto representation of a given JPA entity
	 *
	 * @param entity Entity to convert
	 * @return Entity converted
	 */
	public abstract T toDto(E entity);

	/**
	 * Build a JPA representation from a DTO
	 *
	 * @param dto DTO to convert
	 * @return The JPA entity built from the DTO
	 * @throws org.meveo.exceptions.EntityDoesNotExistsException if a linked entity does not exists
	 */
	public abstract E fromDto(T dto) throws org.meveo.exceptions.EntityDoesNotExistsException;

	/**
	 * @return The persistence service used by the implementation
	 */
	public abstract IPersistenceService<E> getPersistenceService();

	/**
	 * Use a dto to check if the JPA version already exists or not
	 *
	 * @param dto DTO representation to use
	 * @return <code>true</code> if the JPA version of the entity exists
	 */
	public abstract boolean exists(T dto);

	/*
	 * (non-Javadoc)
	 *
	 * @see org.meveo.api.ApiService#findIgnoreNotFound(java.lang.String)
	 */
	@Override
	public T findIgnoreNotFound(String code) throws MeveoApiException {
		try {
			return find(code);
		} catch (EntityDoesNotExistsException | org.meveo.exceptions.EntityDoesNotExistsException e) {
			return null;
		}
	}

	/**
	 * Export entities matching filters to an XML file
	 *
	 * @param conf Filters
	 * @return the export file
	 */
	public File exportXML(PaginationConfiguration conf) throws IOException {
		return exportEntities(conf, ExportFormat.XML);
	}

	/**
	 * Export entities matching filters to a JSON file
	 *
	 * @param config Filters
	 * @return the export file
	 */
	public File exportJSON(PaginationConfiguration config) throws IOException {
		return exportEntities(config, ExportFormat.JSON);
	}

	/**
	 * Export entities matching filters to a CSV file
	 *
	 * @param config Filters
	 * @return the export file
	 */
	public File exportCSV(PaginationConfiguration config) throws IOException {
		return exportEntities(config, ExportFormat.CSV);
	}

	/**
	 * Export entities matching filters to a give format
	 *
	 * @param conf   Filters
	 * @param format Format of generated file
	 * @return the export file
	 */
	public File exportEntities(PaginationConfiguration conf, ExportFormat format) throws IOException {
		List<E> entities = getPersistenceService().list(conf);

		List<T> dtos = new ArrayList<>();
		for (E entity : entities) {
			dtos.add(toDto(entity));
		}

		return exportDtos(format, dtos);
	}

	public File exportEntities(ExportFormat format, List<E> entities) throws IOException {
		List<T> dtos;

		if (CollectionUtils.isEmpty(entities)) {
			dtos = new ArrayList<>();
		} else {
			dtos = entities.stream().map(this::toDto).collect(Collectors.toList());
		}

		return exportDtos(format, dtos);
	}

	private File exportDtos(ExportFormat format, List<T> dtos) throws IOException {
		if (format == null) {
			throw new IllegalArgumentException("Format must be provided");
		}

		File exportFile = new File("export_" + jpaClass.getSimpleName() + System.currentTimeMillis() + "." + format.getFormat());

		switch (format) {

			case JSON:
				new ObjectMapper().configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
						.enable(SerializationFeature.INDENT_OUTPUT)
						.writeValue(exportFile, dtos);
				break;

			case XML:
				new XmlMapper().configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
						.enable(SerializationFeature.INDENT_OUTPUT)
						.writeValue(exportFile, dtos);
				break;

			case CSV:
				CsvMapper csvMapper = (CsvMapper) new CsvMapper()
						.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
						.enable(SerializationFeature.INDENT_OUTPUT);

				CsvSchema schema = csvMapper.schemaFor(dtoClass).withColumnSeparator(';');
				ObjectWriter myObjectWriter = csvMapper.writer(schema);
				myObjectWriter.writeValue(exportFile, dtos);
				break;
		}

		return exportFile;
	}

	/**
	 * Import a list of entities into the database
	 *
	 * @param entities  Entities to import
	 * @param overwrite Whether we should update existing entities
	 */
	public void importEntities(List<T> entities, boolean overwrite) throws BusinessException, MeveoApiException {
		for (T entity : entities) {
			if (overwrite) {
				createOrUpdate(entity);
			} else if (!exists(entity)) {
				createOrUpdate(entity);
			}
		}
	}

	/**
	 * Import data from an XML file
	 *
	 * @param xml       File to import
	 * @param overwrite Whether we should update existing data
	 */
	public void importXML(InputStream xml, boolean overwrite) throws IOException, BusinessException, MeveoApiException {
		XmlMapper xmlMapper = new XmlMapper();
		xmlMapper.registerModule(new JaxbAnnotationModule());

		List<?> entities = xmlMapper.readValue(xml, List.class);
		List<T> entitiesCasted = new ArrayList<>();
		for (Object entity : entities) {

			entitiesCasted.add(xmlMapper.convertValue(entity, dtoClass));
		}

		importEntities(entitiesCasted, overwrite);
	}

	/**
	 * Import data from a JSON file
	 *
	 * @param json      File to import
	 * @param overwrite Whether we should update existing data
	 */
	public void importJSON(InputStream json, boolean overwrite) throws BusinessException, IOException, MeveoApiException {
		ObjectMapper jsonMapper = new ObjectMapper();
		List<?> entities = jsonMapper.readValue(json, List.class);

		List<T> entitiesCasted = new ArrayList<>();
		for (Object entity : entities) {
			Map<String, Object> map = (LinkedHashMap<String, Object>) entity;
			if (map.containsKey("moduleItems")) {
				List<String> items = (List<String>) map.get("moduleFiles");
				for (String moduleFile : items) {
					for (File file : fileImport) {
						if (moduleFile.endsWith(file.getName())) {
							String filePath = paramBeanFactory.getInstance().getChrootDir(currentUser.getProviderCode()).replace("\\", "") + moduleFile.replace("\\", "/");
							File fileFromModule = new File(filePath);
							if (fileFromModule.isDirectory()) {
								if (!fileFromModule.exists()) {
									fileFromModule.mkdir();
								}
							} else {
								FileInputStream inputStream = new FileInputStream(file);
								copyFile(filePath, inputStream);
							}
						}
					}
				}
			}
			entitiesCasted.add(jsonMapper.convertValue(entity, dtoClass));
		}

	importEntities(entitiesCasted, overwrite);

}
	
	/**
	 * Import data from a CSV file
	 * 
	 * @param csv       File to import
	 * @param overwrite Whether we should update existing data
	 */
	public void importCSV(InputStream csv, boolean overwrite) throws IOException, BusinessException, MeveoApiException {
		CsvMapper csvMapper = new CsvMapper();
		
		CsvSchema schema = csvMapper.schemaFor(dtoClass)
				.withColumnSeparator(';');
		
		MappingIterator<T> reader = csvMapper.readerFor(dtoClass)
				.with(schema)
				.readValues(csv);
		
		importEntities(reader.readAll(), overwrite);
	}

	/**
	 * Import data from a zip
	 *
	 * @param file       File to import
	 * @param overwrite Whether we should update existing data
	 */
	public void importZip(String fileName, InputStream file, boolean overwrite) {
		try {
			FileUtils.unzipFile(fileName, file);
			buildFileList(fileName, overwrite);
		} catch (Exception e) {}
	}

	private void buildFileList(String fileName, boolean overwrite) throws BusinessException, IOException, MeveoApiException {
		try {
			File file = new File(fileName);
			if (fileName.endsWith(".zip")) {
				File[] files = file.listFiles();
				for (File fileFromZip : files) {
					fileImport.add(fileFromZip);
				}
				for (File importFile : files) {
					if (importFile.getName().startsWith("export_") && importFile.getName().endsWith(".json")) {
						FileInputStream inputStream = new FileInputStream(importFile);
						importJSON(inputStream, overwrite);
					}
				}
			}
		} catch (FileNotFoundException e) {}
	}

	private void copyFile(String fileName, InputStream in) {
		try {

			// write the inputStream to a FileOutputStream
			OutputStream out = new FileOutputStream(new File(fileName));

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}

			in.close();
			out.flush();
			out.close();

			log.debug("New file created!");
		} catch (Exception e) {
			log.error("Failed saving file. ", e);
		}
	}

	/**
	 * @see BaseCrudApi#exportCSV(PaginationConfiguration)
	 */
	public File exportCSV(PagingAndFiltering config) throws InvalidParameterException, IOException {
		PaginationConfiguration pagination = toPaginationConfiguration("code", SortOrder.ASCENDING, null, config, jpaClass);
		return exportCSV(pagination);
	}

	/**
	 * @see BaseCrudApi#exportXML(PaginationConfiguration)
	 */
	public File exportXML(PagingAndFiltering config) throws InvalidParameterException, IOException {
		PaginationConfiguration pagination = toPaginationConfiguration("code", SortOrder.ASCENDING, null, config, jpaClass);
		return exportXML(pagination);
	}

	/**
	 * @see BaseCrudApi#exportJSON(PaginationConfiguration)
	 */
	public File exportJSON(PagingAndFiltering config) throws InvalidParameterException, IOException {
		PaginationConfiguration pagination = toPaginationConfiguration("code", SortOrder.ASCENDING, null, config, jpaClass);
		return exportJSON(pagination);
	}
}
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.module.MeveoModuleItemDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.export.ExportFormat;
import org.meveo.commons.utils.FileUtils;
import org.meveo.model.IEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.typereferences.GenericTypeReferences;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.util.NullAwareBeanUtilsBean;
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
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 * 
 * @param <E> Entity class
 * @param <T> Dto class
 */
public abstract class BaseCrudApi<E extends IEntity, T extends BaseEntityDto> extends BaseApi
		implements ApiService<E, T> {

	private Class<T> dtoClass;
	private Class<E> jpaClass;
	private Set<File> fileImport = new HashSet<>();

	public BaseCrudApi(Class<E> jpaClass, Class<T> dtoClass) {
		super();
		this.dtoClass = dtoClass;
		this.jpaClass = jpaClass;
	}

	/**
	 * @return all entities
	 */
	@Transactional
	public List<T> findAll() {

		List<E> entities = getPersistenceService().list();
		return entities == null ? new ArrayList<>() : entities.stream().map(t -> {
			try {
				return toDto(t);

			} catch (MeveoApiException e) {
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	/**
	 * Function used to construct a dto representation of a given JPA entity
	 *
	 * @param entity Entity to convert
	 * @return Entity converted
	 * @throws MeveoApiException
	 */
	public abstract T toDto(E entity) throws MeveoApiException;

	/**
	 * Build a JPA representation from a DTO
	 *
	 * @param dto DTO to convert
	 * @return The JPA entity built from the DTO
	 * @throws MeveoApiException
	 * @throws BusinessException 
	 */
	public E fromDto(T dto) throws MeveoApiException, BusinessException {
		return fromDto(dto, null);
	}

	public E fromDto(T dto, E entity) throws MeveoApiException, BusinessException {

		if (entity == null) {
			try {
				entity = jpaClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new MeveoApiException("Failed to instantiate class " + jpaClass.getName());
			}
		}

		try {
			BeanUtilsBean beanUtilsBean = new NullAwareBeanUtilsBean();
			beanUtilsBean.copyProperties(entity, dto);

		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new MeveoApiException("Unable to copy dto to entity. Make sure that the properties match.");
		}

		return entity;
	}

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
	public boolean exists(T dto) {
		try {
			return findIgnoreNotFound(dto.getCode()) != null;
		} catch (MeveoApiException e) {
			return false;
		}
	}

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
	
	public void remove(T dto) throws MeveoApiException, BusinessException {
		E entity = getPersistenceService().findByCode(dto.getCode());
		if (entity != null) {
			getPersistenceService().remove(entity);
		}
	}

	/**
	 * Export entities matching filters to an XML file
	 *
	 * @param conf Filters
	 * @return the export file
	 */
	public File exportXML(PaginationConfiguration conf) throws MeveoApiException {
		return exportEntities(conf, ExportFormat.XML);
	}

	/**
	 * Export entities matching filters to a JSON file
	 *
	 * @param config Filters
	 * @return the export file
	 */
	public File exportJSON(PaginationConfiguration config) throws MeveoApiException {
		return exportEntities(config, ExportFormat.JSON);
	}

	/**
	 * Export entities matching filters to a CSV file
	 *
	 * @param config Filters
	 * @return the export file
	 */
	public File exportCSV(PaginationConfiguration config) throws MeveoApiException {
		return exportEntities(config, ExportFormat.CSV);
	}

	/**
	 * Export entities matching filters to a give format
	 *
	 * @param conf   Filters
	 * @param format Format of generated file
	 * @return the export file
	 */
	public File exportEntities(PaginationConfiguration conf, ExportFormat format) throws MeveoApiException {
		List<E> entities = getPersistenceService().list(conf);

		List<T> dtos = new ArrayList<>();
		for (E entity : entities) {
			try {
				dtos.add(toDto(entity));
			} catch (MeveoApiException e) {
				throw e;
			}
		}

		try {
			return exportDtos(format, dtos);
		} catch (IOException e) {
			return null;
		}
	}

	public File exportEntities(ExportFormat format, List<E> entities) throws IOException {
		List<T> dtos;

		if (CollectionUtils.isEmpty(entities)) {
			dtos = new ArrayList<>();
		} else {
			dtos = entities.stream().map(t -> {
				try {
					return toDto(t);
				} catch (MeveoApiException e) {
					log.error("Null entity during entity conversion={}", e.getMessage());
					return null;
				}
			}).filter(Objects::nonNull).collect(Collectors.toList());
		}

		return exportDtos(format, dtos);
	}

	public File exportDtos(ExportFormat format, List<T> dtos) throws IOException {
		if (format == null) {
			throw new IllegalArgumentException("Format must be provided");
		}

		File exportFile = new File(
				"export_" + jpaClass.getSimpleName() + System.currentTimeMillis() + "." + format.getFormat());

		switch (format) {

		case JSON:
			new ObjectMapper().configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
					.enable(SerializationFeature.INDENT_OUTPUT).writeValue(exportFile, dtos);
			break;

		case XML:
			new XmlMapper().configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
					.enable(SerializationFeature.INDENT_OUTPUT).writeValue(exportFile, dtos);
			break;

		case CSV:
			CsvMapper csvMapper = (CsvMapper) new CsvMapper().configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
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
	public void importJSON(InputStream json, boolean overwrite)
			throws BusinessException, IOException, MeveoApiException {
		ObjectMapper jsonMapper = new ObjectMapper();
		List<?> entities = jsonMapper.readValue(json, List.class);

		List<T> entitiesCasted = new ArrayList<>();
		for (Object entity : entities) {
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

		CsvSchema schema = csvMapper.schemaFor(dtoClass).withColumnSeparator(';');

		MappingIterator<T> reader = csvMapper.readerFor(dtoClass).with(schema).readValues(csv);

		importEntities(reader.readAll(), overwrite);
	}

	/**
	 * Import data from a zip
	 *
	 * @param fileName  Name of the file
	 * @param file      File to import
	 * @param overwrite Whether we should update existing data
	 */
	public void importZip(String fileName, InputStream file, boolean overwrite) throws EntityDoesNotExistsException {
		Path fileImport = null;

		try {
			fileImport = Files.createTempDirectory(fileName);
			FileUtils.unzipFile(fileImport.toString(), file);
			buildFileList(fileImport.toFile(), overwrite);
		} catch (EntityDoesNotExistsException e) {
			throw new EntityDoesNotExistsException(e.getMessage());
		} catch (Exception e) {
			log.error("Error import zip file {}", fileName, e);
		}

		if (fileImport != null) {
			try {
				org.apache.commons.io.FileUtils.deleteDirectory(fileImport.toFile());
			} catch (IOException e) {
				log.error("Can't delete temp folder {}", fileImport, e);
			}
		}
	}

	private void buildFileList(File file, boolean overwrite) throws BusinessException, IOException, MeveoApiException {
		File[] files = file.listFiles();
		fileImport.clear();

		for (File fileFromZip : files) {
			fileImport.add(fileFromZip);
		}

		for (File importFile : files) {
			if (importFile.getName().endsWith(".json")) {
				FileInputStream inputStream = new FileInputStream(importFile);
				importJSON(inputStream, overwrite);
			}
		}
	}

	/**
	 * @see BaseCrudApi#exportCSV(PaginationConfiguration)
	 */
	public File exportCSV(PagingAndFiltering config) throws MeveoApiException {
		PaginationConfiguration pagination = toPaginationConfiguration("code", SortOrder.ASCENDING, null, config,
				jpaClass);
		return exportCSV(pagination);
	}

	/**
	 * @see BaseCrudApi#exportXML(PaginationConfiguration)
	 */
	public File exportXML(PagingAndFiltering config) throws MeveoApiException {
		PaginationConfiguration pagination = toPaginationConfiguration("code", SortOrder.ASCENDING, null, config,
				jpaClass);
		return exportXML(pagination);
	}

	/**
	 * @see BaseCrudApi#exportJSON(PaginationConfiguration)
	 */
	public File exportJSON(PagingAndFiltering config) throws MeveoApiException {
		PaginationConfiguration pagination = toPaginationConfiguration("code", SortOrder.ASCENDING, null, config,
				jpaClass);
		return exportJSON(pagination);
	}

	public Class<T> getDtoClass() {
		return dtoClass;
	}

	public Set<File> getFileImport() {
		return fileImport;
	}
	
	public List<MeveoModuleItemDto> readModuleItems(File directory, String dtoClassName) {
		List<MeveoModuleItemDto> items = new ArrayList<>();
		for (File entityFile : directory.listFiles()) {
			String entityFileName = entityFile.getName();
			if (entityFileName.endsWith("-schema.json")) {
				continue;
			}

			items.add(readModuleItem(entityFile, dtoClassName));
		}
		return items;
	}
	
	public MeveoModuleItemDto readModuleItem(File entityFile, String dtoClassName) {
		try {
			String fileToString = org.apache.commons.io.FileUtils.readFileToString(entityFile, StandardCharsets.UTF_8);
			Map<String, Object> data = JacksonUtil.fromString(fileToString, GenericTypeReferences.MAP_STRING_OBJECT);
			return new MeveoModuleItemDto(dtoClassName, data);
		} catch (IOException e) {
			log.error("Can't read entityFile", e);
			return null;
		}
	}
	
	public MeveoModuleItemDto parseModuleItem(File entityFile, String directoryName, Set<MeveoModuleItemDto> alreadyParseItems, String gitRepository) {
		ModuleItem item = jpaClass.getAnnotation(ModuleItem.class);
		if (directoryName.equals(item.path())) {
			return this.readModuleItem(entityFile, this.dtoClass.getName());
		}
		return null;
	}
	
	public MeveoModuleItem getExistingItem(File entityFile) {
		return null;
	}
	
    public Map<String, T> filterModuleDtos(List<MeveoModuleItemDto> itemDtos) {
    	Map<String, T> dtos = new HashMap<>();
		itemDtos.stream()
			.filter(dto -> dto.getDtoClassName().equals(dtoClass.getName()))
			.map(dto -> JacksonUtil.convert(dto.getDtoData(), dtoClass))
			.forEach(dto -> dtos.put(dto.getCode(), dto));
		return dtos;
    }
	
}
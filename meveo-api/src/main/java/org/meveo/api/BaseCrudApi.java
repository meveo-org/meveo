package org.meveo.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.model.IEntity;
import org.meveo.service.base.local.IPersistenceService;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

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
    
	public BaseCrudApi(Class<T> dtoClass) {
		super();
		this.dtoClass = dtoClass;
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
    public T findIgnoreNotFound(String code) throws MissingParameterException, InvalidParameterException, MeveoApiException, org.meveo.exceptions.EntityDoesNotExistsException {
        try {
            return find(code);
        } catch (EntityDoesNotExistsException e) {
            return null;
        }
    }
    
    /**
     * Export entities matching filters to an XML file
     * @param conf Filters 
     * @return the export file
     */
	public File exportXML(PaginationConfiguration conf) throws JsonGenerationException, JsonMappingException, IOException, BusinessException {
		return exportEntities(new XmlMapper(), conf, "xml");
	}
	
    /**
     * Export entities matching filters to a JSON file
     * @param config Filters 
     * @return the export file
     */
	public File exportJSON(PaginationConfiguration config) throws JsonGenerationException, JsonMappingException, IOException, BusinessException {
		return exportEntities(new ObjectMapper(), config, "json");
	}
	
    /**
     * Export entities matching filters to a CSV file
     * @param config Filters 
     * @return the export file
     */
	public File exportCSV(PaginationConfiguration config) throws JsonGenerationException, JsonMappingException, IOException, BusinessException {
		return exportEntities(new CsvMapper(), config, "csv");
	}
	
	/**
	 * Export entities matching filters to a give format
	 * 
	 * @param mapper Mapper used to serialize data
	 * @param conf   Filters
	 * @param format Format of generated file
	 * @return the export file
	 */
	public File exportEntities(ObjectMapper mapper, PaginationConfiguration conf, String format) throws JsonGenerationException, JsonMappingException, IOException, BusinessException {
		List<E> entities = getPersistenceService().list(conf);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
		
		List<T> dtos = new ArrayList<>();
		for(E entity : entities) {
			dtos.add(toDto(entity));
		}
		
		Class<? extends IEntity> entityClass = entities.get(0).getClass();
		Class<? extends BaseEntityDto> dtoClass = dtos.get(0).getClass();

		File exportFile = new File("export_" + entityClass.getSimpleName() + System.currentTimeMillis() + "." + format);
		
		if(mapper instanceof CsvMapper) {
			CsvMapper csvMapper = (CsvMapper) mapper;
			CsvSchema schema = csvMapper.schemaFor(dtoClass)
					.withColumnSeparator(';');
			
	        ObjectWriter myObjectWriter = mapper.writer(schema);
	        myObjectWriter.writeValue(exportFile, dtos);
		}else {
			mapper.writeValue(exportFile, dtos);
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
			if(overwrite) {
				createOrUpdate(entity);
			} else if(!exists(entity)) {
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
	public void importXML(InputStream xml, boolean overwrite) throws JsonParseException, JsonMappingException, IOException, BusinessException, MeveoApiException {
		XmlMapper xmlMapper = new XmlMapper();
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
	 * @param xml       File to import
	 * @param overwrite Whether we should update existing data
	 */
	public void importJSON(InputStream json, boolean overwrite) throws BusinessException, JsonParseException, JsonMappingException, IOException, MeveoApiException {
		ObjectMapper jsonMapper = new ObjectMapper();
		List<?> entities = jsonMapper.readValue(json, List.class);
		
		List<T> entitiesCasted = new ArrayList<>();
		for(Object entity : entities) {
			entitiesCasted.add(jsonMapper.convertValue(entity, dtoClass));
		}
		
		importEntities(entitiesCasted, overwrite);
	}
	
	/**
	 * Import data from a CSV file
	 * 
	 * @param xml       File to import
	 * @param overwrite Whether we should update existing data
	 */
	public void importCSV(InputStream csv, boolean overwrite) throws JsonParseException, JsonMappingException, IOException, BusinessException, MeveoApiException {
		CsvMapper csvMapper = new CsvMapper();
		
		CsvSchema schema = csvMapper.schemaFor(dtoClass)
				.withColumnSeparator(';');
		
		MappingIterator<T> reader = csvMapper.readerFor(dtoClass)
				.with(schema)
				.readValues(csv);
		
		importEntities(reader.readAll(), overwrite);
	}
}
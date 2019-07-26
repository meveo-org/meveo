package org.meveo.api;

import java.io.File;
import java.io.IOException;
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
import com.fasterxml.jackson.databind.JsonMappingException;
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
	
	public T toDto(E entity) throws BusinessException {
		throw new BusinessException("toDto not implemented");
	}
	
	public E fromDto(T dto) throws BusinessException {
		throw new BusinessException("fromDto not implemented");
	}
	
	public IPersistenceService<E> getPersistenceService() throws BusinessException{
		throw new BusinessException("getPersistenceService not implemented");
	}

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
    
	public File exportXML(PaginationConfiguration conf) throws JsonGenerationException, JsonMappingException, IOException, BusinessException {
		return exportEntities(new XmlMapper(), conf, "xml");
	}
	
	public File exportJSON(PaginationConfiguration config) throws JsonGenerationException, JsonMappingException, IOException, BusinessException {
		return exportEntities(new ObjectMapper(), config, "json");
	}
	
	public File exportCSV(PaginationConfiguration config) throws JsonGenerationException, JsonMappingException, IOException, BusinessException {
		return exportEntities(new CsvMapper(), config, "csv");
	}
	
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
	
//TODO: Write javadoc
//	public void importXML(InputStream xml, boolean overwrite) throws JsonParseException, JsonMappingException, IOException, BusinessException {
//		XmlMapper xmlMapper = new XmlMapper();
//		TypeReference<List<E>> typeReference = new TypeReference<List<E>>() {};
//		List<E> entities = xmlMapper.readValue(xml, typeReference);
//		importEntities(entities, overwrite);
//	}
//	
//	public void importJSON(InputStream json, boolean overwrite) throws BusinessException, JsonParseException, JsonMappingException, IOException {
//		ObjectMapper jsonMapper = new ObjectMapper();
//		TypeReference<List<E>> typeReference = new TypeReference<List<P>>() {};
//		List<P> entities = jsonMapper.readValue(json, typeReference);
//		importEntities(entities, overwrite);
//	}
//	
//	public void importCSV(InputStream csv, boolean overwrite) throws JsonParseException, JsonMappingException, IOException, BusinessException {
//		CsvMapper csvMapper = new CsvMapper();
//		TypeReference<List<P>> typeReference = new TypeReference<List<P>>() {};
//		List<P> entities = csvMapper.readValue(csv, typeReference);
//		importEntities(entities, overwrite);
//	}
}
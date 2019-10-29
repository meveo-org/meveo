/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.service.custom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.collections4.MapUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.search.sort.SortOrder;
import org.hibernate.SQLQuery;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ValidationException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.jpa.JpaAmpNewTx;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomTableRecord;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.shared.DateUtils;
import org.meveo.model.typereferences.GenericTypeReferences;
import org.meveo.service.base.NativePersistenceService;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.index.ElasticClient;
import org.meveo.service.index.ElasticSearchClassInfo;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @lastModifiedVersion 6.4.0
 */
@Stateless
public class CustomTableService extends NativePersistenceService {

    /**
     * File prefix indicating that imported data should be appended to exiting data
     */
    public static final String FILE_APPEND = "_append";

    @Inject
    private ElasticClient elasticClient;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @EJB
    private CustomTableService customTableService;

    @Inject
    protected ParamBeanFactory paramBeanFactory;
    
    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private CustomFieldsCacheContainerProvider customFieldsCacheContainerProvider;

    @Inject
    private CustomFieldInstanceService customFieldInstanceService;

	/**
	 * Inserts a {@linkplain CustomEntityInstance} into the database.
	 *
	 * @param cei transient {@link CustomEntityInstance}
	 * @return UUID of created row
	 * @deprecated Use {@link CustomTableService#create(CustomEntityTemplate, Map)}
	 *             instead. If you have to use, check that all the values has to be
	 *             stored in SQL
	 */
	@Override
	@Deprecated
	public String create(CustomEntityInstance cei) throws BusinessException {

		String uuid = super.create(cei, true); // Force to return ID as we need it to retrieve data for Elastic Search
												// population
		elasticClient.createOrUpdate(CustomTableRecord.class, uuid, cei, false, true);

		return uuid;
	}

	/**
	 * Inserts an instance of {@linkplain CustomEntityInstance} into the database.
	 *
	 * @param cet {@link CustomEntityTemplate} to insert values to
	 * @param cei transient {@link CustomEntityInstance}
	 * @return uuid of the newly created entity
	 * @throws BusinessException failed creating the entity
	 */
	@SuppressWarnings("deprecation")
	public String create(CustomEntityTemplate cet, CustomEntityInstance cei) throws BusinessException {

		Collection<CustomFieldTemplate> cfts = customFieldsCacheContainerProvider.getCustomFieldTemplates(cet.getAppliesTo()).values();
		return create(cei, true, true, cfts, true);
	}
    
	/**
	 * Inserts an instance of {@linkplain CustomEntityInstance} into the database
	 * using a new transaction.
	 *
	 * @param cet {@link CustomEntityTemplate} to insert values to
	 * @param cei transient {@link CustomEntityInstance}
	 * @throws BusinessException failed creating the entity
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void createInNewTx(CustomEntityTemplate cet, CustomEntityInstance cei) throws BusinessException {

		create(cet, cei);
	}

	/**
	 * Insert multiple values into table
	 *
	 * @param tableName Table name to insert values to
	 * @param ceis transient list of {@link CustomEntityInstance}
	 * @throws BusinessException General exception
	 * @deprecated prefer using
	 *             {@link CustomTableService#create(CustomEntityTemplate, List)}
	 *             instead for value filtering.
	 */
	@Override
	@Deprecated
	public void create(String tableName, List<CustomEntityInstance> ceis) throws BusinessException {

		for (CustomEntityInstance cei : ceis) {
			String uuid = super.create(cei, true); // Force to return ID as we need it to retrieve data for Elastic Search
																// population
			elasticClient.createOrUpdate(CustomTableRecord.class, uuid, cei, false, false);
		}

		elasticClient.flushChanges();
	}

	/**
	 * Insert multiple {@linkplain CustomEntityInstance} into a database.
	 *
	 * @param cet {@link CustomEntityTemplate} to insert values to
	 * @param ceis list of transient {@link CustomEntityInstance}
	 * @throws BusinessException General exception
	 */
	@SuppressWarnings("deprecation")
	public void create(CustomEntityTemplate cet, List<CustomEntityInstance> ceis) throws BusinessException {

		for (CustomEntityInstance cei : ceis) {
			create(cet, cei);
		}
	}

	/**
	 * nsert multiple {@linkplain CustomEntityInstance} into a database using a new transaction.
	 *
	 * @param cet {@link CustomEntityTemplate} to insert values to
	 * @param updateES if true, update the records in the ElasticSearch cache
	 * @param values map of <String, Object>
	 * @throws BusinessException
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void createInNewTx(CustomEntityTemplate cet, boolean updateES, List<Map<String, Object>> values) throws BusinessException {

		List<CustomEntityInstance> ceis = new ArrayList<>();

		for (Map<String, Object> value : values) {
			CustomEntityInstance cei = new CustomEntityInstance();
			cei.setCetCode(cet.getCode());
			customFieldInstanceService.setCfValues(cei, cei.getCetCode(), value);
			ceis.add(cei);
		}

		createInNewTx(cet, ceis, updateES);
	}

    /**
     * Insert multiple values into table with optionally not updating ES. Will execute in a new transaction
     * 
     * @param cet CustomEntityTemplate to insert values to
     * @param ceis list of transient {@link CustomEntityInstance} to insert
     * @param updateES should Elastic search be updated during record creation. If false, ES population must be done outside this call
     * @throws BusinessException General exception
     */
	@JpaAmpNewTx
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void createInNewTx(CustomEntityTemplate cet, List<CustomEntityInstance> ceis, boolean updateES) throws BusinessException {

		// Insert record to db, with ID returned, but flush to ES after the values are
		// processed
		if (updateES) {
			create(cet, ceis);

		} else {
			final String tablename = SQLStorageConfiguration.getDbTablename(cet);
			super.create(tablename, ceis);
		}
	}

	@Override
	@Deprecated
	/**
	 * Updates a {@linkplain CustomEntityInstance}. This object must contain a cf value with code uuid.
	 *
	 * @param cei transient {@link CustomEntityInstance}
	 * @throws BusinessException failed to update the entity
	 */
	public void update(CustomEntityInstance cei) throws BusinessException {

		super.update(cei);
		elasticClient.createOrUpdate(CustomTableRecord.class, cei.getCfValuesAsValues().get(NativePersistenceService.FIELD_ID), cei, false, true);
	}

	/**
	 * Updates a {@linkplain CustomEntityInstance}. This object must contain a cf value with code uuid.
	 *
	 * @param cet
	 * @param cei transient {@link CustomEntityInstance}
	 * @throws BusinessException failed to update the entity
	 */
    @SuppressWarnings("deprecation")
    public void update(CustomEntityTemplate cet, CustomEntityInstance cei) throws BusinessException {

    	Collection<CustomFieldTemplate> cfts = customFieldsCacheContainerProvider.getCustomFieldTemplates(cet.getAppliesTo()).values();
		super.update(cei, true, cfts, false);
    }

    /**
     * Update multiple {@linkplain CustomEntityInstance} in a table. Record is identified by an "uuid" field value.
     * 
     * @param cet CustomEntityTemplate to update values
     * @param values list of {@link CustomEntityInstance}. Must contain a cf field with code 'uuid'
     * @throws BusinessException General exception
     */
	public void update(CustomEntityTemplate cet, List<CustomEntityInstance> ceis) throws BusinessException {

		for (CustomEntityInstance cei : ceis) {
			update(cet, cei);
		}
		elasticClient.flushChanges();
	}

    @Override
    public void updateValue(String tableName, String uuid, String fieldName, Object value) throws BusinessException {
        super.updateValue(tableName, uuid, fieldName, value);
        elasticClient.createOrUpdate(CustomTableRecord.class, tableName, uuid, MapUtils.putAll(new HashMap<>(), new Object[] { fieldName, value }), true, true);
    }

    @Override
    public void disable(String tableName, String uuid) throws BusinessException {
        super.disable(tableName, uuid);
        elasticClient.remove(CustomTableRecord.class, tableName, uuid, true);
    }

    @Override
    public void disable(String tableName, Set<String> ids) throws BusinessException {
        super.disable(tableName, ids);
        elasticClient.remove(CustomTableRecord.class, tableName, ids, true);
    }

    @Override
    public void enable(String tableName, String uuid) throws BusinessException {
        super.enable(tableName, uuid);
        Map<String, Object> values = findById(tableName, uuid);
        elasticClient.createOrUpdate(CustomTableRecord.class, tableName, uuid, values, false, true);
    }

    @Override
    public void enable(String tableName, Set<String> ids) throws BusinessException {
        super.enable(tableName, ids);
        for (String uuid : ids) {
            Map<String, Object> values = findById(tableName, uuid);
            elasticClient.createOrUpdate(CustomTableRecord.class, tableName, uuid, values, false, false);
        }
        elasticClient.flushChanges();
    }

    @Override
    public void remove(String tableName, String uuid) throws BusinessException {
        super.remove(tableName, uuid);
        elasticClient.remove(CustomTableRecord.class, tableName, uuid, true);
    }

    @Override
    public void remove(String tableName, Set<String> ids) throws BusinessException {
        super.remove(tableName, ids);
//        elasticClient.remove(CustomTableRecord.class, tableName, ids, true); FIXME: Update ES to use UUID instead of ID
    }

    @Override
    public void remove(String tableName) throws BusinessException {
        super.remove(tableName);
        elasticClient.remove(CustomTableRecord.class, tableName);
    }

    /**
     * Export data into a file into exports directory. Filename is in the following format: &lt;db table name&gt;_id_&lt;formated date&gt;.csv
     * 
     * @param customEntityTemplate Custom table definition
     * @param config Pagination and search criteria
     * @return A future with a file name where the data will be exported to or an exception occurred
     */
    @Asynchronous
    @SuppressWarnings({"unchecked", "deprecation"})
    public Future<DataImportExportStatistics> exportData(CustomEntityTemplate customEntityTemplate, PaginationConfiguration config) {

        try {
            final String dbTablename = SQLStorageConfiguration.getDbTablename(customEntityTemplate);
            QueryBuilder queryBuilder = getQuery(dbTablename, config);

            SQLQuery query = queryBuilder.getNativeQuery(getEntityManager(), true);

            int firstRow = 0;
            int nrItemsFound;

            ParamBean parambean = paramBeanFactory.getInstance();
            String providerRoot = parambean.getChrootDir(currentUser.getProviderCode());
            String exportDir = providerRoot + File.separator + "exports" + File.separator;

            File exportsDirFile = new File(exportDir);

            File exportFile = new File(exportDir + dbTablename + DateUtils.formatDateWithPattern(new Date(), "_yyyy-MM-dd_HH-mm-ss") + ".csv");

            if (!exportsDirFile.exists()) {
                exportsDirFile.mkdirs();
            }

            Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(customEntityTemplate.getAppliesTo());

            if (cfts == null || cfts.isEmpty()) {
                throw new ValidationException("No fields are defined for custom table " + dbTablename + "customTable.noFields");
            }

            List<CustomFieldTemplate> fields = new ArrayList<>(cfts.values());

            fields.sort((cft1, cft2) -> {
                int pos1 = cft1.getGUIFieldPosition();
                int pos2 = cft2.getGUIFieldPosition();

                return pos1 - pos2;
            });

            ObjectWriter oWriter = getCSVWriter(fields);

            try (FileWriter fileWriter = new FileWriter(exportFile)) {

                SequenceWriter sWriter = oWriter.writeValues(fileWriter);

                do {
                    queryBuilder.applyPagination(query, firstRow, 500);
                    List<Map<String, Object>> values = query.list();

                    /* Fetch entity references */
                    Map<Integer, Object[]> fetchedEntityReferences = new HashMap<>();			// Map used to replace data with as key the index of the value to replace and as value the actual (key, value) pair to replace
                    Map<String, Map<String, String>> entityReferencesCache = new HashMap<>();	// Cache used to avoid fetching multiple time the same data
                    values.forEach(map -> map.forEach((key, value) -> fields.stream()
                            .filter(f -> f.getDbFieldname().equals(key)).findFirst()
                            .ifPresent(customFieldTemplate -> {
                                if(customFieldTemplate.getFieldType() == CustomFieldTypeEnum.ENTITY) {
                                    String referencedEntity = entityReferencesCache.computeIfAbsent(key, k -> new HashMap<>())
                                        .computeIfAbsent(
                                            customFieldTemplate.getDbFieldname(),
                                            k -> {
                                                log.info("Fetching {} with uuid {}", customFieldTemplate.getCode(), value);
                                                Map<String, Object> entityRefValues = findById(k, (String) value);
                                                entityRefValues.remove("uuid");				// We don't want to save the uuid
                                                return JacksonUtil.toString(entityRefValues);
                                            }
                                    );
                                    fetchedEntityReferences.put(values.indexOf(map), new Object[]{key, referencedEntity});
                                }
                            })));
                    fetchedEntityReferences.forEach((index, array) -> values.get(index).put((String) array[0], array[1])); 
                    
                    nrItemsFound = values.size();
                    firstRow = firstRow + 500;

                    sWriter.writeAll(values);

                } while (nrItemsFound == 500);

            } catch (IOException e) {
                log.error("Failed to write {} table data to a file {}", dbTablename, exportFile.getAbsolutePath(), e);
                throw new BusinessException(e);
            }

            return new AsyncResult<>(new DataImportExportStatistics(exportFile.getAbsolutePath().substring(providerRoot.length())));

        } catch (Exception e) {
            return new AsyncResult<>(new DataImportExportStatistics(e));
        }
    }

    /**
     * Import data into custom table
     * 
     * @param customEntityTemplate Custom table definition
     * @param file Data file
     * @param append True if data should be appended to the existing data
     * @return Number of records imported
     * @throws BusinessException General business exception
     */
//    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public int importData(CustomEntityTemplate customEntityTemplate, File file, boolean append) throws BusinessException {

        try (FileInputStream inputStream = new FileInputStream(file)) {
            return importData(customEntityTemplate, inputStream, append);

        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }

    /**
     * Import data into custom table in asynchronous mode
     * 
     * @param customEntityTemplate Custom table definition
     * @param inputStream Data stream
     * @param append True if data should be appended to the existing data
     * @return A future with a number of records imported or exception occurred
     */
    @Asynchronous
//    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Future<DataImportExportStatistics> importDataAsync(CustomEntityTemplate customEntityTemplate, InputStream inputStream, boolean append) {

        try {
            int itemsImported = importData(customEntityTemplate, inputStream, append);
            return new AsyncResult<>(new DataImportExportStatistics(itemsImported));

        } catch (Exception e) {
        	log.error("Error importing data", e);
            return new AsyncResult<>(new DataImportExportStatistics(e));
        }
    }

    /**
     * Import data into custom table
     * 
     * @param cet Custom table definition
     * @param inputStream Data stream
     * @param append True if data should be appended to the existing data. If false, data will be added in batch. One by one otherwise.
     * @return Number of records imported
     * @throws BusinessException General business exception
     */
//    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public int importData(CustomEntityTemplate cet, InputStream inputStream, boolean append) throws BusinessException {

        final String dbTableName = SQLStorageConfiguration.getDbTablename(cet);
        // Custom table fields. Fields will be sorted by their GUI 'field' position.
        Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());
        if (cfts == null || cfts.isEmpty()) {
            throw new ValidationException("No fields are defined for custom table " + dbTableName, "customTable.noFields");
        }
        List<CustomFieldTemplate> fields = new ArrayList<>(cfts.values());

        fields.sort((cft1, cft2) -> {
            int pos1 = cft1.getGUIFieldPosition();
            int pos2 = cft2.getGUIFieldPosition();

            return pos1 - pos2;
        });

        int importedLines = 0;
        int importedLinesTotal = 0;
        List<Map<String, Object>> values = new ArrayList<>();

        ObjectReader oReader = getCSVReader(fields);

        try (Reader reader = new InputStreamReader(inputStream)) {
        	
    		Map<String, Map<String, String>> entityReferencesCache = new HashMap<>(); // Cache used to avoid fetching multiple time the same data

            MappingIterator<Map<String, Object>> mappingIterator = oReader.readValues(reader);

            while (mappingIterator.hasNext()) {
                Map<String, Object> lineValues = mappingIterator.next();
            	lineValues.remove("uuid");
                
                if(append) {
                	lineValues = convertValue(lineValues, cfts, true, null);
                	replaceEntityreferences(fields, entityReferencesCache, lineValues);
                	String uuid = findIdByValues(dbTableName, lineValues);
                	if(uuid == null) {
                		final String tablename = SQLStorageConfiguration.getDbTablename(cet);
                		super.createInNewTx(tablename, lineValues);
                		importedLines++;
                        importedLinesTotal++;
                	}
                } else {
	            	// Save to DB every 500 records
	                if (importedLines >= 500) {
	
	                    saveBatch(cfts, fields, cet.getCode(), values, entityReferencesCache);
	
	                    values.clear();
	                    importedLines = 0;
	                }
	                importedLines++;
	                importedLinesTotal++;
	            	values.add(lineValues);
                }
                
                
                if (importedLinesTotal % 30000 == 0) {
                    log.trace("Imported {} lines to {} table", importedLinesTotal, dbTableName);
                }
                
            }

            if(!append) {
                // Save remaining records
	            saveBatch(cfts, fields, cet.getCode(), values, entityReferencesCache);
            }

            // Re-populate ES index
            elasticClient.populateAll(currentUser, CustomTableRecord.class, cet.getCode());

            log.info("Imported {} lines to {} table", importedLinesTotal, dbTableName);

        } catch (RuntimeJsonMappingException e) {
            throw new ValidationException("Invalid file format", "message.upload.fail.invalidFormat", e);

        } catch (IOException e) {
            throw new BusinessException(e);
        }

        return importedLinesTotal;
    }

	private void saveBatch(Map<String, CustomFieldTemplate> cfts, List<CustomFieldTemplate> fields, String cetCode, List<Map<String, Object>> values,
			Map<String, Map<String, String>> entityReferencesCache) throws BusinessException {

		List<CustomEntityInstance> ceis = new ArrayList<>();
		for (Map<String, Object> value : values) {
			CustomEntityInstance cei = new CustomEntityInstance();
			cei.setCetCode(cetCode);
			customFieldInstanceService.setCfValues(cei, cei.getCetCode(), value);
			ceis.add(cei);
		}

		saveBatch(cfts, fields, ceis, entityReferencesCache);
	}

	/**
	 * Inserts a list of {@linkplain CustomEntityInstance} into the database in batch.
	 *
	 * @param cfts map of {@link CustomFieldTemplate}
	 * @param fields list of {@link CustomFieldTemplate}
	 * @param ceis list of transient {@link CustomEntityInstance}
	 * @param entityReferencesCache
	 * @throws BusinessException batch saving failed
	 */
	private void saveBatch(Map<String, CustomFieldTemplate> cfts, List<CustomFieldTemplate> fields,  List<CustomEntityInstance> ceis, Map<String, Map<String, String>> entityReferencesCache) throws BusinessException {
		
		if(ceis == null || ceis.isEmpty()) {
			return;
		}

		List<Map<String, Object>> values = ceis.stream().map(CustomEntityInstance::getCfValuesAsValues).collect(Collectors.toList());
		values = convertValues(values, cfts, false);
		values = replaceEntityReferences(fields, values, entityReferencesCache);
        final CustomEntityTemplate cet = customEntityTemplateService.findByCodeOrDbTablename(ceis.get(0).getTableName());
        customTableService.createInNewTx(cet, false, values);
	}

	private List<Map<String, Object>> replaceEntityReferences(List<CustomFieldTemplate> fields, List<Map<String, Object>> oldvalues, Map<String, Map<String, String>> entityReferencesCache) throws BusinessException {
		List<Map<String, Object>> values = new ArrayList<>(oldvalues);
		/* Create or retrieve entity references */
		for (Map<String, Object> map : values) {
		    replaceEntityreferences(fields, entityReferencesCache, map);
		}
		return values;
	}

	private void replaceEntityreferences(List<CustomFieldTemplate> fields, Map<String, Map<String, String>> entityReferencesCache, Map<String, Object> entityRefValueMap) throws BusinessException {
		final HashMap<String, Object> iterationMap = new HashMap<>(entityRefValueMap);
		for (Entry<String, Object> entry : iterationMap.entrySet()) {
		    String key = entry.getKey();
		    Object value = entry.getValue();
		    final Optional<CustomFieldTemplate> templateOptional = fields.stream().filter(f -> f.getDbFieldname().equals(key)).findFirst();
		    if (templateOptional.isPresent() && templateOptional.get().getFieldType() == CustomFieldTypeEnum.ENTITY) {
		    	String entityRefTableName = SQLStorageConfiguration.getCetDbTablename(templateOptional.get().getEntityClazzCetCode());
		        // Try to retrieve record first
		        String uuid = entityReferencesCache.computeIfAbsent(key, k -> new HashMap<>())
		                .computeIfAbsent(
		                        (String) value,
		                        serializedValues -> {
		                            Map<String, Object> entityRefValues = JacksonUtil.fromString(serializedValues, GenericTypeReferences.MAP_STRING_OBJECT);
		                            return findIdByValues(entityRefTableName, entityRefValues);
		                        }
		                );

		        // If record is not found, create it
				if (uuid == null) {
					Map<String, Object> entityRefValues = JacksonUtil.fromString((String) value, GenericTypeReferences.MAP_STRING_OBJECT);
					log.info("Creating missing entity reference {}", entityRefValues);
					CustomEntityTemplate cet = customFieldsCacheContainerProvider.getCustomEntityTemplate(templateOptional.get().getEntityClazzCetCode());
					CustomEntityInstance cei = new CustomEntityInstance();
					cei.setCetCode(cet.getCode());
					customFieldInstanceService.setCfValues(cei, cei.getCetCode(), entityRefValues);
					uuid = create(cet, cei);
				}

		        entityRefValueMap.put(key, uuid);
		    }
		}
	}

	public int importData(CustomModelObject customModelObject, List<CustomEntityInstance> ceis, boolean append) throws BusinessException {
		if (ceis == null || ceis.isEmpty()) {
			return 0;
		}

		List<Map<String, Object>> values = ceis.stream().map(CustomEntityInstance::getCfValuesAsValues).collect(Collectors.toList());

		return importData(ceis.get(0).getTableName(), customModelObject, values, append);
	}

    /**
     * Import data into custom table
     * 
     * @param customModelObject Custom table definition
     * @param values A list of records to import. Each record is a map of values with field name as a map key and field value as a value.
     * @param append True if data should be appended to the existing data
     * @return Number of records imported
     * @throws BusinessException General business exception
     */
    public int importData(String tableName, CustomModelObject customModelObject, List<Map<String, Object>> values, boolean append) throws BusinessException {

        // Custom table fields. Fields will be sorted by their GUI 'field' position.
        Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(customModelObject.getAppliesTo());
        List<CustomFieldTemplate> fields = new ArrayList<>(cfts.values());

        fields.sort((cft1, cft2) -> {
            int pos1 = cft1.getGUIFieldPosition();
            int pos2 = cft2.getGUIFieldPosition();

            return pos1 - pos2;
        });

        int importedLines = 0;
        int importedLinesTotal = 0;
        List<Map<String, Object>> valuesPartial = new ArrayList<>();

        // Delete current data first if in override mode
        if (!append) {
            customTableService.remove(tableName);
        }

        // By default will update ES immediately. If more than 100 records are being updated, ES will be updated in batch way - reconstructed from a table
        boolean updateESImediately = append;
        if (values.size() > 100) {
            updateESImediately = false;
        }

        try {

            CustomEntityTemplate cet = customEntityTemplateService.findByCodeOrDbTablename(tableName);

            for (Map<String, Object> value : values) {

                // Save to DB every 1000 records
                if (importedLines >= 1000) {

                    valuesPartial = convertValues(valuesPartial, cfts, false);
                    customTableService.createInNewTx(cet, updateESImediately, valuesPartial);

                    valuesPartial.clear();
                    importedLines = 0;
                }

                valuesPartial.add(value);

                importedLines++;
                importedLinesTotal++;
            }

            // Save to DB remaining records
            valuesPartial = convertValues(valuesPartial, cfts, false);
            customTableService.createInNewTx(cet, updateESImediately, valuesPartial);

            // Repopulate ES index
            if (!updateESImediately) {
                elasticClient.populateAll(currentUser, CustomTableRecord.class, customModelObject.getCode());
            }

        } catch (Exception e) {
            throw new BusinessException(e);
        }

        return importedLinesTotal;
    }

    /**
     * Get the CSV file reader. Schema is created from field's dbFieldname values.
     * 
     * @param fields Custom table fields definition
     * @return The CSV file reader
     */
    private ObjectReader getCSVReader(Collection<CustomFieldTemplate> fields) {
        CsvSchema.Builder builder = CsvSchema.builder();

        builder.addColumn(NativePersistenceService.FIELD_ID, ColumnType.NUMBER);

        for (CustomFieldTemplate cft : fields) {
            builder.addColumn(cft.getDbFieldname(),
                cft.getFieldType() == CustomFieldTypeEnum.LONG || cft.getFieldType() == CustomFieldTypeEnum.DOUBLE ? ColumnType.NUMBER : ColumnType.STRING);
        }

        CsvSchema schema = builder.setUseHeader(true).setStrictHeaders(true).setReorderColumns(true).build();
        CsvMapper mapper = new CsvMapper();
        return mapper.readerFor(Map.class).with(schema);
    }

    /**
     * Get the CSV file writer. Schema is created from field's dbFieldname values.
     * 
     * @param fields Custom table fields definition
     * @return The CSV file reader
     */
    private ObjectWriter getCSVWriter(Collection<CustomFieldTemplate> fields) {
        CsvSchema.Builder builder = CsvSchema.builder();

        builder.addColumn(NativePersistenceService.FIELD_ID, ColumnType.NUMBER);

        for (CustomFieldTemplate cft : fields) {
            builder.addColumn(cft.getDbFieldname(),
                cft.getFieldType() == CustomFieldTypeEnum.LONG || cft.getFieldType() == CustomFieldTypeEnum.DOUBLE ? ColumnType.NUMBER : ColumnType.STRING);
        }

        CsvSchema schema = builder.setUseHeader(true).build();
        CsvMapper mapper = new CsvMapper();

        return mapper.writerFor(Map.class).with(schema).with(new SimpleDateFormat(ParamBean.getInstance().getDateTimeFormat(appProvider.getCode())))
            .with(Feature.WRITE_BIGDECIMAL_AS_PLAIN);
    }
    
    /**
     * Execute a search on given fields for given query values. See ElasticClient.search() for a query format.
     *
     * @param cetCodeOrTablename Custom entity template code, or custom table name to query
     * @param queryValues Fields and values to match
     * @param from Pagination - starting record. Defaults to 0.
     * @param size Pagination - number of records per page. Defaults to ElasticClient.DEFAULT_SEARCH_PAGE_SIZE.
     * @param sortFields - Fields to sort by. If omitted, will sort by score. If search query contains a 'closestMatch' expression, sortFields and sortOrder will be overwritten
     *        with a corresponding field and descending order.
     * @param sortOrders Sorting orders
     * @param returnFields Return only certain fields - see Elastic Search documentation for details
     * @return Search result
     * @throws BusinessException General business exception
     */
    public List<Map<String, Object>> search(String cetCodeOrTablename, Map<String, Object> queryValues, Integer from, Integer size, String[] sortFields, SortOrder[] sortOrders,
            String[] returnFields) throws BusinessException {

        ElasticSearchClassInfo classInfo = new ElasticSearchClassInfo(CustomTableRecord.class, cetCodeOrTablename);
        SearchResponse searchResult = elasticClient.search(queryValues, from, size, sortFields, sortOrders, returnFields, Collections.singletonList(classInfo));

        if (searchResult == null) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> responseValues = new ArrayList<>();

        searchResult.getHits().forEach(hit -> {
            Map<String, Object> values = new HashMap<>();
            responseValues.add(values);

            if (hit.getFields() != null && !hit.getFields().values().isEmpty()) {
                for (DocumentField field : hit.getFields().values()) {
                    if (field.getValues() != null) {
                        if (field.getValues().size() > 1) {
                            values.put(field.getName(), field.getValues());
                        } else {
                            values.put(field.getName(), field.getValue());
                        }
                    }
                }

            } else if (hit.getSourceAsMap() != null) {
                values.putAll(hit.getSourceAsMap());
            }
        });

        // log.debug("AKK ES search result values are {}", responseValues);
        return responseValues;
    }

    /**
     * Get field value of the first record matching search criteria
     * 
     * @param cetCodeOrTablename Custom entity template code, or custom table name to query
     * @param fieldToReturn Field value to return
     * @param queryValues Search criteria with condition/field name as a key and field value as a value. See ElasticClient.search() for a query format.
     * @return A field value
     * @throws BusinessException General exception
     */
    public Object getValue(String cetCodeOrTablename, String fieldToReturn, Map<String, Object> queryValues) throws BusinessException {

        Map<String, Object> values = new HashMap<>(queryValues);

        List<Map<String, Object>> results = search(cetCodeOrTablename, values, 0, 1, new String[] { FIELD_ID }, new SortOrder[] { SortOrder.DESC }, new String[] { fieldToReturn });

        if (results == null || results.isEmpty()) {
            return null;
        } else {
            return results.get(0).get(fieldToReturn);
        }
    }

    /**
     * Get field value of the first record matching search criteria for a given date. Applicable to custom tables that contain 'valid_from' and 'valid_to' fields
     * 
     * @param cetCodeOrTablename Custom entity template code, or custom table name to query
     * @param fieldToReturn Field value to return
     * @param date Record validity date, as expressed by 'valid_from' and 'valid_to' fields, to match
     * @param queryValues Search criteria with condition/field name as a key and field value as a value. See ElasticClient.search() for a query format.
     * @return A field value
     * @throws BusinessException General exception
     */
    public Object getValue(String cetCodeOrTablename, String fieldToReturn, Date date, Map<String, Object> queryValues) throws BusinessException {

        Map<String, Object> values = new HashMap<>(queryValues);
        values.put("minmaxRange valid_from valid_to", date);

        List<Map<String, Object>> results = search(cetCodeOrTablename, values, 0, 1, new String[] { FIELD_VALID_PRIORITY, FIELD_VALID_FROM, FIELD_ID },
            new SortOrder[] { SortOrder.DESC, SortOrder.DESC, SortOrder.DESC }, new String[] { fieldToReturn });

        if (results == null || results.isEmpty()) {
            return null;
        } else {
            return results.get(0).get(fieldToReturn);
        }
    }

    /**
     * Get field values of the first record matching search criteria
     * 
     * @param cetCodeOrTablename Custom entity template code, or custom table name to query
     * @param fieldsToReturn Field values to return. Optional. If not provided all fields will be returned.
     * @param queryValues Search criteria with condition/field name as a key and field value as a value. See ElasticClient.search() for a query format.
     * @return A map of values with field name as a key and field value as a value. Note field value is always of String data type.
     * @throws BusinessException General exception
     */
    public Map<String, Object> getValues(String cetCodeOrTablename, String[] fieldsToReturn, Map<String, Object> queryValues) throws BusinessException {

        Map<String, Object> values = new HashMap<>(queryValues);

        List<Map<String, Object>> results = search(cetCodeOrTablename, values, 0, 1, new String[] { FIELD_ID }, new SortOrder[] { SortOrder.DESC }, fieldsToReturn);

        if (results == null || results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    /**
     * Get field values of the first record matching search criteria for a given date. Applicable to custom tables that contain 'valid_from' and 'valid_to' fields
     * 
     * @param cetCodeOrTablename Custom entity template code, or custom table name to query
     * @param fieldsToReturn Field values to return. Optional. If not provided all fields will be returned.
     * @param date Record validity date, as expressed by 'valid_from' and 'valid_to' fields, to match
     * @param queryValues Search criteria with condition/field name as a key and field value as a value. See ElasticClient.search() for a query format.
     * @return A map of values with field name as a key and field value as a value. Note field value is always of String data type.
     * @throws BusinessException General exception
     */
    public Map<String, Object> getValues(String cetCodeOrTablename, String[] fieldsToReturn, Date date, Map<String, Object> queryValues) throws BusinessException {

        Map<String, Object> values = new HashMap<>(queryValues);
        values.put("minmaxRange valid_from valid_to", date);

        List<Map<String, Object>> results = search(cetCodeOrTablename, values, 0, 1, new String[] { FIELD_VALID_PRIORITY, FIELD_VALID_FROM, FIELD_ID },
            new SortOrder[] { SortOrder.DESC, SortOrder.DESC, SortOrder.DESC }, fieldsToReturn);

        if (results == null || results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

	/**
	 * Convert values to a data type matching field definition. Cannot be converted
	 * to CEI as map is filtered per key not per CEI.
	 *
	 * @param values      A map of values with field name of customFieldTemplate
	 *                    code as a key and field value as a value
	 * @param fields      Field definitions with field name or field code as a key
	 *                    and data class as a value
	 * @param discardNull If True, null values will be discarded
	 * @return Converted values with db field name as a key and field value as
	 *         value.
	 */
    @SuppressWarnings("rawtypes")
    public List<Map<String, Object>> convertValues(List<Map<String, Object>> values, Map<String, CustomFieldTemplate> fields, boolean discardNull) throws ValidationException {

        if (values == null) {
            return null;
        }
        List<Map<String, Object>> convertedValues = new LinkedList<>();

        String[] datePatterns = new String[] { DateUtils.DATE_TIME_PATTERN, paramBean.getDateTimeFormat(), DateUtils.DATE_PATTERN, paramBean.getDateFormat() };

        for (Map<String, Object> value : values) {
            convertedValues.add(convertValue(value, fields, discardNull, datePatterns));
        }

        return convertedValues;
    }

	/**
	 * Convert values to a data type matching field definition. Cannot be converted
	 * to CEI as map is filtered per key not per CEI.
	 *
	 * @param values       A map of values with customFieldTemplate code or db field
	 *                     name as a key and field value as a value.
	 * @param fields       Field definitions
	 * @param discardNull  If True, null values will be discarded
	 * @param datePatterns Optional. Date patterns to apply to a date type field.
	 *                     Conversion is attempted in that order until a valid date
	 *                     is matched.If no values are provided, a standard date and
	 *                     time and then date only patterns will be applied.
	 * @return Converted values with db field name as a key and field value as
	 *         value.
	 */
    @SuppressWarnings("rawtypes")
    public Map<String, Object> convertValue(Map<String, Object> values, Map<String, CustomFieldTemplate> fields, boolean discardNull, String[] datePatterns)
            throws ValidationException {

        if (values == null) {
            return null;
        }


        Map<String, Object> valuesConverted = new HashMap<>();

        // Handle ID field
        Object uuid = values.get(FIELD_ID);
        if (uuid != null) {
            valuesConverted.put(FIELD_ID, castValue(uuid, Long.class, false, datePatterns));
        }

        // Convert field based on data type
        if (fields != null) {
            for (Entry<String, Object> valueEntry : values.entrySet()) {

                String key = valueEntry.getKey();
                if (key.equals(FIELD_ID)) {
                    continue; // Was handled before already
                }
                if (valueEntry.getValue() == null && !discardNull) {
                    valuesConverted.put(key, null);

                } else if (valueEntry.getValue() != null) {

                    String[] fieldInfo = key.split(" ");
                    // String condition = fieldInfo.length == 1 ? null : fieldInfo[0];
                    String fieldName = fieldInfo.length == 1 ? fieldInfo[0] : fieldInfo[1]; // field name here can be a db field name or a custom field code

                    Optional<CustomFieldTemplate> customFieldTemplateOpt = fields.values()
                    		.stream()
                    		.filter(f -> f.getDbFieldname().equals(fieldName))
                    		.findFirst();

                    if(!customFieldTemplateOpt.isPresent()){
                        throw new ValidationException("No custom field template for " + fieldName + " was found");
                    }

                    CustomFieldTemplate customFieldTemplate = customFieldTemplateOpt.get();
                    
					final CustomFieldTypeEnum fieldType = customFieldTemplate
                    		.getFieldType();

                    Class dataClass = fieldType.getDataClass();
                    if (dataClass == null) {
                        throw new ValidationException("No field definition " + fieldName + " was found");
                    }

                    boolean isList = customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.LIST;

                    if(isList && fieldType.isStoredSerializedList()) {
                    	isList = false;
                    }

                    Object value = castValue(valueEntry.getValue(), dataClass, isList, datePatterns);

                    // Replace cft code with db field name if needed
                    String dbFieldname = CustomFieldTemplate.getDbFieldname(fieldName);
                    if (!fieldName.equals(dbFieldname)) {
                        key = key.replaceAll(fieldName, dbFieldname);
                    }
                    valuesConverted.put(key, value);
                }
            }

        }
        return valuesConverted;
    }

    public Map<String, Object> findById(CustomEntityTemplate cet, String uuid) throws EntityDoesNotExistsException {
        return findById(cet, uuid, null);
    }

    public List<Map<String, Object>> list(CustomEntityTemplate cet, PaginationConfiguration config) {
        PaginationConfiguration paginationConfiguration = new PaginationConfiguration(config);

        // Only use SQL filters
        if (config.getFilters() != null) {
            final Map<String, Object> sqlFilters = config.getFilters().entrySet().stream()
                    .filter(stringObjectEntry -> sqlCftFilter(cet, stringObjectEntry.getKey()))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            paginationConfiguration.setFilters(sqlFilters);
        }

        // Only fetch SQL fields
        if (config.getFetchFields() != null) {
            List<String> sqlFetchFields = config.getFetchFields().stream()
                    .filter(s -> sqlCftFilter(cet, s))
                    .collect(Collectors.toList());
            paginationConfiguration.setFetchFields(sqlFetchFields);
        }

        final List<Map<String, Object>> data = super.list(SQLStorageConfiguration.getDbTablename(cet), paginationConfiguration);
        return convertData(data, cet);
    }

	/**
	 * Retrieves and convert data from database table
	 *
	 * @param cet          Template of the data
	 * @param uuid         UUID of the row
	 * @param selectFields Fields to retrieve. Will retrieve all fields if null or empty
	 * @return the converted row data
	 */
	@SuppressWarnings("deprecation")
	public Map<String, Object> findById(CustomEntityTemplate cet, String uuid, List<String> selectFields) throws EntityDoesNotExistsException {
		// Retrieve fields of the template
		Collection<CustomFieldTemplate> cfts = customFieldsCacheContainerProvider.getCustomFieldTemplates(cet.getAppliesTo()).values();

		// Get raw data
		Map<String, Object> data = super.findById(SQLStorageConfiguration.getDbTablename(cet), uuid, selectFields);

		if(data == null) {
		    throw new EntityDoesNotExistsException("CET " + cet.getCode() + " with UUID : " + uuid);
        }

		// Format the data to the representation defined by the fields
		Map<String, Object> convertedData = convertData(data, cet);

		// Replace the db column names by the fields codes
		return replaceKeys(cfts, convertedData);
	}

	/**
	 * Convert the data to the expected format. For instance, deserializes lists
	 *
	 * @param data Raw data
	 * @param cet  Template of the data
	 * @return the converted data
	 */
    private Map<String, Object> convertData(Map<String, Object> data, CustomEntityTemplate cet){
    	return convertData(Collections.singletonList(data), cet).get(0);
    }

    /**
     * Convert the data to the expected format. For instance, deserializes lists
     *
     * @param data Raw data
     * @param cet  Template of the data
     * @return the converted data
     */
    private List<Map<String, Object>> convertData(List<Map<String, Object>> data, CustomEntityTemplate cet){
        final Collection<CustomFieldTemplate> cfts = customFieldsCacheContainerProvider.getCustomFieldTemplates(cet.getAppliesTo()).values();
        final List<Map<String, Object>> convertedData = new ArrayList<>();

        for(int i = 0; i < data.size(); i++){
        	Map<String, Object> modifiableMap = new HashMap<>();
        	convertedData.add(i, modifiableMap);

            for(Entry<String, Object> field : data.get(i).entrySet()){
            	if(field.getKey().equals("uuid")) {
            		modifiableMap.put(field.getKey(), field.getValue());
            		continue;
            	}

            	CustomFieldTemplate cft = getCustomFieldTemplate(cfts, field).get();

            	// De-serialize lists
                if(cft.getStorageType().equals(CustomFieldStorageTypeEnum.LIST)){
                    if(!(field.getValue() instanceof Collection) && field.getValue() != null){
                        modifiableMap.put(field.getKey(), JacksonUtil.fromString((String) field.getValue(), List.class));
                    }
                } else {
                	modifiableMap.put(field.getKey(), field.getValue());
                }
            }
        }

        return convertedData;
    }

    public boolean sqlCftFilter(CustomEntityTemplate cet, String key) {
        final CustomFieldTemplate cft = customFieldsCacheContainerProvider.getCustomFieldTemplate(key, cet.getAppliesTo());
        if (cft != null) {
            return cft.getStorages().contains(DBStorageType.SQL);
        }

        return true;
    }

    public List<Map<String, Object>> list(CustomEntityTemplate cet) {
        return list(cet, null);
    }

    /**
     * Search etities, fetching entity references and converting field names from db column name to custom field names
     */
    public List<Map<String, Object>> searchAndFetch(String cetCode, PaginationConfiguration pagination){
    	CustomEntityTemplate cet = customEntityTemplateService.findByCode(cetCode);
    	Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());
    	
    	List<Map<String, Object>> entities = customTableService.list(SQLStorageConfiguration.getDbTablename(cet), pagination);
    	
    	cfts.values().forEach(cft -> entities.forEach(entity -> {
            Object property = entity.get(cft.getDbFieldname());
            if(property != null) {
                // Fetch entity reference
                if(cft.getFieldType() == CustomFieldTypeEnum.ENTITY) {
                    String propertyTableName = SQLStorageConfiguration.getCetDbTablename(cft.getEntityClazzCetCode());
                    property = customTableService.findById(propertyTableName, (String) property);
                }

                // Replace db field names to cft name
                entity.remove(cft.getDbFieldname());
                entity.put(cft.getCode(), property);
            }
        }));
    	
    	return entities;
    }

    public Map<String, Object> replaceKeys(Collection<CustomFieldTemplate> cfts, Map<String, Object> values){
        for(CustomFieldTemplate cft : cfts){
            final Object tempVal = values.remove(cft.getDbFieldname());
            if(tempVal != null){
                values.put(cft.getCode(), tempVal);
            }
        }

        return values;
    }

    private Map<String, Object> filterValues(Map<String, Object> values, CustomModelObject cet) {
    	return filterValues(values, cet, true);
    }

    private Map<String, Object> filterValues(Map<String, Object> values, CustomModelObject cet, boolean removeNullValues) {
    	Collection<CustomFieldTemplate> cfts = customFieldsCacheContainerProvider.getCustomFieldTemplates(cet.getAppliesTo()).values();
    	
        return values.entrySet()
                .stream()
                .filter(entry -> {
                	// Do not allow files to be stored directly in table
                	if(entry.getValue() instanceof File) {
                		return false;
                	}

                	// Do not allow list of files to be stored directly in table
                	if(entry.getValue() instanceof List) {
                		List<?> listValue = (List<?>) entry.getValue();
                		if(!listValue.isEmpty() && (listValue.get(0) instanceof File)) {
                			return false;
                		}
                	}

                    if(entry.getKey().equals("uuid")) {
                        return true;
                    }
                    
                    if(entry.getValue() == null && removeNullValues) {
                    	return false;
                    }

                    Optional<CustomFieldTemplate> customFieldTemplateOpt = getCustomFieldTemplate(cfts, entry);

                    if(customFieldTemplateOpt.isPresent()) {
                        return customFieldTemplateOpt.get().getStorages().contains(DBStorageType.SQL);
                    }else {
                    	log.warn("Column {} of table {} cannot be translated into custom field", entry.getKey(), SQLStorageConfiguration.getCetDbTablename(cet.getCode()));
                    	return false;
                    }
                    
                }).collect(HashMap::new, (m,v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);
    }

    private Optional<CustomFieldTemplate> getCustomFieldTemplate(Collection<CustomFieldTemplate> cfts, Entry<String, Object> entry) {
        return cfts.stream()
                                .filter(f -> f.getCode().equals(entry.getKey()) || f.getDbFieldname().equals(entry.getKey()))
                                .findFirst();
    }
}
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
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.hibernate.SQLQuery;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ValidationException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.shared.DateUtils;
import org.meveo.model.typereferences.GenericTypeReferences;
import org.meveo.service.base.NativePersistenceService;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;

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
 * @version 6.6.0
 */
// @Stateless
public class CustomTableService extends NativePersistenceService {

    /**
     * File prefix indicating that imported data should be appended to exiting data
     */
    public static final String FILE_APPEND = "_append";

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    private Instance<CustomTableService> customTableService;

    @Inject
    protected ParamBeanFactory paramBeanFactory;
    
    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private CustomFieldsCacheContainerProvider customFieldsCacheContainerProvider;

    @Inject
    private CustomFieldInstanceService customFieldInstanceService;
    
	/**
	 * Inserts an instance of {@linkplain CustomEntityInstance} into the database.
	 *
	 * @param cet {@link CustomEntityTemplate} to insert values to
	 * @param cei transient {@link CustomEntityInstance}
	 * @return uuid of the newly created entity
	 * @throws BusinessException failed creating the entity
	 */
	public String create(String sqlConnectionCode, CustomEntityTemplate cet, CustomEntityInstance cei) throws BusinessException {
		Collection<CustomFieldTemplate> cfts = (cet.getSuperTemplate() == null ? customFieldsCacheContainerProvider.getCustomFieldTemplates(cet.getAppliesTo()) : customFieldTemplateService.getCftsWithInheritedFields(cet)).values();
		cei.setCet(cet);
		return create(sqlConnectionCode, cei, true, true, cfts, true);
	}

	/**
	 * Insert multiple {@linkplain CustomEntityInstance} into a database.
	 *
	 * @param cet {@link CustomEntityTemplate} to insert values to
	 * @param ceis list of transient {@link CustomEntityInstance}
	 * @throws BusinessException General exception
	 */
	@SuppressWarnings("deprecation")
	public void create(String sqlConnectionCode, CustomEntityTemplate cet, List<CustomEntityInstance> ceis) throws BusinessException {

		for (CustomEntityInstance cei : ceis) {
			create(sqlConnectionCode, cet, cei);
		}
	}

	/**
	 * Inserts an instance of {@linkplain CustomEntityInstance} into the database
	 * using a new transaction.
	 *
	 * @param cet {@link CustomEntityTemplate} to insert values to
	 * @param cei transient {@link CustomEntityInstance}
	 * @throws BusinessException failed creating the entity
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void createInNewTx(String sqlConnectionCode, CustomEntityTemplate cet, CustomEntityInstance cei) throws BusinessException {

		create(sqlConnectionCode, cet, cei);
	}

	/**
	 * nsert multiple {@linkplain CustomEntityInstance} into a database using a new transaction.
	 *
	 * @param cet {@link CustomEntityTemplate} to insert values to
	 * @param updateES if true, update the records in the ElasticSearch cache
	 * @param values map of <String, Object>
	 * @throws BusinessException
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void createInNewTx(String sqlConnectionCode, CustomEntityTemplate cet, boolean updateES, List<Map<String, Object>> values) throws BusinessException {

		List<CustomEntityInstance> ceis = new ArrayList<>();

		for (Map<String, Object> value : values) {
			CustomEntityInstance cei = new CustomEntityInstance();
			cei.setCetCode(cet.getCode());
			customFieldInstanceService.setCfValues(cei, cei.getCetCode(), value);
			ceis.add(cei);
		}

		createInNewTx(sqlConnectionCode, cet, ceis, updateES);
	}

    /**
     * Insert multiple values into table with optionally not updating ES. Will execute in a new transaction
     * 
     * @param cet CustomEntityTemplate to insert values to
     * @param ceis list of transient {@link CustomEntityInstance} to insert
     * @param updateES should Elastic search be updated during record creation. If false, ES population must be done outside this call
     * @throws BusinessException General exception
     */
	@Transactional(TxType.REQUIRES_NEW)
	public void createInNewTx(String sqlConnectionCode, CustomEntityTemplate cet, List<CustomEntityInstance> ceis, boolean updateES) throws BusinessException {

		// Insert record to db, with ID returned, but flush to ES after the values are
		// processed
		if (updateES) {
			create(sqlConnectionCode, cet, ceis);

		} else {
			final String tablename = SQLStorageConfiguration.getDbTablename(cet);
			super.create(sqlConnectionCode, tablename, ceis);
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
	public void update(String sqlConnectionCode, CustomEntityInstance cei) throws BusinessException {

		super.update(sqlConnectionCode, cei);
	}

	/**
	 * Updates a {@linkplain CustomEntityInstance}. This object must contain a cf value with code uuid.
	 *
	 * @param cet
	 * @param cei transient {@link CustomEntityInstance}
	 * @throws BusinessException failed to update the entity
	 */
    @SuppressWarnings("deprecation")
    public void update(String sqlConnectionCode, CustomEntityTemplate cet, CustomEntityInstance cei) throws BusinessException {
    	Collection<CustomFieldTemplate> cfts = customFieldsCacheContainerProvider.getCustomFieldTemplates(cet.getAppliesTo()).values();
		cei.setCet(cet);
    	super.update(sqlConnectionCode, cei, true, cfts, false);
    }

    /**
     * Update multiple {@linkplain CustomEntityInstance} in a table. Record is identified by an "uuid" field value.
     * 
     * @param cet CustomEntityTemplate to update values
     * @param values list of {@link CustomEntityInstance}. Must contain a cf field with code 'uuid'
     * @throws BusinessException General exception
     */
	public void update(String sqlConnectionCode, CustomEntityTemplate cet, List<CustomEntityInstance> ceis) throws BusinessException {

		for (CustomEntityInstance cei : ceis) {
			update(sqlConnectionCode, cet, cei);
		}
	}

    @Override
    public void updateValue(String sqlConnectionCode, String tableName, String uuid, String fieldName, Object value) throws BusinessException {
        super.updateValue(sqlConnectionCode, tableName, uuid, fieldName, value);
    }

    @Override
    public void disable(String sqlConnectionCode, String tableName, String uuid) throws BusinessException {
        super.disable(sqlConnectionCode, tableName, uuid);
    }

    @Override
    public void disable(String sqlConnectionCode, String tableName, Set<String> ids) throws BusinessException {
        super.disable(sqlConnectionCode, tableName, ids);
    }

    @Override
    public void enable(String sqlConnectionCode, String tableName, String uuid) throws BusinessException {
        super.enable(sqlConnectionCode, tableName, uuid);
        Map<String, Object> values = findById(sqlConnectionCode, tableName, uuid);
    }

    @Override
    public void enable(String sqlConnectionCode, String tableName, Set<String> ids) throws BusinessException {
        super.enable(sqlConnectionCode, tableName, ids);
        for (String uuid : ids) {
            Map<String, Object> values = findById(sqlConnectionCode, tableName, uuid);
        }
    }

    /**
     * Export data into a file into exports directory. Filename is in the following format: &lt;db table name&gt;_id_&lt;formated date&gt;.csv
     * 
     * @param cet Custom table definition
     * @param config Pagination and search criteria
     * @return A future with a file name where the data will be exported to or an exception occurred
     */
    @Asynchronous
    @SuppressWarnings({"unchecked", "deprecation"})
    public Future<DataImportExportStatistics> exportData(String sqlConnectionCode, CustomEntityTemplate cet, PaginationConfiguration config) {

        try {
            final String dbTablename = SQLStorageConfiguration.getDbTablename(cet);
            QueryBuilder queryBuilder = getQuery(dbTablename, config);

            SQLQuery query = queryBuilder.getNativeQuery(getEntityManager(sqlConnectionCode), true);

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

            Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());

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
                    Map<String, Map<String, String>> entityReferencesCache = new HashMap<>();	// Cache used to avoid fetching multiple time the same data
                    for(Map<String, Object> map : values) {
                    	for(Entry<String, Object> entry : new HashMap<>(map).entrySet()) {
                    		for(CustomFieldTemplate field : fields) {
                    			if(field.getDbFieldname().equals(entry.getKey()) && field.getFieldType() == CustomFieldTypeEnum.ENTITY) {
                    				String referencedEntity = entityReferencesCache
                    						.computeIfAbsent((String) entry.getValue(), k -> new HashMap<>())
                    						.computeIfAbsent(field.getDbFieldname(), fieldName -> fetchField(sqlConnectionCode, entry.getValue(), field, fieldName));

                    				map.put(entry.getKey(), referencedEntity);
                    			}
                    		}
                    	}
                    }
                    
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

    public String fetchField(String sqlConnectionCode, Object id, CustomFieldTemplate field, String tableName) {
		log.info("Fetching {} with uuid {}", field.getCode(), id);
		CustomEntityTemplate cet = customFieldsCacheContainerProvider.getCustomEntityTemplate(field.getEntityClazzCetCode());
		Map<String, Object> entityRefValues;
		entityRefValues = findById(sqlConnectionCode, cet, (String) id);
		entityRefValues.remove("uuid");				// We don't want to save the uuid
		return JacksonUtil.toString(entityRefValues);
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
    public int importData(String sqlConnectionCode, CustomEntityTemplate customEntityTemplate, File file, boolean append) throws BusinessException {

        try (FileInputStream inputStream = new FileInputStream(file)) {
            return importData(sqlConnectionCode, customEntityTemplate, inputStream, append);

        } catch (IOException e) {
            throw new BusinessException(e);
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
    public int importData(String sqlConnectionCode, CustomEntityTemplate cet, InputStream inputStream, boolean append) throws BusinessException {

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
                	replaceEntityreferences(sqlConnectionCode, fields, entityReferencesCache, lineValues);
                	String uuid = findIdByUniqueValues(sqlConnectionCode, cet, lineValues, fields);
                	if(uuid == null) {
                		final String tablename = SQLStorageConfiguration.getDbTablename(cet);
                		super.createInNewTx(sqlConnectionCode, tablename, lineValues);
                		importedLines++;
                        importedLinesTotal++;
                	}
                } else {
	            	// Save to DB every 500 records
	                if (importedLines >= 500) {
	
	                    saveBatch(sqlConnectionCode, cfts, fields, cet.getCode(), values, entityReferencesCache);
	
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
	            saveBatch(sqlConnectionCode, cfts, fields, cet.getCode(), values, entityReferencesCache);
            }

            log.info("Imported {} lines to {} table", importedLinesTotal, dbTableName);

        } catch (RuntimeJsonMappingException e) {
            throw new ValidationException("Invalid file format", "message.upload.fail.invalidFormat", e);

        } catch (IOException e) {
            throw new BusinessException(e);
        }

        return importedLinesTotal;
    }

	public int importData(String sqlConnectionCode, CustomModelObject customModelObject, List<CustomEntityInstance> ceis, boolean append) throws BusinessException {
		if (ceis == null || ceis.isEmpty()) {
			return 0;
		}

		List<Map<String, Object>> values = ceis.stream().map(CustomEntityInstance::getCfValuesAsValues).collect(Collectors.toList());

		return importData(sqlConnectionCode, ceis.get(0).getTableName(), customModelObject, values, append);
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
    public int importData(String sqlConnectionCode, String tableName, CustomModelObject customModelObject, List<Map<String, Object>> values, boolean append) throws BusinessException {

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
            remove(sqlConnectionCode, (CustomEntityTemplate) customModelObject);
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
                    customTableService.get().createInNewTx(sqlConnectionCode, cet, updateESImediately, valuesPartial);

                    valuesPartial.clear();
                    importedLines = 0;
                }

                valuesPartial.add(value);

                importedLines++;
                importedLinesTotal++;
            }

            // Save to DB remaining records
            valuesPartial = convertValues(valuesPartial, cfts, false);
            customTableService.get().createInNewTx(sqlConnectionCode, cet, updateESImediately, valuesPartial);
        } catch (Exception e) {
            throw new BusinessException(e);
        }

        return importedLinesTotal;
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
    public Future<DataImportExportStatistics> importDataAsync(String sqlConnectionCode, CustomEntityTemplate customEntityTemplate, InputStream inputStream, boolean append) {

        try {
            int itemsImported = importData(sqlConnectionCode, customEntityTemplate, inputStream, append);
            return new AsyncResult<>(new DataImportExportStatistics(itemsImported));

        } catch (Exception e) {
        	log.error("Error importing data", e);
            return new AsyncResult<>(new DataImportExportStatistics(e));
        }
    }

	private void saveBatch(String sqlConnectionCode, Map<String, CustomFieldTemplate> cfts, List<CustomFieldTemplate> fields, String cetCode, List<Map<String, Object>> values,
			Map<String, Map<String, String>> entityReferencesCache) throws BusinessException {

		List<CustomEntityInstance> ceis = new ArrayList<>();
		for (Map<String, Object> value : values) {
			CustomEntityInstance cei = new CustomEntityInstance();
			cei.setCetCode(cetCode);
			customFieldInstanceService.setCfValues(cei, cei.getCetCode(), value);
			ceis.add(cei);
		}

		saveBatch(sqlConnectionCode, cfts, fields, ceis, entityReferencesCache);
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
	private void saveBatch(String sqlConnectionCode, Map<String, CustomFieldTemplate> cfts, List<CustomFieldTemplate> fields,  List<CustomEntityInstance> ceis, Map<String, Map<String, String>> entityReferencesCache) throws BusinessException {
		
		if(ceis == null || ceis.isEmpty()) {
			return;
		}

		List<Map<String, Object>> values = ceis.stream().map(CustomEntityInstance::getCfValuesAsValues).collect(Collectors.toList());
		values = convertValues(values, cfts, false);
		values = replaceEntityReferences(sqlConnectionCode, fields, values, entityReferencesCache);
        final CustomEntityTemplate cet = customEntityTemplateService.findByCodeOrDbTablename(ceis.get(0).getTableName());
        customTableService.get().createInNewTx(sqlConnectionCode, cet, false, values);
	}

	private List<Map<String, Object>> replaceEntityReferences(String sqlConnectionCode, List<CustomFieldTemplate> fields, List<Map<String, Object>> oldvalues, Map<String, Map<String, String>> entityReferencesCache) throws BusinessException {
		List<Map<String, Object>> values = new ArrayList<>(oldvalues);
		/* Create or retrieve entity references */
		for (Map<String, Object> map : values) {
		    replaceEntityreferences(sqlConnectionCode, fields, entityReferencesCache, map);
		}
		return values;
	}

	private void replaceEntityreferences(String sqlConnectionCode, List<CustomFieldTemplate> fields, Map<String, Map<String, String>> entityReferencesCache, Map<String, Object> entityRefValueMap) throws BusinessException {
		final HashMap<String, Object> iterationMap = new HashMap<>(entityRefValueMap);
		for (Entry<String, Object> entry : iterationMap.entrySet()) {
		    String key = entry.getKey();
		    Object value = entry.getValue();
		    final Optional<CustomFieldTemplate> templateOptional = fields.stream().filter(f -> f.getDbFieldname().equals(key)).findFirst();

		    if (templateOptional.isPresent() && templateOptional.get().getFieldType() == CustomFieldTypeEnum.ENTITY) {
		    	CustomEntityTemplate entityRef = customEntityTemplateService.findByCode(templateOptional.get().getEntityClazzCetCode());
		        // Try to retrieve record first
		        String uuid = entityReferencesCache.computeIfAbsent(key, k -> new HashMap<>())
		                .computeIfAbsent(
		                        (String) value,
		                        serializedValues -> {
		                            Map<String, Object> entityRefValues = JacksonUtil.fromString(serializedValues, GenericTypeReferences.MAP_STRING_OBJECT);
		                            return findIdByUniqueValues(sqlConnectionCode, entityRef, entityRefValues, fields);
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
					uuid = create(sqlConnectionCode, cet, cei);
				}

		        entityRefValueMap.put(key, uuid);
		    }
		}
	}

    /**
     * Get the CSV file reader. Schema is created from field's dbFieldname values.
     * 
     * @param fields Custom table fields definition
     * @return The CSV file reader
     */
    private ObjectReader getCSVReader(Collection<CustomFieldTemplate> fields) {
        CsvSchema.Builder builder = CsvSchema.builder();

        builder.addColumn(NativePersistenceService.FIELD_ID, ColumnType.STRING);

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
    
    public List<Map<String, Object>> list(String sqlConnectionCode, CustomEntityTemplate cet) {
        return list(sqlConnectionCode, cet, null);
    }

	@Override
	public List<Map<String, Object>> list(String sqlConnectionCode, CustomEntityTemplate cet, PaginationConfiguration config) {
		PaginationConfiguration paginationConfiguration = new PaginationConfiguration(config);
		
		// Only use SQL filters
		if (config != null && config.getFilters() != null) {
			final Map<String, Object> sqlFilters = config.getFilters().entrySet().stream().filter(stringObjectEntry -> sqlCftFilter(cet, stringObjectEntry.getKey()))
					.filter(e -> Objects.nonNull(e.getValue())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			paginationConfiguration.setFilters(sqlFilters);
		}

		// Only fetch SQL fields
		if (config != null && config.getFetchFields() != null) {
			List<String> sqlFetchFields = config.getFetchFields().stream().filter(s -> sqlCftFilter(cet, s)).collect(Collectors.toList());
			paginationConfiguration.setFetchFields(sqlFetchFields);
		}
		
		if(cet.getSuperTemplate() != null) {
			CustomEntityTemplate parentCet = customEntityTemplateService.findById(cet.getSuperTemplate().getId());
			var parentCfts = customFieldsCacheContainerProvider.getCustomFieldTemplates(parentCet.getAppliesTo());
			paginationConfiguration.setSuperType(SQLStorageConfiguration.getDbTablename(parentCet));
			paginationConfiguration.setSuperTypeFields(parentCfts.keySet());
		}
		
		final List<Map<String, Object>> data = super.list(sqlConnectionCode, SQLStorageConfiguration.getDbTablename(cet), paginationConfiguration);
		if (cet.getCode().startsWith(CustomEntityTemplate.AUDIT_PREFIX)) {
			return data;

		} else {
			return convertData(data, cet);
		}
	}

    public Map<String, Object> findById(String sqlConnectionCode, CustomEntityTemplate cet, String uuid) {
        return findById(sqlConnectionCode, cet, uuid, null);
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
	public Map<String, Object> findById(String sqlConnectionCode, CustomEntityTemplate cet, String uuid, List<String> selectFields)  {
		var selectFieldsCopy = selectFields == null ? null : new ArrayList<>(selectFields);
		
		// Retrieve fields of the template
		Collection<CustomFieldTemplate> cfts = customFieldsCacheContainerProvider.getCustomFieldTemplates(cet.getAppliesTo()).values();

		Map<String, Object> data = new HashMap<>();
		
		// Complete data with parent table
		var superTemplate = cet.getSuperTemplate() != null ? customEntityTemplateService.findById(cet.getSuperTemplate().getId()) : null;
		if(superTemplate != null && superTemplate.storedIn(DBStorageType.SQL)) {
			var parentCfts = customFieldsCacheContainerProvider.getCustomFieldTemplates(superTemplate.getAppliesTo());
			List<String> parentFieldsToSelect;
			if(selectFieldsCopy != null) {
				parentFieldsToSelect = new ArrayList<>();
				parentCfts.values()
					.forEach(cft -> {
						var dbColKey = cft.getCode();
						var isFieldSelected =  selectFieldsCopy.remove(dbColKey);
						if(isFieldSelected) {
							parentFieldsToSelect.add(dbColKey);
						}
					});
			} else {
				parentFieldsToSelect = null;
			}

			
			var parentData = super.findById(sqlConnectionCode, SQLStorageConfiguration.getDbTablename(cet.getSuperTemplate()), uuid, parentFieldsToSelect);
			if(parentData != null) {
				data.putAll(parentData);
			}
		}
		
		// Get raw data
		var rowData = super.findById(sqlConnectionCode, SQLStorageConfiguration.getDbTablename(cet), uuid, selectFieldsCopy);
		if(rowData != null) {
			data.putAll(rowData);
		}
		
		if(data.isEmpty()) {
			return null;
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
        var cfts = customFieldsCacheContainerProvider.getCustomFieldTemplates(cet.getAppliesTo());
		if(cet.getSuperTemplate() != null) {
			var parentCet = customEntityTemplateService.findById(cet.getSuperTemplate().getId());
			var parentCfts = customFieldTemplateService.findByAppliesTo(parentCet.getAppliesTo());
			parentCfts.forEach(cfts::putIfAbsent);
		}
        
        final List<Map<String, Object>> convertedData = new ArrayList<>();

        for(int i = 0; i < data.size(); i++){
        	Map<String, Object> modifiableMap = new HashMap<>();
        	convertedData.add(i, modifiableMap);

            for(Entry<String, Object> field : data.get(i).entrySet()){
            	if(field.getKey().equals("uuid")) {
            		modifiableMap.put(field.getKey(), field.getValue());
            		continue;
            	}

            	Optional<CustomFieldTemplate> customFieldTemplate = getCustomFieldTemplate(cfts.values(), field);
            	if(!customFieldTemplate.isPresent()) {
            		log.warn("No custom field template found for {}", field);
            		continue;
            	}
            	
            	CustomFieldTemplate cft = customFieldTemplate.get();

            	// De-serialize lists
                if(cft.getStorageType().equals(CustomFieldStorageTypeEnum.LIST)){
                    if(!(field.getValue() instanceof Collection) && field.getValue() != null){
                        modifiableMap.put(field.getKey(), JacksonUtil.fromString((String) field.getValue(), List.class));
                    }
                } else if(cft.getFieldType().equals(CustomFieldTypeEnum.BOOLEAN) && field.getValue() instanceof Integer) {
                	modifiableMap.put(field.getKey(), ((int) field.getValue()) == 1);
                } else if(field.getValue() instanceof BigInteger) {
                	modifiableMap.put(field.getKey(), ((BigInteger) field.getValue()).longValue());
            	} else if(field.getValue() instanceof String && cft.getFieldType().equals(CustomFieldTypeEnum.EMBEDDED_ENTITY)) {
                    modifiableMap.put(field.getKey(), JacksonUtil.fromString((String) field.getValue(), GenericTypeReferences.MAP_STRING_OBJECT));
            	} else if(field.getValue() instanceof String && cft.getFieldType().equals(CustomFieldTypeEnum.CHILD_ENTITY)) {
                    modifiableMap.put(field.getKey(), JacksonUtil.fromString((String) field.getValue(), GenericTypeReferences.MAP_STRING_OBJECT));
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
            return cft.getStoragesNullSafe().contains(DBStorageType.SQL);
        }

        return true;
    }

    /**
     * Search etities, fetching entity references and converting field names from db column name to custom field names
     */
    public List<Map<String, Object>> searchAndFetch(String sqlConnectionCode, String cetCode, PaginationConfiguration pagination){
    	CustomEntityTemplate cet = customEntityTemplateService.findByCode(cetCode);
    	Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());
    	
    	List<Map<String, Object>> entities = list(sqlConnectionCode, SQLStorageConfiguration.getDbTablename(cet), pagination);
    	
    	cfts.values().forEach(cft -> entities.forEach(entity -> {
            Object property = entity.get(cft.getDbFieldname());
            if(property != null) {
                // Fetch entity reference
                if(cft.getFieldType() == CustomFieldTypeEnum.ENTITY) {
                    String propertyTableName = SQLStorageConfiguration.getCetDbTablename(cft.getEntityClazzCetCode());
                    property = findById(sqlConnectionCode, propertyTableName, (String) property);
                }

                // Replace db field names to cft name
                entity.remove(cft.getDbFieldname());
                entity.put(cft.getCode(), property);
            }
        }));
    	
    	return entities;
    }

    public Map<String, Object> replaceKeys(Collection<CustomFieldTemplate> cfts, Map<String, Object> values){
    	if(values == null) {
    		values = new HashMap<>();
    	}
    	
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
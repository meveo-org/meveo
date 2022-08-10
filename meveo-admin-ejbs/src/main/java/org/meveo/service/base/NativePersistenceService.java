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
package org.meveo.service.base;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.util.HibernateUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ValidationException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.IdentifiableEnum;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.EntityReferenceWrapper;
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
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.model.transformer.AliasToEntityOrderedMapResultTransformer;
import org.meveo.persistence.CrossStorageTransaction;
import org.meveo.persistence.impl.SQLStorageImpl;
import org.meveo.persistence.sql.SQLConnectionProvider;
import org.meveo.persistence.sql.SqlConfigurationService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomTableService;
import org.meveo.service.custom.PostgresReserverdKeywords;
import org.meveo.util.MeveoParamBean;

/**
 * Generic implementation that provides the default implementation for
 * persistence methods working directly with native DB tables
 *
 * @author Andrius Karpavicius
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.12
 */
public class NativePersistenceService extends BaseService {
	
	@FunctionalInterface
	public static interface SqlAction {
		void doWork(PreparedStatement ps) throws Exception;
	}

	/**
	 * ID field name
	 */
	public static String FIELD_ID = "uuid";

	/**
	 * Valid from field name
	 */
	public static String FIELD_VALID_FROM = "valid_from";

	/**
	 * Validity priority field name
	 */
	public static String FIELD_VALID_PRIORITY = "valid_priority";

	/**
	 * Disabled field name
	 */
	public static String FIELD_DISABLED = "disabled";

	@Inject
	@MeveoJpa
	private EntityManagerWrapper emWrapper;

	@Inject
	@MeveoParamBean
	protected ParamBean paramBean;

	@Inject
	@Updated
	private Event<CustomTableRecord> customTableRecordUpdate;

	@Inject
	@Removed
	private Event<CustomTableRecord> customTableRecordRemoved;

	@Inject
	private SQLConnectionProvider sqlConnectionProvider;
	
	@Inject
	private SqlConfigurationService sqlConfigurationService;
	
	@Inject
	private CustomFieldTemplateService customFieldTemplateService;

	@Inject
    private CustomEntityTemplateService customEntityTemplateService;
	
    @Inject
    private SQLStorageImpl sqlStorageImpl;
    
    @Inject
    private CustomFieldsCacheContainerProvider cache;

	/**
	 * Return an entity manager for a current provider
	 * @param sqlConfigurationCode Code of the sql configuration used to init the entity manager
	 *
	 * @deprecated Use {@link SQLConnectionProvider#getSession(String)} instead
	 * @return Entity manager
	 */
	@Deprecated
	public EntityManager getEntityManager(String sqlConfigurationCode) {

		EntityManager em;
		if (sqlConfigurationCode.equals(SqlConfiguration.DEFAULT_SQL_CONNECTION)) {
			em = emWrapper.getEntityManager();
			em.joinTransaction();

		} else {
			em = sqlConnectionProvider.getSession(sqlConfigurationCode);

		}

		return em;
	}

	/**
	 * Find record by its identifier
	 * @param sqlConnectionCode Datasource to query
	 * @param tableName 		Table name
	 * @param uuid      		Identifier
	 * @return A map of values with field name as a map key and field value as a map
	 *         value
	 */
	public Map<String, Object> findById(String sqlConnectionCode, String tableName, String uuid) {
		return findById(sqlConnectionCode, tableName, uuid, null);
	}

	/**
	 * Find record by its identifier
	 *
	 * @param tableName    Table name
	 * @param uuid         Identifier
	 * @param selectFields Fields to return
	 * @deprecated Use
	 *             {@link CustomTableService#findById(org.meveo.model.customEntities.CustomEntityTemplate, String, List)}
	 *             instead
	 * @return A map of values with field name as a map key and field value as a map
	 *         value
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public Map<String, Object> findById(String sqlConnectionCode, String tableName, String uuid, List<String> selectFields) {
		if(uuid == null) {
			throw new IllegalArgumentException("UUID must be provided");
		}

		try {
			tableName = PostgresReserverdKeywords.escapeAndFormat(tableName);

			Session session = sqlStorageImpl.getHibernateSession(sqlConnectionCode);

			StringBuilder selectQuery = new StringBuilder();

			selectQuery.append("SELECT ");

			if (selectFields == null) {
				selectQuery.append("*");
			} else if (selectFields.isEmpty()) {
				selectQuery.append("uuid");
			} else {
				for (String field : selectFields) {
					field = PostgresReserverdKeywords.escapeAndFormat(field);
					selectQuery.append(field).append(", ");
				}
				selectQuery.delete(selectQuery.length() - 2, selectQuery.length());
			}

			NativeQuery query = session.createSQLQuery(selectQuery + " FROM {h-schema}" + tableName + " e WHERE uuid=:uuid");
			query.setParameter("uuid", uuid);
			query.setResultTransformer(AliasToEntityOrderedMapResultTransformer.INSTANCE);

			Map<String, Object> values = (Map<String, Object>) query.uniqueResult();

			return values;

		} catch (Exception e) {
			log.error("Failed to retrieve values from table by uuid {}/{}", tableName, uuid, e);
			throw e;
		}
	}

	private void setSchema(String sqlConnectionCode, Connection connection) {
		String schema = sqlConfigurationService.getSchema(sqlConnectionCode);		
		if(!StringUtils.isBlank(schema)) {
			try(Statement stmt = connection.createStatement()) {
				stmt.execute("SET SCHEMA '" + schema + "'");
			} catch (Exception e) {
				log.error("Can't set schema for connection {}", connection, e);
			}
		}
	}

	/**
	 * Find a record uuid in table using its exact values
	 * 
	 * @param sqlConnectionCode Code of the sql configuration
	 * @param customTemplate Template related to the table
	 * @param queryValues Values used to filter the result
	 * @param fields 
	 * @return The uuid of the record if it was found or null if it was not
	 */
	public String findIdByUniqueValues(String sqlConnectionCode, CustomModelObject customTemplate, Map<String, Object> queryValues, Collection<CustomFieldTemplate> fields) {
		String result = null;
		
		String tableName = PostgresReserverdKeywords.escapeAndFormat(customTemplate.getDbTableName());

		if(queryValues.isEmpty()) {
			throw new IllegalArgumentException("Query values should not be empty");
		}
		
		StringBuilder q = new StringBuilder();
		q.append("SELECT uuid FROM {h-schema}" + tableName + " as a\n");
		
		Map<Integer, Object> queryParamers = new HashMap<>();
		
		Map<String, Object> uniqueValues = new HashMap<>();
		
		for(CustomFieldTemplate cft : fields) {
			if(cft.isUnique()) {
				Object uniqueValue = Optional.ofNullable(queryValues.get(cft.getCode())).orElse(queryValues.get(cft.getDbFieldname()));
				// Don't use inherited values
				if(uniqueValue != null && cft.getAppliesTo().equals(customTemplate.getAppliesTo())) {
					uniqueValues.put(cft.getDbFieldname(), uniqueValue);
				}
			}
		}
		
		if(uniqueValues.isEmpty()) {
			result = null;
		} else {
			AtomicInteger i = new AtomicInteger(1);
			uniqueValues.forEach((key, value) -> {
				key = PostgresReserverdKeywords.escapeAndFormat(key);
				if (!(value instanceof Collection) && !(value instanceof File) && !(value instanceof Map)) {
					if(value instanceof EntityReferenceWrapper) {
						value = ((EntityReferenceWrapper) value).getUuid();
					}
					if(i.get() == 1) {
						q.append("WHERE a." + key + " = ?\n");
					} else {
						q.append("AND a." + key + " = ?\n");
					}
					queryParamers.put(i.getAndIncrement(), value);
				}
			});
			
			QueryBuilder builder = new QueryBuilder();
			builder.setSqlString(q.toString());
			
			Session session = sqlStorageImpl.getHibernateSession(sqlConnectionCode);
			NativeQuery<Map<String, Object>> query = builder.getNativeQuery(session, true);
			queryParamers.forEach((k, v) -> query.setParameter(k, v));
			
			try {
				Map<String, Object> singleResult = query.getSingleResult();
				result = (String) singleResult.get("uuid");
			
			} catch (NoResultException | NonUniqueResultException e) {
				result = null;
				
			} catch (Exception e) {
				log.error("Error executing query {}", query.getQueryString());
				throw e;
			}
		}
		
		if (result == null && customTemplate instanceof CustomEntityTemplate) {
			CustomEntityTemplate cet = (CustomEntityTemplate) customTemplate;
			CustomEntityTemplate superTemplate = cet.getSuperTemplate();
			if (superTemplate != null) {
				if (HibernateUtils.isLazyLoaded(superTemplate)) {
					superTemplate = customEntityTemplateService.findById(superTemplate.getId(), List.of("superTemplate"));
				}
				result = findIdByUniqueValues(sqlConnectionCode, superTemplate, queryValues, fields);
			}
		}
		
		return result;
	}

	/**
	 * Create new or update existing custom table record value
	 *
	 * @param ceis list of {@link CustomEntityInstance}
	 * @throws BusinessException General exception
	 */
	public void createOrUpdate(String sqlConnectionCode, List<CustomEntityInstance> ceis) throws BusinessException {

		for (CustomEntityInstance cei : ceis) {

			// New record
			if (cei.getCfValuesAsValues().get(FIELD_ID) == null) {
				create(sqlConnectionCode, cei, false);

				// Existing record
			} else {
				update(sqlConnectionCode, cei);
			}
		}
	}

	/**
	 * Insert values into a table.
	 * 
	 * @param cei the {@link CustomEntityInstance}
	 * @return the uuid of the created entity
	 * @throws BusinessException failed to insert the entity
	 */
	public String create(String sqlConnectionCode, CustomEntityInstance cei) throws BusinessException {
		return create(sqlConnectionCode, cei, true);
	}

	/**
	 * Insert a new record into a table. If returnId=True values parameter will be
	 * updated with 'uuid' field value.
	 *
	 * @param cei      the {@link CustomEntityInstance}
	 * @param returnId Should identifier be returned - does a lookup in DB by
	 *                 matching same values. If True values will be updated with
	 *                 'uuid' field value.
	 * @throws BusinessException General exception
	 */
	protected String create(String sqlConnectionCode, CustomEntityInstance cei, boolean returnId) throws BusinessException {
		Collection<CustomFieldTemplate> cfts = (cei.getCet().getSuperTemplate() == null ? cache.getCustomFieldTemplates(cei.getCet().getAppliesTo()) : customFieldTemplateService.getCftsWithInheritedFields(cei.getCet())).values();
		return create(sqlConnectionCode, cei, returnId, false, cfts, true);
	}

	/**
	 * Insert a new record into a table.
	 *
	 * @param cei              the {@link CustomEntityInstance}
	 * @param returnId         if true values parameter will be updated with 'uuid'
	 *                         field value.
	 * @param isFiltered       if true process only the values that is stored in SQL
	 * @param cfts             collection of {@link CustomFieldTemplate}
	 * @param removeNullValues whether to remove the null values from the map
	 * @return the uuid of the newly created entity
	 * @throws BusinessException failed to insert the records
	 */
	protected String create(String sqlConnectionCode, CustomEntityInstance cei, boolean returnId, boolean isFiltered, Collection<CustomFieldTemplate> cfts,
			boolean removeNullValues) throws BusinessException {
		
		CustomEntityTemplate cet = cei.getCet();
		
		Map<String, Object> values = cei.getCfValuesAsValues(isFiltered ? DBStorageType.SQL : null, cfts, removeNullValues);
		Map<String, CustomFieldTemplate> cftsMap = cfts.stream()
				.filter(cft -> cft.getAppliesTo().equals(cet.getAppliesTo()))
				.collect(Collectors.toMap(cft -> cft.getCode(), cft -> cft));
		Map<String, Object> convertedValues = convertValue(values, cftsMap, removeNullValues, null);
		convertedValues.put("uuid", cei.getUuid());
		convertedValues = serializeValues(convertedValues, cftsMap);
		
		
		// If template has parent, create the data in this parent first
		if(cet.getSuperTemplate() != null) {
			CustomEntityTemplate parentTemplate = customEntityTemplateService.findById(cet.getSuperTemplate().getId());
			if (parentTemplate.storedIn(DBStorageType.SQL)) {
				CustomEntityInstance parentCei = new CustomEntityInstance();
				parentCei.setCet(parentTemplate);
				parentCei.setCfValues(cei.getCfValues());
				parentCei.setUuid(cei.getUuid());
				var uuid = create(sqlConnectionCode, parentCei, true, isFiltered, cfts, removeNullValues);
				convertedValues.put("uuid", uuid);	
			}
		}
		
		return create(sqlConnectionCode, cei.getTableName(), convertedValues, returnId);
	}

	/**
	 * Insert a new record into a table using a new transaction.
	 *
	 * @param tableName the name of the SQL table
	 * @param values    {@link Map} of values
	 * @throws BusinessException failed to insert the records
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void createInNewTx(String sqlConnectionCode, String tableName, Map<String, Object> values) throws BusinessException {

		create(sqlConnectionCode, tableName, values, false);
	}

	/**
	 * Insert a new record into a table. If returnId=True values parameter will be
	 * updated with 'uuid' field value.
	 *
	 * @param sqlConnectionCode code of the SQL connection, if valid the data will
	 *                          be save in this data source
	 * @param tableName         Table name to update
	 * @param values            Values
	 * @param returnId          Should identifier be returned - does a lookup in DB
	 *                          by matching same values. If True values will be
	 *                          updated with 'uuid' field value.
	 * @throws BusinessException General exception
	 */
	public String create(String sqlConnectionCode, String tableName_, Map<String, Object> values, boolean returnId) throws BusinessException {
		if("null".equals(values.get(FIELD_ID))) {
			values.remove(FIELD_ID);
		}
		
		if (tableName_ == null) {
			throw new BusinessException("Table name must not be null");
		}

		final String tableName = PostgresReserverdKeywords.escapeAndFormat(tableName_);

		if (values == null || values.isEmpty()) {
			throw new IllegalArgumentException("No values to insert");
		}

		StringBuilder sql = new StringBuilder();
		try {

			final Object uuid = values.get(FIELD_ID);

			sql.append("insert into ").append(tableName);
			StringBuilder fields = new StringBuilder();
			StringBuilder fieldValues = new StringBuilder();
			StringBuilder findIdFields = new StringBuilder();

			boolean first = true;
			String fieldNameInSQL = null;
			for (String fieldName : values.keySet()) {
				// Ignore a null ID field
				if (fieldName.equals(FIELD_ID) && values.get(fieldName) == null) {
					continue;
				}
				
				fieldNameInSQL = PostgresReserverdKeywords.escapeAndFormat(fieldName);

				if (!first) {
					fields.append(",");
					fieldValues.append(",");
					findIdFields.append(" and ");
				}
				fields.append(fieldNameInSQL);
				if (values.get(fieldName) == null) {
					fieldValues.append("NULL");
					findIdFields.append(fieldName).append(" IS NULL");

				} else {
					fieldValues.append(" ? ");
					findIdFields.append(fieldNameInSQL).append(" = :").append(fieldName);
				}
				first = false;
			}

			sql.append(" (").append(fields).append(") values (").append(fieldValues).append(")");

			doUpdate(sqlConnectionCode, sql.toString(), ps -> {
				int parameterIndex = 1;
				for (String fieldName : values.keySet()) {
					Object fieldValue = values.get(fieldName);
					if (fieldValue == null) {
						continue;
					}

					setParameterValue(ps, parameterIndex++, fieldValue);
				}
			});
			
			// Find the identifier of the last inserted record
			if (returnId) {
				if (uuid != null) {
					return (String) uuid;
				}

				var hibernateSession = sqlStorageImpl.getHibernateSession(sqlConnectionCode);
				Query query = hibernateSession.createNativeQuery("select uuid from {h-schema}" + tableName + " where " + findIdFields).setMaxResults(1);

				for (String fieldName : values.keySet()) {
					Object fieldValue = values.get(fieldName);
					if (fieldValue == null) {
						continue;
					}

					// Serialize list values
					if (fieldValue instanceof Collection) {
						fieldValue = JacksonUtil.toString(fieldValue);
					} else if (fieldValue instanceof Map) {
						fieldValue = JacksonUtil.toString(fieldValue);
					} else if (fieldValue instanceof File) {
						fieldValue = ((File) fieldValue).getAbsolutePath();
					} else if (fieldValue instanceof EntityReferenceWrapper) {
						fieldValue = ((EntityReferenceWrapper) fieldValue).getUuid();
					}

					query.setParameter(fieldName, fieldValue);
				}

				Object newUuid = query.getSingleResult();
				values.put(FIELD_ID, newUuid);

				return (String) newUuid;

			} else {
				return null;
			}

		} catch (Exception e) {
			log.error("Failed to insert values into OR find ID of table {} {} sql {}", tableName, values, sql, e);
			throw e;
		}
	}

	/**
	 * Insert multiple values into table. Uses a prepared statement.
	 * <p>
	 * NOTE: The sql statement is determined by the fields passed in the first
	 * value, so its important that either all values have the same fields (order
	 * does not matter), or first value has the maximum number of fields
	 *
	 * @param tableName Table name to insert values to
	 * @param ceis      list of {@link CustomEntityInstance}
	 * @throws BusinessException General exception
	 */
	public void create(String sqlConnectionCode, String tableName, List<CustomEntityInstance> ceis) throws BusinessException {

		tableName = PostgresReserverdKeywords.escapeAndFormat(tableName);

		List<Map<String, Object>> values = ceis.stream().map(e -> e.getCfValuesAsValues()).collect(Collectors.toList());

		if (values == null || values.isEmpty()) {
			return;
		}

		StringBuilder sql = new StringBuilder();
		Map<String, Object> firstValue = values.get(0);

		sql.append("insert into ").append(tableName);
		StringBuilder fields = new StringBuilder();
		StringBuilder fieldValues = new StringBuilder();
		List<String> fieldNames = new LinkedList<>();

		boolean first = true;
		for (String fieldName : firstValue.keySet()) {
			fieldName = PostgresReserverdKeywords.escapeAndFormat(fieldName);

			if (!first) {
				fields.append(",");
				fieldValues.append(",");
			}
			fieldNames.add(fieldName);
			fields.append(fieldName);
			fieldValues.append("?");
			first = false;
		}

		sql.append(" (").append(fields).append(") values (").append(fieldValues).append(")");
		
		doBatch(sqlConnectionCode, sql.toString(), (preparedStatement -> {
			Object fieldValue = null;
			int i = 1;
			int itemsProcessed = 0;
			for (Map<String, Object> value : values) {

				i = 1;
				for (String fieldName : fieldNames) {
					fieldValue = value.get(fieldName);

					if (fieldValue == null) {
						preparedStatement.setNull(i, Types.NULL);
					} else if (fieldValue instanceof String) {
						preparedStatement.setString(i, (String) fieldValue);
					} else if (fieldValue instanceof Long) {
						preparedStatement.setLong(i, (Long) fieldValue);
					} else if (fieldValue instanceof Double) {
						preparedStatement.setDouble(i, (Double) fieldValue);
					} else if (fieldValue instanceof BigInteger) {
						preparedStatement.setInt(i, ((BigInteger) fieldValue).intValue());
					} else if (fieldValue instanceof Integer) {
						preparedStatement.setInt(i, (Integer) fieldValue);
					} else if (fieldValue instanceof BigDecimal) {
						preparedStatement.setBigDecimal(i, (BigDecimal) fieldValue);
					} else if (fieldValue instanceof Date) {
						preparedStatement.setDate(i, new java.sql.Date(((Date) fieldValue).getTime()));
					} else if (fieldValue instanceof Collection) {
						preparedStatement.setString(i, JacksonUtil.toString(fieldValue));
					} else {
						log.error("Unhandled field type {}", fieldValue.getClass());
					}

					i++;
				}

				preparedStatement.addBatch();

				// Batch size: 20
				if (itemsProcessed % 500 == 0) {
					preparedStatement.executeBatch();
				}
				itemsProcessed++;
			}
		}));
	}

	/**
	 * Updates a {@linkplain CustomEntityInstance} in the database given a uuid.
	 *
	 * @param sqlConnectionCode Code of the configuration where to make the update
	 * @param cei               the {@link CustomEntityInstance}. The cf values must contain the field uuid.
	 * @throws BusinessException failed updating the entity
	 */
	public void update(String sqlConnectionCode, CustomEntityInstance cei) throws BusinessException {
		var cfts = cache.getCustomFieldTemplates(cei.getCet().getAppliesTo());
		update(sqlConnectionCode, cei, false, cfts.values(), false);
	}

	/**
	 * Update a record in a table. Record is identified by an "uuid" field value.
	 * @param sqlConnectionCode Code of the {@link SqlConfiguration} to use
	 *
	 * @param cei              the {@link CustomEntityInstance}. The cf values must
	 *                         contain the field uuid.
	 * @param isFiltered       if true process only the fields with storage=SQL
	 * @param removeNullValues if true, remove the null values
	 * @throws BusinessException General exception
	 */
	public void update(String sqlConnectionCode, CustomEntityInstance cei, boolean isFiltered, Collection<CustomFieldTemplate> cfts, boolean removeNullValues)
			throws BusinessException {
		
		// Update data in parent template
		if(cei.getCet().getSuperTemplate() != null && cei.getCet().getSuperTemplate().storedIn(DBStorageType.SQL)) {
			var parentCfts = customFieldTemplateService.findByAppliesTo(cei.getCet().getSuperTemplate().getAppliesTo()).values();
			var parentCei = new CustomEntityInstance();
			parentCei.setCet(cei.getCet().getSuperTemplate());
			parentCei.setCfValues(cei.getCfValues());
			parentCei.setUuid(cei.getUuid());
			update(sqlConnectionCode, parentCei, true, parentCfts, removeNullValues);
		}

		String tableName = PostgresReserverdKeywords.escapeAndFormat(cei.getTableName());
		
		Map<String, Object> sqlValues = cei.getCfValuesAsValues(isFiltered ? DBStorageType.SQL : null, cfts, removeNullValues);
		var appliesTo = CustomEntityTemplate.getAppliesTo(cei.getCetCode());
		Map<String, CustomFieldTemplate> cftsMap = cfts.stream()
				.filter(cft -> cft.getAppliesTo().equals(appliesTo))
				.collect(Collectors.toMap(cft -> cft.getCode(), cft -> cft));
		
		// remove inherited data
		for(String sqlValueKey : List.copyOf(sqlValues.keySet())) {
			if(sqlValueKey.equals("uuid")) continue;
			
			if(!cftsMap.containsKey(sqlValueKey)) {
				sqlValues.remove(sqlValueKey);
			}
		}
		
		final Map<String, Object> values = serializeValues(
				convertValue(sqlValues, cftsMap, removeNullValues, null), 
				cftsMap
				);

		for (String key: cftsMap.keySet()) {
			if (key != null && !values.keySet().contains(key) && cftsMap.get(key).getStorageType().equals(CustomFieldStorageTypeEnum.LIST)) {
				values.put(key, new ArrayList<>());
			}
		}

		if (sqlValues.get(FIELD_ID) == null) {
			throw new BusinessException("'uuid' field value not provided to update values in native table");
		}

		if (sqlValues.size() < 2) {
			return; // Nothing to update a there is only "uuid" value inside the map
		}

		StringBuilder sql = new StringBuilder();
		
		try {
			sql.append("UPDATE ").append(tableName).append(" SET ");
			boolean first = true;
			for (String fieldName : values.keySet()) {
				String fieldNameInSQL = PostgresReserverdKeywords.escapeAndFormat(fieldName);

				if (fieldName.equals(FIELD_ID)) {
					continue;
				}

				if (!first) {
					sql.append(",");
				}
				if (values.get(fieldName) == null) {
					sql.append(fieldNameInSQL).append(" = NULL");

				} else {
					sql.append(fieldNameInSQL).append(" = ? ");
				}
				first = false;
			}

			sql.append(" WHERE uuid='" + cei.getUuid() + "'");

			doUpdate(sqlConnectionCode, sql.toString(), ps -> {
				int parameterIndex = 1;
				for (String fieldName : values.keySet()) {
					Object fieldValue = values.get(fieldName);
					if (fieldValue != null && fieldName != "uuid") {
						setParameterValue(ps, parameterIndex++, fieldValue);
					}
				}
			});

			CustomTableRecord record = new CustomTableRecord();
			record.setUuid((String) values.get(FIELD_ID));
			record.setCetCode(cei.getTableName());

			customTableRecordUpdate.fire(record);

		} catch (Exception e) {
			log.error("Failed to insert values into table {} {} sql {}", tableName, values, sql, e);
			throw e;
		}
	}
	
	private void doBatch(String sqlConnectionCode, String sql, SqlAction action) {
		Session hibernateSession = sqlStorageImpl.getHibernateSession(sqlConnectionCode);
		hibernateSession.doWork(connection -> {
			if (!sqlConnectionCode.equals(SqlConfiguration.DEFAULT_SQL_CONNECTION)) {
				if (!sqlConnectionProvider.getSqlConfiguration(sqlConnectionCode).isXAResource())
					connection.setAutoCommit(false);
			}
			setSchema(sqlConnectionCode, connection);

			try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
				action.doWork(ps);
				
				ps.executeBatch();
				if (!sqlConnectionCode.equals(SqlConfiguration.DEFAULT_SQL_CONNECTION)) {
					if (!sqlConnectionProvider.getSqlConfiguration(sqlConnectionCode).isXAResource()) {
						connection.commit();
					}
				}
			} catch (Exception e) {
				log.error("Query failed: {}", e.getMessage());
				if (!sqlConnectionCode.equals(SqlConfiguration.DEFAULT_SQL_CONNECTION)) {
					if (!sqlConnectionProvider.getSqlConfiguration(sqlConnectionCode).isXAResource()) {
						connection.rollback();
					}
				}
			}
		});
	}
	
	private void doUpdate(String sqlConnectionCode, String sql, SqlAction action) {
		Session hibernateSession = sqlStorageImpl.getHibernateSession(sqlConnectionCode);
		hibernateSession.doWork(connection -> {
			if (!sqlConnectionCode.equals(SqlConfiguration.DEFAULT_SQL_CONNECTION)) {
				if (!sqlConnectionProvider.getSqlConfiguration(sqlConnectionCode).isXAResource())
					connection.setAutoCommit(false);
			}
			setSchema(sqlConnectionCode, connection);

			try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
				action.doWork(ps);
				
				ps.executeUpdate();
				if (!sqlConnectionCode.equals(SqlConfiguration.DEFAULT_SQL_CONNECTION)) {
					if (!sqlConnectionProvider.getSqlConfiguration(sqlConnectionCode).isXAResource()) {
						connection.commit();
					}
				}
			} catch (Exception e) {
				log.error("Query failed: {}", e.getMessage());
				if (!sqlConnectionCode.equals(SqlConfiguration.DEFAULT_SQL_CONNECTION)) {
					if (!sqlConnectionProvider.getSqlConfiguration(sqlConnectionCode).isXAResource()) {
						connection.rollback();
					}
				}
			}
		});
	}

	/**
	 * Update field value in a table
	 *
	 * @param sqlConnectionCode Code of the sql repository
	 * @param tableName         Table name to update
	 * @param uuid              Record identifier
	 * @param fieldName         Field to update
	 * @param value             New value
	 * @throws BusinessException General exception
	 */
	@Transactional(value = TxType.SUPPORTS)
	public void updateValue(String sqlConnectionCode, String tableName, String uuid, String fieldName, Object value) throws BusinessException {
		var finalValue = value instanceof Collection ? value = JacksonUtil.toString(value) : value;

		String cetCode = tableName;
		var finalTableName = PostgresReserverdKeywords.escapeAndFormat(tableName);
		var finalFieldName = PostgresReserverdKeywords.escapeAndFormat(fieldName);
		
		StringBuilder sql = new StringBuilder();
		if (finalValue == null) {
			sql.append("update " + finalTableName + " set " + finalFieldName + "= null where uuid = ?");
		} else {
			sql.append("update " + finalTableName + " set " + finalFieldName + "= ? where uuid = ?");
		}
		
		doUpdate(sqlConnectionCode, sql.toString(), statement -> {
			if(finalValue == null) {
				statement.setString(1, uuid);
			} else {
				setParameterValue(statement, 1, finalValue);
				statement.setString(2, uuid);
			}
		});
		
		CustomTableRecord record = new CustomTableRecord();
		record.setUuid(uuid);
		record.setCetCode(cetCode);
		customTableRecordUpdate.fire(record);
	}

	/**
	 * Disable multiple records
	 *
	 * @param tableName Table name to update
	 * @param ids       A list of record identifiers
	 * @throws BusinessException General exception
	 */
	public void disable(String sqlConnectionCode, String tableName, Set<String> ids) throws BusinessException {
		tableName = PostgresReserverdKeywords.escapeAndFormat(tableName);
		doUpdate(sqlConnectionCode, "update " + tableName + " set disabled=1 where uuid in ?", ps -> {
			setParameterValue(ps, 1, ids);
		});
	}

	/**
	 * Disable a record
	 *
	 * @param tableName Table name to update
	 * @param uuid      Record identifier
	 * @throws BusinessException General exception
	 */
	public void disable(String sqlConnectionCode, String tableName, String uuid) throws BusinessException {
		tableName = PostgresReserverdKeywords.escapeAndFormat(tableName);
		doUpdate(sqlConnectionCode, "update " + tableName + " set disabled=1 where uuid= ?", ps -> {
			setParameterValue(ps, 1, uuid);
		});
	}

	/**
	 * Enable multiple records
	 *
	 * @param tableName Table name to update
	 * @param ids       A list of record identifiers
	 * @throws BusinessException General exception
	 */
	public void enable(String sqlConnectionCode, String tableName, Set<String> ids) throws BusinessException {
		tableName = PostgresReserverdKeywords.escapeAndFormat(tableName);
		doUpdate(sqlConnectionCode, "update " + tableName + " set disabled=0 where uuid in ?", ps -> {
			setParameterValue(ps, 1, ids);
		});
	}

	/**
	 * Enable a record
	 *
	 * @param tableName Table name to update
	 * @param uuid      Record identifier
	 * @throws BusinessException General exception
	 */
	public void enable(String sqlConnectionCode, String tableName, String uuid) throws BusinessException {
		tableName = PostgresReserverdKeywords.escapeAndFormat(tableName);
		doUpdate(sqlConnectionCode, "update " + tableName + " set disabled=0 where uuid= ?", ps -> {
			setParameterValue(ps, 1, uuid);
		});
	}

	/**
	 * Delete all records
	 *
	 * @param tableName Table name to update
	 * @throws BusinessException General exception
	 */
	@SuppressWarnings("unchecked")
	public void remove(String sqlConnectionCode, CustomEntityTemplate template) throws BusinessException {
		var tableName = tableName(template);
		
		// Remove records in children tables
		var subTemplates = customEntityTemplateService.getSubTemplates(template);
		subTemplates.forEach(subT -> doUpdate(sqlConnectionCode, "delete from " + tableName(subT), (ps) -> {}));
		
		// Gather uuids to delete
		List<String> uuids = getEntityManager(sqlConnectionCode)
				.createNativeQuery("SELECT uuid FROM {h-schema}"+ tableName, String.class)
				.getResultList();
		
		// Remove in own table
		removeRecords(sqlConnectionCode, tableName, uuids);
		
		// Remove in parent table
		if(template.getSuperTemplate() != null) {
			CustomEntityTemplate parentTemplate = customEntityTemplateService.findById(template.getSuperTemplate().getId());
			if (parentTemplate.storedIn(DBStorageType.SQL)) {
				remove(sqlConnectionCode, parentTemplate, uuids);
			}
		}
	}

	/**
	 * Delete multiple records
	 *
	 * @param tableName Table name to update
	 * @param ids       A set of record identifiers
	 * @throws BusinessException General exception
	 */
	public void remove(String sqlConnectionCode, CustomEntityTemplate template, Collection<String> ids) throws BusinessException {
		// Remove record in children tables
		var subTemplates = customEntityTemplateService.getSubTemplates(template);
		subTemplates.forEach(subT -> removeRecords(sqlConnectionCode, tableName(subT), ids));
		
		// Remove in own table
		removeRecords(sqlConnectionCode, tableName(template), ids);
		
		// Remove record in parent table
		if(template.getSuperTemplate() != null) {
			CustomEntityTemplate parentTemplate = customEntityTemplateService.findById(template.getSuperTemplate().getId());
			remove(sqlConnectionCode, parentTemplate, ids);
		} else {
			for (String id : ids) {
				CustomTableRecord record = new CustomTableRecord();
				record.setCetCode(template.getCode());
				record.setUuid(id);
				customTableRecordRemoved.fire(record);
			}
		}
	}

	/**
	 * @param template
	 * @return
	 */
	private String tableName(CustomEntityTemplate template) {
		var tableName = SQLStorageConfiguration.getDbTablename(template);
		return PostgresReserverdKeywords.escapeAndFormat(tableName);
	}

	/**
	 * @param sqlConnectionCode
	 * @param tableName
	 * @param ids
	 */
	private void removeRecords(String sqlConnectionCode, String tableName, Collection<String> ids) {
		doUpdate(sqlConnectionCode, "delete from " + tableName + " where uuid in ?", (ps -> {
			setParameterValue(ps, 1, ids);
		}));
	}

	/**
	 * Delete a record
	 *
	 * @param tableName Table name to update
	 * @param uuid      Record identifier
	 * @throws BusinessException General exception
	 */
	public void remove(String sqlConnectionCode, CustomEntityTemplate template, String uuid) throws BusinessException {
		// Remove record in children tables
		var subTemplates = customEntityTemplateService.getSubTemplates(template);
		subTemplates.forEach(subT -> removeRecord(sqlConnectionCode, uuid, tableName(subT)));
		
		// Remove in own table
		removeRecord(sqlConnectionCode, uuid, tableName(template));
		
		// Remove record in parent table
		if(template.getSuperTemplate() != null) {
			CustomEntityTemplate parentTemplate = customEntityTemplateService.findById(template.getSuperTemplate().getId());
			remove(sqlConnectionCode, parentTemplate, uuid);
		} else {
			CustomTableRecord record = new CustomTableRecord();
			record.setCetCode(template.getCode());
			record.setUuid(uuid);
			customTableRecordRemoved.fire(record);
		}
	}

	private void removeRecord(String sqlConnectionCode, String uuid, String tableName) {
		doUpdate(sqlConnectionCode, "delete from " + tableName + " where uuid= ?", (ps -> {
			setParameterValue(ps, 1, uuid);
		}));
	}

	/**
	 * Retrieve ONLY enabled values from a table
	 *
	 * @param tableName Table name to query
	 * @return A list of map of values with field name as map's key and field value
	 *         as map's value
	 */
	public List<Map<String, Object>> listActive(String sqlConnectionCode, String tableName) {

		Map<String, Object> filters = new HashMap<>();
		filters.put("disabled", 0);
		return list(sqlConnectionCode, tableName, new PaginationConfiguration(filters));
	}

	/**
	 * Creates NATIVE query to filter entities according data provided in pagination
	 * configuration.
	 * <p>
	 * Search filters (key = Filter key, value = search pattern or value).
	 * <p>
	 * Filter key can be:
	 * <ul>
	 * <li>SQL. Additional sql to apply. Value is either a sql query or an array
	 * consisting of sql query and one or more parameters to apply</li>
	 * <li>&lt;condition&gt; &lt;fieldname1&gt; &lt;fieldname2&gt; ...
	 * &lt;fieldnameN&gt;. Value is a value to apply in condition</li>
	 * </ul>
	 * <p>
	 * A union between different filter items is AND.
	 * <p>
	 * <p>
	 * Condition is optional. Number of fieldnames depend on condition used. If no
	 * condition is specified an "equals ignoring case" operation is considered.
	 * <p>
	 * <p>
	 * Following conditions are supported:
	 * <ul>
	 * <li>fromRange. Ranged search - field value in between from - to values.
	 * Specifies "from" part value: e.g value&lt;=fiel.value. Applies to date and
	 * number type fields.</li>
	 * <li>toRange. Ranged search - field value in between from - to values.
	 * Specifies "to" part value: e.g field.value&lt;=value</li>
	 * <li>list. Value is in field's list value. Applies to date and number type
	 * fields.</li>
	 * <li>inList/not-inList. Field value is [not] in value (list). A comma
	 * separated string will be parsed into a list if values. A single value will be
	 * considered as a list value of one item</li>
	 * <li>minmaxRange. The value is in between two field values. TWO field names
	 * must be provided. Applies to date and number type fields.</li>
	 * <li>minmaxOptionalRange. Similar to minmaxRange. The value is in between two
	 * field values with either them being optional. TWO fieldnames must be
	 * specified.</li>
	 * <li>overlapOptionalRange. The value range is overlapping two field values
	 * with either them being optional. TWO fieldnames must be specified. Value must
	 * be an array of two values.</li>
	 * <li>likeCriterias. Multiple fieldnames can be specified. Any of the multiple
	 * field values match the value (OR criteria). In case value contains *, a like
	 * criteria match will be used. In either case case insensative matching is
	 * used. Applies to String type fields.</li>
	 * <li>wildcardOr. Similar to likeCriterias. A wildcard match will always used.
	 * A * will be appended to start and end of the value automatically if not
	 * present. Applies to
	 * <li>wildcardOrIgnoreCase. Similar to wildcardOr but ignoring case String type
	 * fields.</li>
	 * <li>ne. Not equal.
	 * </ul>
	 * <p>
	 * Following special meaning values are supported:
	 * <ul>
	 * <li>IS_NULL. Field value is null</li>
	 * <li>IS_NOT_NULL. Field value is not null</li>
	 * </ul>
	 * <p>
	 * <p>
	 * <p>
	 * To filter by a related entity's field you can either filter by related
	 * entity's field or by related entity itself specifying code as value. These
	 * two example will do the same in case when quering a customer account:
	 * customer.code=aaa OR customer=aaa
	 * <p>
	 * To filter a list of related entities by a list of entity codes use "inList"
	 * on related entity field. e.g. for quering offer template by sellers: inList
	 * sellers=code1,code2
	 *
	 *
	 * <b>Note:</b> Quering by related entity field directly will result in
	 * exception when entity with a specified code does not exists
	 * <p>
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>invoice number equals "1578AU": Filter key: invoiceNumber. Filter value:
	 * 1578AU</li>
	 * <li>invoice number is not "1578AU": Filter key: ne invoiceNumber. Filter
	 * value: 1578AU</li>
	 * <li>invoice number is null: Filter key: invoiceNumber. Filter value:
	 * IS_NULL</li>
	 * <li>invoice number is not empty: Filter key: invoiceNumber. Filter value:
	 * IS_NOT_NULL</li>
	 * <li>Invoice date is between 2017-05-01 and 2017-06-01: Filter key: fromRange
	 * invoiceDate. Filter value: 2017-05-01 Filter key: toRange invoiceDate. Filter
	 * value: 2017-06-01</li>
	 * <li>Date is between creation and update dates: Filter key: minmaxRange
	 * audit.created audit.updated. Filter value: 2017-05-25</li>
	 * <li>invoice number is any of 158AU, 159KU or 189LL: Filter key: inList
	 * invoiceNumber. Filter value: 158AU,159KU,189LL</li>
	 * <li>any of param1, param2 or param3 fields contains "energy": Filter key:
	 * wildcardOr param1 param2 param3. Filter value: energy</li>
	 * <li>any of param1, param2 or param3 fields start with "energy": Filter key:
	 * likeCriterias param1 param2 param3. Filter value: *energy</li>
	 * <li>any of param1, param2 or param3 fields is "energy": Filter key:
	 * likeCriterias param1 param2 param3. Filter value: energy</li>
	 * </ul>
	 *
	 * @param tableName A name of a table to query
	 * @param config    Data filtering, sorting and pagination criteria
	 * @return Query builder to filter entities according to pagination
	 *         configuration data.
	 */
	@SuppressWarnings({ "rawtypes" })
	public QueryBuilder getQuery(String tableName, PaginationConfiguration config) {
		String startQuery;
		tableName = PostgresReserverdKeywords.escapeAndFormat(tableName);
		
		String superType = config != null ? config.getSuperType() : null;
		if(superType != null) {
			superType = PostgresReserverdKeywords.escapeAndFormat(superType);
		}

		// If no fetch fields are defined, return everything
		if (config == null || config.getFetchFields() == null) {
			if(superType != null) {
				startQuery = "select a.*";
				
				// Declaratively fetch super-type fields
				if(config.getSuperTypeFields() != null) {
					for(var superTypeField : config.getSuperTypeFields()) {
						String fieldName = PostgresReserverdKeywords.escapeAndFormat(superTypeField);
						startQuery += ", b." + fieldName + " ";
					}
				} else {
					startQuery += ", b.*";
				}
				
				startQuery += "from {h-schema}" + tableName + " a ";
				startQuery += " INNER JOIN " + superType + " b ON a.uuid = b.uuid";
			} else {
				startQuery = "select * from {h-schema}" + tableName + " a ";
			}
		} else if (config.getFetchFields().isEmpty()) {
			// If fetch fields are empty, only return UUID
			startQuery = "select a.uuid from {h-schema}" + tableName + " a ";
			if(superType != null) {
				startQuery += " INNER JOIN " + superType + " b ON a.uuid = b.uuid"; 
			}
		} else {
			StringBuilder builder = new StringBuilder("select a.uuid, "); // Always return UUID
			config.getFetchFields().forEach(s -> {
				String fieldName = PostgresReserverdKeywords.escapeAndFormat(s);
				builder.append(fieldName).append(", ");
			});
			builder.delete(builder.length() - 2, builder.length());
			startQuery = builder.append(" from {h-schema}").append(tableName).append(" a ").toString();
			if(superType != null) {
				startQuery += " INNER JOIN " + superType + " b ON a.uuid = b.uuid"; 
			}
		}

		QueryBuilder queryBuilder = new QueryBuilder(startQuery, null);

		if (config == null) {
			return queryBuilder;
		}

		Map<String, Object> filters = config.getFilters();

		if (filters != null && !filters.isEmpty()) {

			for (String key : filters.keySet()) {

				Object filterValue = filters.get(key);
				if (filterValue == null) {
					continue;
				}

				// Key format is: condition field1 field2 or condition-field1-field2-fieldN
				// example: "ne code", condition=code, fieldName=code, fieldName2=null
				String[] fieldInfo = key.split(" ");
				String condition = fieldInfo.length == 1 ? null : fieldInfo[0];
				String fieldName = fieldInfo.length == 1 ? fieldInfo[0] : fieldInfo[1];
				String fieldName2 = fieldInfo.length == 3 ? fieldInfo[2] : null;

				String[] fields = null;
				if (condition != null) {
					fields = Arrays.copyOfRange(fieldInfo, 1, fieldInfo.length);
				}
				
				if (config.getSuperTypeFields() != null && config.getSuperTypeFields().contains(fieldName)) {
					fieldName = "b." + fieldName;
				} else {
					fieldName = "a." + fieldName;
				}

				// if ranged search - field value in between from - to values. Specifies "from"
				// value: e.g value<=field.value
				if ("fromRange".equals(condition)) {
					if (filterValue instanceof Double) {
						BigDecimal rationalNumber = new BigDecimal((Double) filterValue);
						queryBuilder.addCriterion(fieldName, " >= ", rationalNumber, false);
					} else if (filterValue instanceof Number) {
						queryBuilder.addCriterion(fieldName, " >= ", filterValue, false);
					} else if (filterValue instanceof Date) {
						queryBuilder.addCriterionDateRangeFromTruncatedToDay(fieldName, ((Date) filterValue).toInstant());
					}  else if (filterValue instanceof Instant) {
						queryBuilder.addCriterionDateRangeFromTruncatedToDay(fieldName, (Instant) filterValue);
					}

					// if ranged search - field value in between from - to values. Specifies "to"
					// value: e.g field.value<=value
				} else if ("toRange".equals(condition)) {
					if (filterValue instanceof Double) {
						BigDecimal rationalNumber = new BigDecimal((Double) filterValue);
						queryBuilder.addCriterion(fieldName, " <= ", rationalNumber, false);
					} else if (filterValue instanceof Number) {
						queryBuilder.addCriterion(fieldName, " <= ", filterValue, false);
					} else if (filterValue instanceof Date) {
						queryBuilder.addCriterionDateRangeToTruncatedToDay(fieldName, ((Date) filterValue).toInstant());
					} else if (filterValue instanceof Instant) {
						queryBuilder.addCriterionDateRangeToTruncatedToDay(fieldName, (Instant) filterValue);
					}

					// Value is in field value (list)
				} else if ("list".equals(condition)) {
					String paramName = queryBuilder.convertFieldToParam(fieldName);
					queryBuilder.addSqlCriterion(":" + paramName + " in elements(" + fieldName + ")", paramName, filterValue);

					// Field value is in value (list)
				} else if ("inList".equals(condition) || "not-inList".equals(condition)) {

					boolean isNot = "not-inList".equals(condition);

					if (filterValue instanceof String) {
						queryBuilder.addSql(fieldName + (isNot ? " NOT " : "") + " IN (" + filterValue + ")");
					} else if (filterValue instanceof Collection) {
						String paramName = queryBuilder.convertFieldToParam(fieldName);
						queryBuilder.addSqlCriterion(fieldName + (isNot ? " NOT " : "") + " IN (:" + paramName + ")", paramName, filterValue);
					}

					// The value is in between two field values
				} else if ("minmaxRange".equals(condition)) {
					if (filterValue instanceof Double) {
						BigDecimal rationalNumber = new BigDecimal((Double) filterValue);
						queryBuilder.addCriterion(fieldName, " <= ", rationalNumber, false);
						queryBuilder.addCriterion(fieldName2, " >= ", rationalNumber, false);
					} else if (filterValue instanceof Number) {
						queryBuilder.addCriterion(fieldName, " <= ", filterValue, false);
						queryBuilder.addCriterion(fieldName2, " >= ", filterValue, false);
					}
					if ((filterValue instanceof Date) || (filterValue instanceof Instant)) {
						Date value = (filterValue instanceof Date ) ? (Date) filterValue:(Date.from((Instant)filterValue));
						Calendar c = Calendar.getInstance();
						c.setTime(value);
						int year = c.get(Calendar.YEAR);
						int month = c.get(Calendar.MONTH);
						int date = c.get(Calendar.DATE);
						c.set(year, month, date, 0, 0, 0);
						c.set(Calendar.MILLISECOND, 0);
						value = c.getTime();
						queryBuilder.addCriterion(fieldName, "<=", value, false);
						queryBuilder.addCriterion(fieldName2, ">=", value, false);
					}

					// The value is in between two field values with either them being optional
				} else if ("minmaxOptionalRange".equals(condition)) {

					String paramName = queryBuilder.convertFieldToParam(fieldName);

					String sql = "((" + fieldName + " IS NULL and a." + fieldName2 + " IS NULL) or (" + fieldName + "<=:" + paramName + " and :" + paramName + "<a."
							+ fieldName2 + ") or (" + fieldName + "<=:" + paramName + " and a." + fieldName2 + " IS NULL) or (a." + fieldName + " IS NULL and :" + paramName
							+ "<a." + fieldName2 + "))";
					queryBuilder.addSqlCriterionMultiple(sql, paramName, filterValue);

					// The value range is overlapping two field values with either them being
					// optional
				} else if ("overlapOptionalRange".equals(condition)) {

					String paramNameFrom = queryBuilder.convertFieldToParam(fieldName);
					String paramNameTo = queryBuilder.convertFieldToParam(fieldName2);

					String sql = "((" + fieldName + " IS NULL and a." + fieldName2 + " IS NULL) or  (" + fieldName + " IS NULL and a." + fieldName2 + ">:" + paramNameFrom
							+ ") or (a." + fieldName2 + " IS NULL and " + fieldName + "<:" + paramNameTo + ") or (" + fieldName + " IS NOT NULL and a." + fieldName2
							+ " IS NOT NULL and ((" + fieldName + "<=:" + paramNameFrom + " and :" + paramNameFrom + "<a." + fieldName2 + ") or (:" + paramNameFrom + "<=a."
							+ fieldName + " and " + fieldName + "<:" + paramNameTo + "))))";

					if (filterValue.getClass().isArray()) {
						queryBuilder.addSqlCriterionMultiple(sql, paramNameFrom, ((Object[]) filterValue)[0], paramNameTo, ((Object[]) filterValue)[1]);
					} else if (filterValue instanceof List) {
						queryBuilder.addSqlCriterionMultiple(sql, paramNameFrom, ((List) filterValue).get(0), paramNameTo, ((List) filterValue).get(1));
					}

					// Any of the multiple field values wildcard or not wildcard match the value (OR
					// criteria)
				} else if ("likeCriterias".equals(condition)) {

					queryBuilder.startOrClause();
					if (filterValue instanceof String) {
						String filterString = (String) filterValue;
						for (String field : fields) {
							queryBuilder.addCriterionWildcard(field, filterString, false);
						}
					}
					queryBuilder.endOrClause();

					// Any of the multiple field values wildcard match the value (OR criteria) - a
					// diference from "likeCriterias" is that wildcard will be appended to the value
					// automatically
				} else if (PersistenceService.SEARCH_WILDCARD_OR.equals(condition)) {
					queryBuilder.startOrClause();
					for (String field : fields) {
						queryBuilder.addSql(field + " like '%" + filterValue + "%'");
					}
					queryBuilder.endOrClause();

					// Just like wildcardOr but ignoring case :
				} else if (PersistenceService.SEARCH_WILDCARD_OR_IGNORE_CAS.equals(condition)) {
					queryBuilder.startOrClause();
					for (String field : fields) { // since SEARCH_WILDCARD_OR_IGNORE_CAS , then filterValue is necessary a String
						//lowercase functions may give different results in postgres/java => to avoid mismatch, postrges's function is the only one used. Example of mismath : Danışmanlık_İth
						queryBuilder.addSqlCriterion("lower(a." + field + ") like concat('%', lower(:" + field + "), '%')", field, filterValue);
					}
					queryBuilder.endOrClause();

					// Search by additional Sql clause with specified parameters
				} else if (PersistenceService.SEARCH_SQL.equals(condition)) {
					if (filterValue.getClass().isArray()) {
						String additionalSql = (String) ((Object[]) filterValue)[0];
						Object[] additionalParameters = Arrays.copyOfRange(((Object[]) filterValue), 1, ((Object[]) filterValue).length);
						queryBuilder.addSqlCriterionMultiple(additionalSql, additionalParameters);
					} else {
						queryBuilder.addSql((String) filterValue);
					}

				} else {
					if (filterValue instanceof String && PersistenceService.SEARCH_IS_NULL.equals(filterValue)) {
						queryBuilder.addSql(fieldName + " is null ");

					} else if (filterValue instanceof String && PersistenceService.SEARCH_IS_NOT_NULL.equals(filterValue)) {
						queryBuilder.addSql(fieldName + " is not null ");

					} else if (filterValue instanceof String) {

						// if contains dot, that means join is needed
						String filterString = (String) filterValue;
						boolean wildcard = (filterString.indexOf("*") != -1);
						if (wildcard) {
							queryBuilder.addCriterionWildcard(fieldName, filterString, false, "ne".equals(condition));
						} else {
							queryBuilder.addCriterion(fieldName, "ne".equals(condition) ? " != " : " = ", filterString, false);
						}

					} else if (filterValue instanceof Date) {
						queryBuilder.addCriterionDateTruncatedToDay(fieldName, ((Date) filterValue).toInstant());

					} else if (filterValue instanceof Instant) {
						queryBuilder.addCriterionDateTruncatedToDay(fieldName, (Instant) filterValue);

					} else if (filterValue instanceof Number || filterValue instanceof Boolean) {
						queryBuilder.addCriterion(fieldName, "ne".equals(condition) ? " != " : " = ", filterValue, false);

					} else if (filterValue instanceof Enum) {
						if (filterValue instanceof IdentifiableEnum) {
							String enumIdKey = new StringBuilder(fieldName).append("Id").toString();
							queryBuilder.addCriterion(enumIdKey, "ne".equals(condition) ? " != " : " = ", ((IdentifiableEnum) filterValue).getId(), false);
						} else {
							queryBuilder.addCriterionEnum(fieldName, (Enum) filterValue, "ne".equals(condition) ? " != " : " = ");
						}

					} else if (filterValue instanceof List) {
						queryBuilder.addSqlCriterion(fieldName + ("ne".equals(condition) ? " not in  " : " in ") + ":" + fieldName, fieldName, filterValue);
					}
				}
			}
		}

		queryBuilder.addPaginationConfiguration(config, "a");

		// FIXME: Will only works for Postgres and few others ...
		if (config.isRandomize()) {
			queryBuilder.getSqlStringBuffer().append("ORDER BY RANDOM() ");
		}

		return queryBuilder;
	}

	/**
	 * Retrieve values from a table
	 *
	 * @param tableName Table name to query
	 * @return A list of map of values with field name as map's key and field value
	 *         as map's value
	 */
	public List<Map<String, Object>> list(String sqlConnectionCode, String tableName) {

		return list(sqlConnectionCode, tableName, null);
	}

	/**
	 * Load and return the list of the records IN A MAP format from database
	 * according to sorting and paging information in
	 * {@link PaginationConfiguration} object.
	 *
	 * @param tableName A name of a table to query
	 * @param config    Data filtering, sorting and pagination criteria
	 * @return A list of map of values for each record
	 */
	public List<Map<String, Object>> list(String sqlConnectionCode, String tableName, PaginationConfiguration config) {
		QueryBuilder queryBuilder = getQuery(tableName, config);
		
		Session session = sqlStorageImpl.getHibernateSession(sqlConnectionCode);

		NativeQuery<Map<String, Object>> query = queryBuilder.getNativeQuery(session, true);
		try {
			return query.list();
		} catch (PersistenceException e) {
			if (query!= null)
				throw new PersistenceException("Error with query: " + query.getQueryString() + "\n" + 
						(queryBuilder.getParams()!=null ? "params " + queryBuilder.getParams().toString() + "\n":"")
						+ e.getMessage(), e);
			throw e;
		}
	}
	
	/**
	 * Load and return the list of the records IN A Object[] format from database
	 * according to sorting and paging information in
	 * {@link PaginationConfiguration} object.
	 *
	 * @param tableName A name of a table to query
	 * @param config    Data filtering, sorting and pagination criteria
	 * @return A list of Object[] values for each record
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> listAsObjets(String sqlConnectionCode, String tableName, PaginationConfiguration config) {

		QueryBuilder queryBuilder = getQuery(tableName, config);
		SQLQuery query = queryBuilder.getNativeQuery(getEntityManager(sqlConnectionCode), false);
		return query.list();
	}

	/**
	 * Count number of records in a database table
	 *
	 * @param tableName A name of a table to query
	 * @param config    Data filtering, sorting and pagination criteria
	 * @return Number of entities.
	 */
	public long count(String sqlConnectionCode, String tableName, PaginationConfiguration config) {
		
		QueryBuilder queryBuilder = getQuery(tableName, config);
		Session session = sqlStorageImpl.getHibernateSession(sqlConnectionCode);

		Query query = queryBuilder.getNativeCountQuery(session);
		Object count = query.getSingleResult();
		
		if (count instanceof Long) {
			return (Long) count;
			
		} else if (count instanceof BigDecimal) {
			return ((BigDecimal) count).longValue();
			
		} else if (count instanceof Integer) {
			return ((Integer) count).longValue();
			
		} else {
			return Long.valueOf(count.toString());
		}
	}

	/**
	 * Convert value of unknown data type to a target data type. A value of type
	 * list is considered as already converted value, as would come only from WS.
	 *
	 * @param value        Value to convert
	 * @param targetClass  Target data type class to convert to
	 * @param expectedList Is return value expected to be a list. If value is not a
	 *                     list and is a string a value will be parsed as comma
	 *                     separated string and each value will be converted
	 *                     accordingly. If a single value is passed, it will be
	 *                     added to a list.
	 * @param datePatterns Optional. Date patterns to apply to a date type field.
	 *                     Conversion is attempted in that order until a valid date
	 *                     is matched.If no values are provided, a standard date and
	 *                     time and then date only patterns will be applied.
	 * @return A converted data type
	 * @throws ValidationException Value can not be cast to a target class
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object castValue(Object value, Class targetClass, boolean expectedList, String[] datePatterns) throws ValidationException {

		if (StringUtils.isBlank(value)) {
			return null;
		}
		
		if(value.equals(true)) {
			return 1;
		}else if(value.equals(false)) {
			return 0;
		}

		// Nothing to cast - same data type
		if (targetClass.isAssignableFrom(value.getClass()) && !expectedList) {
			return value;
		}
		
		// A list is expected as value. If value is not a list, parse value as comma
		// separated string and convert each value separately
		if (expectedList) {
			if(value instanceof Collection) {
				Collection<?> collectionValue = (Collection<?>) value;
				
				// Convert entity references wrapper list to list of string
				{
					List<String> entityReferences = collectionValue.stream()
						.filter(EntityReferenceWrapper.class::isInstance)
						.map(EntityReferenceWrapper.class::cast)
						.map(EntityReferenceWrapper::getUuid)
						.filter(Objects::nonNull)
						.collect(Collectors.toList());
					
					if(!entityReferences.isEmpty()) {
						return entityReferences;
					}
				}
				
				// Convert entity references wrapper list to list of long
				{ 
					List<Long> entityReferences = collectionValue.stream()
							.filter(EntityReferenceWrapper.class::isInstance)
							.map(EntityReferenceWrapper.class::cast)
							.map(EntityReferenceWrapper::getId)
							.filter(Objects::nonNull)
							.collect(Collectors.toList());
					
					if(!entityReferences.isEmpty()) {
						return entityReferences;
					}
				}
				
				return value;
				
			} else if (value instanceof String) {
				try {
					// First try to parse json
					return JacksonUtil.fromString((String) value, List.class);
				} catch (Exception e) {
					// If it fails, parse comma separated string
					List valuesConverted = new ArrayList<>();
					String[] valueItems = ((String) value).split(",");
					for (String valueItem : valueItems) {
						Object valueConverted = castValue(valueItem, targetClass, false, datePatterns);
						if (valueConverted != null) {
							valuesConverted.add(valueConverted);
						} else {
							throw new ValidationException("Filter value " + value + " does not match " + targetClass.getSimpleName());
						}
					}
					return valuesConverted;

				}
			} else { // A single value list
				Object valueConverted = castValue(value, targetClass, false, datePatterns);
				if (valueConverted != null) {
					return Collections.singletonList(valueConverted);
				} else {
					throw new ValidationException("Filter value " + value + " does not match " + targetClass.getSimpleName());
				}
			}

		} else {
			if (value instanceof Collection) {
				return JacksonUtil.toString(value);
			}
		}

		Number numberVal = null;
		BigDecimal bdVal = null;
		String stringVal = null;
		Boolean booleanVal = null;
		Date dateVal = null;
		List listVal = null;

		if (value instanceof BigDecimal) {
			bdVal = (BigDecimal) value;
		} else if (value instanceof Number) {
			numberVal = (Number) value;
		} else if (value instanceof Boolean) {
			booleanVal = (Boolean) value;
		} else if (value instanceof Date) {
			dateVal = (Date) value;
		} else if (value instanceof String) {
			stringVal = (String) value;
		} else if (value instanceof List) {
			listVal = (List) value;
		} else if (value instanceof Map) {
			stringVal = JacksonUtil.toString(value);
		} else if (value instanceof File) {
			stringVal = ((File) value).getAbsolutePath();
		} else {
			throw new ValidationException("Unrecognized data type for value " + value + " type " + value.getClass());
		}

		try {
			if (targetClass == String.class) {
				if(value instanceof Map) {
					return stringVal;
				}
				
				if (stringVal != null || listVal != null) {
					return value;
				} else {
					return value.toString();
				}

			} else if (targetClass == Boolean.class || (targetClass.isPrimitive() && targetClass.getName().equals("boolean"))) {
				if (booleanVal != null) {
					return value;
				} else {
					return Boolean.parseBoolean(value.toString());
				}

			} else if (targetClass == Date.class || targetClass == Instant.class) {
				if (dateVal != null || listVal != null) {
					return value;
				} else if (numberVal != null) {
					return Instant.ofEpochMilli(numberVal.longValue());
				} else if (stringVal != null) {

					// Use provided date patterns or try default patterns if they were not provided
					if (datePatterns != null) {
						for (String datePattern : datePatterns) {
							Instant date = DateUtils.parseDateWithPattern(stringVal, datePattern);
							if (date != null) {
								return date;
							}
						}
					} else {

						// first try with date and time and then only with date format
						Instant date = DateUtils.parseDateWithPattern(stringVal, DateUtils.DATE_TIME_PATTERN);
						if (date == null) {
							date = DateUtils.parseDateWithPattern(stringVal, paramBean.getDateTimeFormat());
						}
						if (date == null) {
							date = DateUtils.parseDateWithPattern(stringVal, DateUtils.DATE_PATTERN);
						}
						if (date == null) {
							date = DateUtils.parseDateWithPattern(stringVal, paramBean.getDateFormat());
						}
						return date;
					}
				}

			} else if (targetClass.isEnum()) {
				if (listVal != null || targetClass.isAssignableFrom(value.getClass())) {
					return value;
				} else if (stringVal != null) {
					Enum enumVal = ReflectionUtils.getEnumFromString((Class<? extends Enum>) targetClass, stringVal);
					if (enumVal != null) {
						return enumVal;
					}
				}

			} else if (targetClass == Integer.class || (targetClass.isPrimitive() && targetClass.getName().equals("int"))) {
				if (numberVal != null || bdVal != null || listVal != null) {
					return value;
				} else if (stringVal != null) {
					return Integer.parseInt(stringVal);
				}

			} else if (targetClass == Long.class || (targetClass.isPrimitive() && targetClass.getName().equals("long"))) {
				if (numberVal != null || bdVal != null || listVal != null) {
					return value;
				} else if (stringVal != null) {
					return Long.parseLong(stringVal);
				}

			} else if (targetClass == Byte.class || (targetClass.isPrimitive() && targetClass.getName().equals("byte"))) {
				if (numberVal != null || bdVal != null || listVal != null) {
					return value;
				} else if (stringVal != null) {
					return Byte.parseByte(stringVal);
				}

			} else if (targetClass == Short.class || (targetClass.isPrimitive() && targetClass.getName().equals("short"))) {
				if (numberVal != null || bdVal != null || listVal != null) {
					return value;
				} else if (stringVal != null) {
					return Short.parseShort(stringVal);
				}

			} else if (targetClass == Double.class || (targetClass.isPrimitive() && targetClass.getName().equals("double"))) {
				if (numberVal != null || bdVal != null || listVal != null) {
					return value;
				} else if (stringVal != null) {
					return Double.parseDouble(stringVal);
				}

			} else if (targetClass == Float.class || (targetClass.isPrimitive() && targetClass.getName().equals("float"))) {
				if (numberVal != null || bdVal != null || listVal != null) {
					return value;
				} else if (stringVal != null) {
					return Float.parseFloat(stringVal);
				}

			} else if (targetClass == BigDecimal.class) {
				if (numberVal != null || bdVal != null || listVal != null) {
					return value;
				} else if (stringVal != null) {
					return new BigDecimal(stringVal);
				}

			}

		} catch (NumberFormatException e) {
			// Swallow - validation will take care of it later
		}

		return value;
	}

	/**
	 * Sets the parameter of the given index in {@link PreparedStatement}.
	 * 
	 * @param ps             the {@link PreparedStatement} class
	 * @param parameterIndex parameter index, starts with 1
	 * @param value          the value of the parameter
	 * @throws SQLException error assigning the parameter value
	 */
	protected void setParameterValue(PreparedStatement ps, int parameterIndex, Object value) throws SQLException {
		if(value instanceof Map) {
			value = JacksonUtil.toString(value);
		} else if (value instanceof Collection) {
			value = JacksonUtil.toString(value);
		} else if (value instanceof File) {
			value = ((File) value).getAbsolutePath();
		} if(value instanceof EntityReferenceWrapper) {
			EntityReferenceWrapper erw = (EntityReferenceWrapper) value;
			if (customFieldTemplateService.isReferenceJpaEntity(erw.getClassnameCode())) {
				value = erw.getId();
			
			} else {
				value = erw.getUuid();
			}
		}

		if (value instanceof String) {
			ps.setString(parameterIndex, (String) value);

		} else if (value instanceof Date) {
			Date date = (Date) value;
			ps.setDate(parameterIndex, new java.sql.Date(date.getTime()));
			
		} else if (value instanceof LocalDateTime) {
			LocalDateTime date = LocalDateTime.parse(value.toString());
			ps.setTimestamp(parameterIndex, Timestamp.valueOf(date));
			
		} else if (value instanceof LocalDate) {
			LocalDate date = LocalDate.parse(value.toString());
			ps.setDate(parameterIndex, java.sql.Date.valueOf(date));

		} else if(value instanceof Instant)  { 
			Instant instant = (Instant) value;
			ps.setTimestamp(parameterIndex, new Timestamp(instant.toEpochMilli()));
			
		} else if (value instanceof Long) {
			ps.setLong(parameterIndex, (Long) value);

		} else if (value instanceof Double) {
			ps.setDouble(parameterIndex, (Double) value);

		} else if (value instanceof Boolean) {
			ps.setBoolean(parameterIndex, (Boolean) value);
			
		} else if (value instanceof Integer) {
			ps.setInt(parameterIndex, (Integer) value);
		}
	}

	public List<Map<String, Object>> list(String sqlConnectionCode, CustomEntityTemplate cet, PaginationConfiguration config) {
		throw new NotImplementedException();
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
            valuesConverted.put(FIELD_ID, castValue(uuid, String.class, false, datePatterns));
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
                    		.filter(f -> f.getDbFieldname().equals(fieldName) || f.getCode().equals(fieldName))
                    		.findFirst();

                    if(!customFieldTemplateOpt.isPresent()){
                    	continue;
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

					if (!customFieldTemplate.isPersisted()) {
						value = null;
					}
                    
                    // Replace cft code with db field name if needed
                    String dbFieldname = CustomFieldTemplate.getDbFieldname(fieldName);
                    if (!fieldName.equalsIgnoreCase(dbFieldname)) {
                        key = key.replaceAll(fieldName, dbFieldname);
                    }
                    valuesConverted.put(key, value);
                }
            }

        }
        return valuesConverted;
    }
    
    protected Map<String, Object> serializeValues(Map<String, Object> values, Map<String, CustomFieldTemplate> fields) {
    	Map<String, Object> serializedValues = new HashMap<>(values);
    	
    	values.forEach((k,v) -> {
    		if(v instanceof Collection && !((Collection<?>) v).isEmpty()) {
    			Collection<?> collection = (Collection<?>) v;
    			Object firstItem = collection.iterator().next();
    			
    			if(firstItem instanceof EntityReferenceWrapper) {
    				// Only store UUIDs
    				List<String> uuids = collection.stream()
    						.map(EntityReferenceWrapper.class::cast)
    						.map(EntityReferenceWrapper::getUuid)
    						.collect(Collectors.toList());
    				serializedValues.put(k, uuids);
    			}
    			
    		} else if(v instanceof EntityReferenceWrapper) {
    			if (!StringUtils.isBlank(((EntityReferenceWrapper) v).getUuid())) {
					serializedValues.put(k, ((EntityReferenceWrapper) v).getUuid());
				} else {
					serializedValues.put(k, ((EntityReferenceWrapper) v).getId());
				}
    			
    		}
    	});
    	
    	return serializedValues;
    }
}

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

package org.meveo.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJBException;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.IllegalTransitionException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.elresolver.ELException;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.exceptions.InvalidCustomFieldException;
import org.meveo.model.CustomEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.EntityReferenceWrapper;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.CEIUtils;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.graphql.GraphQLQueryBuilder;
import org.meveo.persistence.neo4j.base.Neo4jDao;
import org.meveo.persistence.neo4j.service.Neo4jService;
import org.meveo.persistence.scheduler.EntityRef;
import org.meveo.security.PasswordUtils;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomTableRelationService;
import org.meveo.service.custom.CustomTableService;
import org.meveo.service.storage.FileSystemService;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import org.slf4j.Logger;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @author clement.bareth
 * @version 6.11.0
 */
// @Stateless
// @LocalBean
// @TransactionManagement(TransactionManagementType.BEAN)
public class CrossStorageService implements CustomPersistenceService {

//	@Resource
//	private UserTransaction transaction;
	
	@Inject
	private CrossStorageTransaction transaction;

	@Inject
	private CustomFieldsCacheContainerProvider cache;

	@Inject
	private Neo4jService neo4jService;

	@Inject
	private Neo4jDao neo4jDao;

	@Inject
	private CustomTableService customTableService;

	@Inject
	private CustomEntityInstanceService customEntityInstanceService;

	@Inject
	private CustomTableRelationService customTableRelationService;

	@Inject
	private CustomFieldTemplateService customFieldTemplateService;

	@Inject
	private FileSystemService fileSystemService;

	@Inject
	private CustomFieldInstanceService customFieldInstanceService;

	@Inject
	private Logger log;
	
	@Inject
	private CustomEntityTemplateService customEntityTemplateService;
	
    @Inject
    @Updated
    private Event<CustomEntityInstance> customEntityInstanceUpdate;
    
    @Inject
    @Created
    private Event<CustomEntityInstance> customEntityInstanceCreate;
    
    @Inject
    @Removed
    private Event<CustomEntityInstance> customEntityInstanceDelete;

	/**
	 * Retrieves one entity instance
	 *
	 * @param repository     Repository code
	 * @param cet            Template of the entities to retrieve
	 * @param uuid           UUID of the entity
	 * @param withReferences Whether to fetch entity references
	 * @return list of matching entities
	 * @throws EntityDoesNotExistsException if entity can't be found
	 */
	public Map<String, Object> find(Repository repository, CustomEntityTemplate cet, String uuid, boolean withReferences) throws EntityDoesNotExistsException {
		return find(repository, cet, uuid, null, new HashMap<>(), true);
	}
	
	public Map<String, Object> find(Repository repository, CustomEntityTemplate cet, String uuid, Collection<String> fetchFields, boolean withEntityReferences) throws EntityDoesNotExistsException {
		return find(repository, cet, uuid, fetchFields, new HashMap<>(), withEntityReferences);
	}

	/**
	 * Retrieves one entity instance
	 *
	 * @param repository           Repository code
	 * @param cet                  Template of the entities to retrieve
	 * @param uuid                 UUID of the entity
	 * @param fetchFields          Fields to select
	 * @param withEntityReferences Whether to fetch entity references
	 * @return list of matching entities
	 * @throws EntityDoesNotExistsException if entity does not exist
	 */
	public Map<String, Object> find(Repository repository, CustomEntityTemplate cet, String uuid, Collection<String> fetchFields, Map<String, Set<String>> subFields, boolean withEntityReferences) throws EntityDoesNotExistsException {
		if (uuid == null) {
			throw new IllegalArgumentException("Cannot retrieve entity by uuid without uuid");
		}

		if (cet == null) {
			throw new IllegalArgumentException("CET should be provided");
		}

		List<String> selectFields;
		Map<String, Object> values = new HashMap<>();
		values.put("uuid", uuid);
		boolean foudEntity=false;
		
		Collection<CustomFieldTemplate> cfts = customFieldTemplateService.getCftsWithInheritedFields(cet).values();

		// Retrieve only asked fields
		if (fetchFields != null && !fetchFields.isEmpty()) {
			selectFields = new ArrayList<>(fetchFields);

		// No restrictions about fields - retrieve all fields
		} else {
			selectFields = cfts.stream().map(CustomFieldTemplate::getCode).collect(Collectors.toList());
		}

		if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
			List<String> neo4jFields = filterFields(selectFields, cet, DBStorageType.NEO4J);
			if (!neo4jFields.isEmpty()) {
				try {
					String repoCode = repository.getNeo4jConfiguration().getCode();
					final Map<String, Object> existingValues = neo4jDao.findNodeById(repoCode, cet.getCode(), uuid, neo4jFields);
					if (existingValues != null) {
						foudEntity=true;
						values.putAll(existingValues);
						// We need to fetch every relationship defined as entity references
						if(withEntityReferences || fetchFields != null) {
							for(CustomFieldTemplate cft : cfts) {
								if(!withEntityReferences && fetchFields != null) { // Skip fields not defined in fetch fields
									if(!fetchFields.contains(cft.getCode())) {
										continue;
									}
								}
								
								if(cft.getStoragesNullSafe().contains(DBStorageType.NEO4J) && cft.getFieldType() == CustomFieldTypeEnum.ENTITY) {
									var referencedCet = cache.getCustomEntityTemplate(cft.getEntityClazzCetCode());
									
									// Don't fetch primitive entities
									if(referencedCet.getNeo4JStorageConfiguration() != null && referencedCet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {
										continue;
									}
									
									if(cft.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
										List<Map<String, Object>> targets = neo4jDao.findTargets(repoCode, uuid, cet.getCode(), cft.getRelationshipName(), referencedCet.getCode());
										values.put(cft.getCode(), targets);
										for (var target : targets) {
											if(target.get("uuid") == null) {
												target.put("uuid", target.remove("meveo_uuid"));
											}
										}
										
									} else {
										Map<String, Object> target = neo4jDao.findTarget(repoCode, uuid, cet.getCode(), cft.getRelationshipName(), referencedCet.getCode());
										if(target != null) {
											values.put(cft.getCode(), target);
											if(target.get("uuid") == null) {
												target.put("uuid", target.remove("meveo_uuid"));
											} 
										} else {
											log.warn("Failed to retrieve target : [source={}/{}, relationship={}, target={}", uuid, cet.getCode(), cft.getRelationshipName(), referencedCet.getCode());
										}
									}
								}
							}
						}
					}
					
				} catch (EJBException e) {
					if (e.getCausedByException() instanceof NoSuchRecordException) {
						throw new EntityDoesNotExistsException(cet.getCode() + " instance with UUID : " + uuid + " does not exist in NEO4J");
					}
				}
			}
		}

		// Don't retrieve the fields we already fetched
		selectFields.removeAll(values.keySet());

		try {
			// transaction.begin();
			transaction.beginTransaction(repository);

			if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
				List<String> sqlFields = filterFields(selectFields, cet, DBStorageType.SQL);
				if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
					final Map<String, Object> customTableValue = customTableService.findById(repository.getSqlConfigurationCode(), cet, uuid, sqlFields);
					replaceKeys(cet, sqlFields, customTableValue);
					if(customTableValue != null) {
						foudEntity=true;
						values.putAll(customTableValue);
					}
				} else {
					final CustomEntityInstance cei = customEntityInstanceService.findByUuid(cet.getCode(), uuid);
					if (cei == null) {
						return null;
					}

					values.put("code", cei.getCode());
					values.put("description", cei.getDescription());
					foudEntity=true;
					if (sqlFields != null) {
						for (String field : sqlFields) {
							if (cei.getCfValues() != null && cei.getCfValues().getCfValue(field) != null) {
								values.putIfAbsent(field, cei.getCfValues().getCfValue(field).getValue());
							}
						}
					} else {
						if (cei.getCfValuesAsValues() != null) {
							values.putAll(cei.getCfValuesAsValues());
						}
					}
				}
			}

			transaction.commitTransaction(repository);
		} catch (Exception e) {
			
			transaction.rollbackTransaction(e);

			if(e instanceof EntityDoesNotExistsException) {
				throw (EntityDoesNotExistsException) e;
			} else {
				log.error("Can't retrieve data stored in SQL", e);
			}
			
			throw new RuntimeException(e);
		}

		if(!foudEntity){
			throw new EntityDoesNotExistsException(cet.getCode(),uuid);
		}
		
		// Remove null values
		values.values().removeIf(Objects::isNull);

		// Fetch entity references
		fetchEntityReferences(repository, cet, values, subFields);

		values = deserializeData(values, cfts);
		
		return values;
	}

	/**
	 * Completed the data spread across multiple storage
	 * 
	 * @param data the actual data
	 * @param repository the repository to get data from
	 * @param cet data type
	 * @param uuid id of the record
	 * @param selectFields the field to fetch
	 * @return the data completed
	 * @throws EntityDoesNotExistsException if data with given id can't be found
	 */
	public Map<String, Object> getMissingData(Map<String, Object> data, Repository repository, CustomEntityTemplate cet, String uuid, Collection<String> selectFields, Map<String, Set<String>> subFields) throws EntityDoesNotExistsException {
		List<String> actualFetchField = new ArrayList<>(selectFields);
		actualFetchField.removeIf(entry -> data.keySet().contains(entry) && !subFields.keySet().contains(entry));

		// Every field has been already retrieved
		if (actualFetchField.isEmpty()) {
			return new HashMap<>();
		}

		// Retrieve the missing fields
		return find(repository, cet, uuid, actualFetchField, subFields, false);
	}

	/**
	 * Retrieves entity instances
	 *
	 * @param repository              Repository code
	 * @param cet                     Template of the entities to retrieve
	 * @param paginationConfiguration Pagination and filters
	 * @return list of matching entities
	 * @throws EntityDoesNotExistsException if data with given id can't be found
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> find(Repository repository, CustomEntityTemplate cet, PaginationConfiguration paginationConfiguration) throws EntityDoesNotExistsException {
		final Map<String, CustomFieldTemplate> fields = customFieldTemplateService.getCftsWithInheritedFields(cet);

		final Set<String> actualFetchFields;
		// If no pagination nor fetch fields are defined, we consider that we must fetch everything
		boolean fetchAllFields = paginationConfiguration == null || paginationConfiguration.getFetchFields() == null || paginationConfiguration.getFetchFields().isEmpty();
		if(fetchAllFields) {
			actualFetchFields = new HashSet<>(fields.keySet());
		} else {
			actualFetchFields = new HashSet<>(paginationConfiguration.getFetchFields());
		}
		
		final Map<String, Set<String>> subFields = extractSubFields(actualFetchFields);
		
		final Map<String, Object> filters = paginationConfiguration == null ? null : paginationConfiguration.getFilters();

		final List<Map<String, Object>> valuesList = new ArrayList<>();
		
		// In case where only graphql query is passed, we will only use it so we won't
		// fetch sql
		boolean dontFetchSql = paginationConfiguration != null && paginationConfiguration.getFilters() == null && paginationConfiguration.getGraphQlQuery() != null;

		boolean hasSqlFetchField = paginationConfiguration != null && actualFetchFields != null && actualFetchFields.stream().anyMatch(s -> customTableService.sqlCftFilter(cet, s));

		boolean hasSqlFilter = paginationConfiguration != null && paginationConfiguration.getFilters() != null && filters.keySet().stream().anyMatch(s -> customTableService.sqlCftFilter(cet, s));

		// Make sure the filters matches the fields
		if(filters != null) {
			filters.keySet()
				.forEach(key -> {
					String[] fieldInfo = key.split(" ");
					String fieldName = fieldInfo.length == 1 ? fieldInfo[0] : fieldInfo[1];
					if(fields.get(fieldName) == null && !"uuid".equals(fieldName) && !("code".equals(fieldName) && cet.getAvailableStorages().contains(DBStorageType.SQL) && !cet.getSqlStorageConfiguration().isStoreAsTable())) {
						throw new IllegalArgumentException("Filter " + key + " does not match fields of " + cet.getCode());
					}
				});
		}
		
		// Collect initial data
		if (cet.getAvailableStorages() != null && cet.getAvailableStorages().contains(DBStorageType.SQL) && !dontFetchSql && (fetchAllFields || hasSqlFetchField || hasSqlFilter)) {
			PaginationConfiguration sqlPaginationConfiguration = new PaginationConfiguration(paginationConfiguration);
			sqlPaginationConfiguration.setFetchFields(List.copyOf(actualFetchFields));
			
			if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
				final List<Map<String, Object>> values = customTableService.list(repository.getSqlConfigurationCode(), cet, sqlPaginationConfiguration);
				values.forEach(v -> replaceKeys(cet, actualFetchFields, v));
				valuesList.addAll(values);

			} else {
				final List<CustomEntityInstance> ceis = customEntityInstanceService.list(cet.getCode(), cet.isStoreAsTable(), filters, sqlPaginationConfiguration);
				final List<Map<String, Object>> values = new ArrayList<>();

				for (CustomEntityInstance cei : ceis) {
					Map<String, Object> cfValuesAsValues = cei.getCfValuesAsValues();
					final Map<String, Object> map = cfValuesAsValues == null ? new HashMap<>() : new HashMap<>(cfValuesAsValues);
					map.put("uuid", cei.getUuid());
					map.put("code", cei.getCode());
					map.put("description", cei.getDescription());

					if (!fetchAllFields) {
						for (String k : cei.getCfValuesAsValues().keySet()) {
							if (!actualFetchFields.contains(k)) {
								map.remove(k);
							}
						}
					}
					values.add(map);
				}

				valuesList.addAll(values);
			}
		}

		if (cet.getAvailableStorages() != null && cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
			String graphQlQuery;
			Map<String, Object> graphQlVariables = new HashMap<>();

			// Find by graphql if query provided
			if (paginationConfiguration != null && paginationConfiguration.getGraphQlQuery() != null) {
				graphQlQuery = paginationConfiguration.getGraphQlQuery();
				graphQlQuery = graphQlQuery.replaceAll("([\\w)]\\s*\\{)(\\s*\\w*)", "$1meveo_uuid,$2");
			} else {
				graphQlQuery = generateGraphQlFromPagination(cet.getCode(), paginationConfiguration, actualFetchFields, filters, subFields)
						.toString();
			}
			
			// Check if filters contains a field not stored in Neo4J
			var dontFilterOnNeo4J = filters != null && filters.keySet().stream()
					.anyMatch(f -> fields.get(f) != null && !fields.get(f).getStorages().contains(DBStorageType.NEO4J));
				
			Map<String, Object> result = null;
			if (!dontFilterOnNeo4J && repository.getNeo4jConfiguration() != null) {
				result = neo4jDao.executeGraphQLQuery(repository.getNeo4jConfiguration().getCode(), graphQlQuery, null, null);
			}
			
			if(result != null) {
				List<Map<String, Object>> values = (List<Map<String, Object>>) result.get(cet.getCode());
				values = values != null ? values : new ArrayList<>();

				values.forEach(map -> {
					final HashMap<String, Object> resultMap = new HashMap<>(map);
					map.forEach((key, mapValue) -> {
						if (!key.equals("uuid") && !key.equals("meveo_uuid") && actualFetchFields != null && !actualFetchFields.contains(key)) {
							resultMap.remove(key);
						}

						// Flatten primitive types and Binary values (singleton maps with only "value"
						// and optionally "meveo_uuid" attribute)
						if (mapValue instanceof Map && ((Map<?, ?>) mapValue).size() == 1 && ((Map<?, ?>) mapValue).containsKey("value")) {
							Object value = ((Map<?, ?>) mapValue).get("value");
							resultMap.put(key, value);
						} else if (mapValue instanceof Map && ((Map<?, ?>) mapValue).size() == 2 && ((Map<?, ?>) mapValue).containsKey("value") && ((Map<?, ?>) mapValue).containsKey("meveo_uuid")) {
							Object value = ((Map<?, ?>) mapValue).get("value");
							resultMap.put(key, value);
						}
					});

					// Rewrite "meveo_uuid" to "uuid"
					if (resultMap.get("meveo_uuid") != null) {
						resultMap.put("uuid", resultMap.remove("meveo_uuid"));
					}

					// merge the values from sql and neo4j
					// valuesList.add(resultMap);
					mergeData(valuesList, resultMap);
				});
			}
		}

		// Complete missing data
		for (Map<String, Object> data : valuesList) {
			String uuid = (String) (data.get("uuid") != null ? data.get("uuid") : data.get("meveo_uuid"));

			final Map<String, Object> missingData = getMissingData(data, repository, cet, uuid, actualFetchFields, subFields);
			if(missingData != null) {
				data.putAll(missingData);
			}
		}
		
		return valuesList.stream()
				.map(values -> deserializeData(values, fields.values()))
				.collect(Collectors.toList());
	}

	/**
	 * @param cet
	 * @param paginationConfiguration
	 * @param actualFetchFields
	 * @param filters
	 * @return
	 */
	private GraphQLQueryBuilder generateGraphQlFromPagination(String type, PaginationConfiguration paginationConfiguration, final Set<String> actualFetchFields, final Map<String, Object> filters, Map<String, Set<String>> subFields) {
		GraphQLQueryBuilder builder = GraphQLQueryBuilder.create(type);
		builder.field("meveo_uuid");
		if(filters != null) {
			filters.forEach((key, value) -> {
				if(!"**".equals(value)) { //FIXME: Dirty hack, must be fixed at higher level
					if (!key.equals("uuid")) {
						builder.filter(key, value);
					} else {
						builder.filter("meveo_uuid", value);
					}
				}
			});
		}
		
		if(actualFetchFields != null) { 
			actualFetchFields
				.stream()
				.filter(field -> !field.equals("uuid"))
				.filter(field -> !subFields.containsKey(field))
				.forEach(builder::field);
		}
		
		if(paginationConfiguration != null) {
			if (paginationConfiguration.getNumberOfRows() != null) {
				builder.limit(paginationConfiguration.getNumberOfRows());
			}
			
			if (paginationConfiguration.getFirstRow() != null) {
				builder.offset(paginationConfiguration.getFirstRow());
			}
		}
		
		for (var subField : subFields.entrySet()) {
			Set<String> subFetchFields = new HashSet<>(subField.getValue());
			Map<String, Set<String>> subSubFields = extractSubFields(subFetchFields);
			GraphQLQueryBuilder subQuery = generateGraphQlFromPagination(null, null, subFetchFields, null, subSubFields);
			builder.field(subField.getKey(), subQuery);
		}
		
		return builder;
	}

	/**
	 * @param actualFetchFields
	 * @return
	 */
	public Map<String, Set<String>> extractSubFields(final Set<String> actualFetchFields) {
		final Map<String, Set<String>> subFields = new HashMap<>();
		for (var fetchField : List.copyOf(actualFetchFields)) {
			if(fetchField.contains(".")) {
				actualFetchFields.remove(fetchField);
				String[] fieldQuery = fetchField.split("\\.", 2);
				actualFetchFields.add(fieldQuery[0]);
				subFields.computeIfAbsent(fieldQuery[0], key -> new HashSet<>())
					.add(fieldQuery[1]);
			}
		}
		return subFields;
	}

	/**
	 * Add or merge a map of data to a list.
	 * 
	 * @param valuesList list of map of values
	 * @param resultMap  map to merge or add
	 */
	private void mergeData(List<Map<String, Object>> valuesList, HashMap<String, Object> resultMap) {

		String uuid = (String) (resultMap.get("uuid") != null ? resultMap.get("uuid") : resultMap.get("meveo_uuid"));

		boolean found = false;
		for (Map<String, Object> mapOfValues : valuesList) {
			String uuid2 = (String) (mapOfValues.get("uuid") != null ? mapOfValues.get("uuid") : mapOfValues.get("meveo_uuid"));
			if (uuid.equals(uuid2)) {
				mapOfValues.putAll(resultMap);
				found = true;
				break;
			}
		}

		if (!found) {
			valuesList.add(resultMap);
		}
	}

	/**
	 * Count the number of record for a given pagination
	 * 
	 * @param repository              the repository
	 * @param cet                     the data type
	 * @param paginationConfiguration the pagination
	 * @return the number of record
	 */
	public int count(Repository repository, CustomEntityTemplate cet, PaginationConfiguration paginationConfiguration) {

		final List<String> actualFetchFields = paginationConfiguration == null ? null : paginationConfiguration.getFetchFields();

		final Map<String, Object> filters = paginationConfiguration == null ? null : paginationConfiguration.getFilters();

		// If no pagination nor fetch fields are defined, we consider that we must fetch
		// everything
		boolean fetchAllFields = paginationConfiguration == null || actualFetchFields == null;

		// In case where only graphql query is passed, we will only use it so we won't
		// fetch sql
		boolean dontFetchSql = paginationConfiguration != null && paginationConfiguration.getFilters() == null && paginationConfiguration.getGraphQlQuery() != null;

		boolean hasSqlFetchField = paginationConfiguration != null && actualFetchFields != null && actualFetchFields.stream().anyMatch(s -> customTableService.sqlCftFilter(cet, s));

		boolean hasSqlFilter = paginationConfiguration != null && paginationConfiguration.getFilters() != null && filters.keySet().stream().anyMatch(s -> customTableService.sqlCftFilter(cet, s));

		if (cet.getAvailableStorages() != null && cet.getAvailableStorages().contains(DBStorageType.SQL) && !dontFetchSql && (fetchAllFields || hasSqlFetchField || hasSqlFilter)) {
			final String dbTablename = SQLStorageConfiguration.getDbTablename(cet);

			if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
				return (int) customTableService.count(repository.getSqlConfigurationCode(), dbTablename, paginationConfiguration);

			} else {
				return (int) customEntityInstanceService.count(cet.getCode(), paginationConfiguration);
			}

		}

		if (cet.getAvailableStorages() != null && cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
			return neo4jService.count(repository, cet, paginationConfiguration);
		}

		return 0;
	}

	/**
	 * Insert / update an entity with the sourceValues. The target entity is retrieved from the target values. <br>
	 * If the target entity does not exist, create the source node. <br>
	 * If the target entity and the relation exists, update the source entity with the source values. <br>
	 * If the target entity exists but not the relation, create the relation along with the source entity.
	 *
	 * @param repository   Code of the repository / configuration to store the data. <br>
	 *                     NOTE : only available for NEO4J at the moment.
	 * @param relationCode Code of the relation to create
	 * @param sourceValues Values to insert
	 * @param targetValues Filters on target entity
	 */
	@Override
	public PersistenceActionResult addSourceEntityUniqueCrt(Repository repository, String relationCode, Map<String, Object> sourceValues, Map<String, Object> targetValues) throws ELException, BusinessException, IOException, BusinessApiException, EntityDoesNotExistsException {
		CustomRelationshipTemplate crt = cache.getCustomRelationshipTemplate(relationCode);
		var cfts = cache.getCustomFieldTemplates(crt.getAppliesTo());

		if (!crt.isUnique()) {
			throw new IllegalArgumentException("CRT must be unique !");
		}

		final CustomEntityTemplate endNode = crt.getEndNode();
		final CustomEntityTemplate startNode = crt.getStartNode();

		// Everything is stored in Neo4J
		if (isEverythingStoredInNeo4J(crt)) {
			return neo4jService.addSourceNodeUniqueCrt(
					repository.getNeo4jConfiguration().getCode(),
					relationCode,
					filterValues(cfts, sourceValues, crt, DBStorageType.NEO4J),
					filterValues(cfts, targetValues, crt, DBStorageType.NEO4J));
		}

		String targetUUUID = findEntityId(repository, targetValues, endNode);

		// Target does not exists. We create the source
		if (targetUUUID == null) {
			CustomEntityInstance cei = new CustomEntityInstance();
			cei.setCetCode(startNode.getCode());
			cei.setRepository(repository);
			customFieldInstanceService.setCfValues(cei, startNode.getCode(), sourceValues);

			return createOrUpdate(repository, cei);

		} else {
			// Target exists. Let's check if the relation exist.
			final String relationUUID = findUniqueRelationByTargetUuid(repository, targetUUUID, crt);

			CustomEntityInstance cei = new CustomEntityInstance();
			cei.setRepository(repository);
			cei.setCetCode(startNode.getCode());
			cei.setCet(cache.getCustomEntityTemplate(startNode.getCode()));
			customFieldInstanceService.setCfValues(cei, startNode.getCode(), sourceValues);

			// Relation does not exists. We create the source.
			if (relationUUID == null) {
				return createOrUpdate(repository, cei);

			} else {
				// Relation exists. We update the source node.
				String sourceUUID = findIdOfSourceEntityByRelationId(repository, relationUUID, crt);
				cei.setUuid(sourceUUID);
				update(repository, cei);

				return new PersistenceActionResult(sourceUUID);
			}
		}
	}

	private boolean isEverythingStoredInNeo4J(CustomRelationshipTemplate crt) {
		return crt.getAvailableStorages().contains(DBStorageType.NEO4J) && crt.getStartNode().getAvailableStorages().contains(DBStorageType.NEO4J) && crt.getEndNode().getAvailableStorages().contains(DBStorageType.NEO4J);
	}

	/**
	 * Create or update an entity and enventyally entity references that it holds
	 *
	 * @param repository Code of the repository / configuration to store the data. <br>
	 *                   NOTE : only available for NEO4J at the moment.
	 * @param ceiToSave  the {@link CustomEntityInstance}
	 * @return the persisted entites
	 */
	@Override
	public PersistenceActionResult createOrUpdate(Repository repository, CustomEntityInstance ceiToSave) throws BusinessException, IOException, BusinessApiException, EntityDoesNotExistsException {
		if (repository == null) {
			throw new IllegalArgumentException("Repository should be provided");
		}
		
		// Retrieve corresponding CET
		CustomEntityTemplate cet = cache.getCustomEntityTemplate(ceiToSave.getCetCode());
		if(cet == null) {
			throw new IllegalArgumentException("CET with code " + ceiToSave.getCetCode() + " does not exist");
		}
				
		Set<EntityRef> persistedEntities = new HashSet<>();

		// Initialize the CEI that will be manipulated
		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setCetCode(ceiToSave.getCetCode());
		cei.setCode(ceiToSave.getCode());
		cei.setDescription(ceiToSave.getDescription());
		cei.setCfValuesOld(ceiToSave.getCfValuesOld());
		cei.setRepository(repository);
		
		if (ceiToSave.getCfValuesOld() != null && !ceiToSave.getCfValuesOld().getValuesByCode().isEmpty()) {
			try {
				checkBeforeUpdate(repository, ceiToSave);
			} catch (EntityDoesNotExistsException | ELException e) {
				throw new BusinessException(e);
			}
		}
		
		if(ceiToSave.getUuid() != null) {
			cei.setUuid(ceiToSave.getUuid());
		}
				
		final Map<String, CustomFieldTemplate> customFieldTemplates =  (cet.getSuperTemplate() == null ? cache.getCustomFieldTemplates(cet.getAppliesTo()) : customFieldTemplateService.getCftsWithInheritedFields(cet));
		cei.setCet(cet);

		// Create referenced entities and set UUIDs in the values
		Map<String, Object> tmpValues = ceiToSave.getCfValuesAsValues() != null ? new HashMap<>(ceiToSave.getCfValuesAsValues()) : new HashMap<>();
		Map<String, Object> entityValues = createEntityReferences(repository, tmpValues, cet);
		customFieldInstanceService.setCfValues(cei, cet.getCode(), entityValues);

		String uuid = null;

		// First check if data exist, in order to synchronize UUID across all storages
		String foundId;
		
		List<String> secretFields = customFieldTemplates.values()
			.stream()
			.filter(cft -> cft.getFieldType() == CustomFieldTypeEnum.SECRET)
			.filter(cft -> cei.get(cft.getCode()) != null)
			.map(CustomFieldTemplate::getCode)
			.collect(Collectors.toList());
		
		try {
			foundId = findEntityId(repository, cei);
		} catch (IllegalArgumentException e) {
			// It's no problem if we can't retrieve record using values - we consider it does not exist
			foundId = null;
		}
		
		if (foundId != null) {
			cei.setUuid(foundId);
			// Handle secret fields
			if(!secretFields.isEmpty()) {
				var foundCei = CEIUtils.pojoToCei(find(repository, cet, foundId, false));
				String oldCeiHash = CEIUtils.getHash(foundCei, customFieldTemplates);
				secretFields.forEach(secretField -> {
					// Secret field has not changed, decrypt it so it will be correctly re-encrypted
					if(cei.get(secretField).equals(foundCei.get(secretField))) {
						String decryptedValue = PasswordUtils.decryptNoSecret(oldCeiHash, cei.get(secretField));
						cei.getCfValues().setValue(secretField, decryptedValue);
						entityValues.put(secretField, decryptedValue);
					}
				});
			}
		}
		
		// Encrypt secret fields
		if(!secretFields.isEmpty()) {
			String ceiHash = CEIUtils.getHash(cei, customFieldTemplates);
			secretFields.forEach(secretField -> {
				String secretValue = cei.get(secretField);
				if(!secretValue.startsWith("🔒")) {	// Value is already encrypted
					String encryptedValue = PasswordUtils.encryptNoSecret(ceiHash, secretValue);
					cei.getCfValues().setValue(secretField, encryptedValue);
					entityValues.put(secretField, encryptedValue);
				}
			});
		}
		
		var listener = customEntityTemplateService.loadCrudEventListener(cei.getCet());
		CustomEntity cetClassInstance = null;
		
		if(listener != null) {
			var cetClass =  listener.getEntityClass();
			cetClassInstance = CEIUtils.ceiToPojo(cei, cetClass);
		}
		
		CustomEntityInstance ceiAfterPreEvents = cei;
		
		try {
			// transaction.begin();
			transaction.beginTransaction(repository);
			
			if(cetClassInstance != null) {
				if(foundId != null) {
					listener.preUpdate(cetClassInstance);
				} else {
					listener.prePersist(cetClassInstance);
				}
				
				ceiAfterPreEvents = CEIUtils.pojoToCei(cetClassInstance);	// Handle case where entity was modified
				ceiAfterPreEvents.setCet(cei.getCet());
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// NEO4J Storage
		if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
			uuid = createOrUpdateNeo4J(repository, ceiAfterPreEvents, customFieldTemplates, persistedEntities, foundId);
			if (foundId == null) {
				ceiAfterPreEvents.setUuid(uuid);
			}
		}

		// SQL Storage
		if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
			Map<String, Object> sqlValues = filterValues(customFieldTemplates, ceiAfterPreEvents.getCfValuesAsValues(), cet, DBStorageType.SQL, false);

			if (!sqlValues.isEmpty() || !cet.getSqlStorageConfiguration().isStoreAsTable()) {
				CustomEntityInstance sqlCei = new CustomEntityInstance();
				sqlCei.setCet(cet);
				sqlCei.setUuid(ceiAfterPreEvents.getUuid());
				sqlCei.setCode(ceiAfterPreEvents.getCode());
				sqlCei.setCetCode(ceiAfterPreEvents.getCetCode());
				sqlCei.setDescription(ceiAfterPreEvents.getDescription());
				customFieldInstanceService.setCfValues(sqlCei, cet.getCode(), sqlValues);

				// Update binaries stored in SQL
				List<CustomFieldTemplate> binariesInSql = customFieldTemplates.values().stream().filter(f -> f.getFieldType().equals(CustomFieldTypeEnum.BINARY)).filter(f -> f.getStoragesNullSafe().contains(DBStorageType.SQL)).collect(Collectors.toList());

				if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
					uuid = createOrUpdateSQL(repository, sqlCei, binariesInSql, customFieldTemplates);

				} else {
					uuid = createOrUpdateCei(repository, sqlCei, binariesInSql);
				}

				persistedEntities.add(new EntityRef(uuid, cet.getCode()));
			}
		}

		try {
			
			if(cetClassInstance != null) {
				if(foundId != null) {
					listener.postUpdate(cetClassInstance);
				} else {
					listener.postPersist(cetClassInstance);
				}
			}
			
			transaction.commitTransaction(repository);
			
		} catch (Exception e) {
			
			log.error("Can't create or update data", e);
			
			transaction.rollbackTransaction(e);
			
			if(e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
		
		if(uuid == null) {
			throw new PersistenceException("Failed to save " + ceiAfterPreEvents);
		}
		
		return new PersistenceActionResult(persistedEntities, uuid);
	}

	private String createOrUpdateCei(Repository repository, CustomEntityInstance ceiToSave, Collection<CustomFieldTemplate> binariesInSql) throws BusinessException, IOException, BusinessApiException {
		ceiToSave.setRepository(repository);
		
		CustomEntityTemplate cet = ceiToSave.getCet();
		Map<String, Object> values = ceiToSave.getCfValuesAsValues();

		CustomEntityInstance cei = getCustomEntityInstance(ceiToSave.getCetCode(), ceiToSave.getCode(), values);		
		
		Map<CustomFieldTemplate, Object> persistedBinaries = new HashMap<>();

		if (cei == null) {
			cei = ceiToSave;

			if (CollectionUtils.isNotEmpty(binariesInSql)) {
				persistedBinaries = fileSystemService.updateBinaries(repository, cei.getUuid(), cet, binariesInSql, values, new HashMap<>());
			}

			for (Map.Entry<CustomFieldTemplate, Object> entry : persistedBinaries.entrySet()) {
				cei.getCfValues().setValue(entry.getKey().getCode(), entry.getValue());
			}

			customEntityInstanceService.create(cei);

		} else {
			cei.setRepository(repository);
			
			if (CollectionUtils.isNotEmpty(binariesInSql)) {
				final Map<String, Object> existingValues = cei.getCfValuesAsValues();
				persistedBinaries = fileSystemService.updateBinaries(repository, cei.getUuid(), cet, binariesInSql, values, existingValues);
			}

			cei.setCfValuesOld(cei.getCfValues());
			cei.setCfValues(ceiToSave.getCfValues());
			cei.setDescription(ceiToSave.getDescription());
			for (Map.Entry<CustomFieldTemplate, Object> entry : persistedBinaries.entrySet()) {
				cei.getCfValues().setValue(entry.getKey().getCode(), entry.getValue());
			}

			customEntityInstanceService.update(cei);
		}

		return cei.getUuid();
	}

	private String createOrUpdateNeo4J(Repository repository, CustomEntityInstance cei, Map<String, CustomFieldTemplate> customFieldTemplates, Set<EntityRef> persistedEntities, String foundUuid) throws IOException, BusinessException, BusinessApiException {
		
		String uuid = null;
		CustomEntityInstance neo4jCei = new CustomEntityInstance();
		neo4jCei.setCetCode(cei.getCetCode());
		neo4jCei.setUuid(foundUuid);
		neo4jCei.setCet(cei.getCet());
		neo4jCei.setRepository(repository);
		cei.setRepository(repository);
		
		var cfts = cache.getCustomFieldTemplates(cei.getCet().getAppliesTo());

		if (cei.getCet().getAvailableStorages().contains(DBStorageType.NEO4J)) {

			Map<String, Object> neo4jValues = filterValues(cfts, cei.getValuesNullSafe(), neo4jCei.getCet(), DBStorageType.NEO4J, false);
			customFieldInstanceService.setCfValues(neo4jCei, neo4jCei.getCetCode(), neo4jValues);

			if (!neo4jValues.isEmpty()) {
				PersistenceActionResult persistenceResult = neo4jService.addCetNode(repository.getNeo4jConfiguration().getCode(), neo4jCei);
				uuid = persistenceResult.getBaseEntityUuid();

				if (uuid == null) {
					throw new NullPointerException("Generated UUID from Neo4J cannot be null");
				}

				if (foundUuid != null && !foundUuid.equals(uuid)) {
					log.error("Wrong Neo4J UUID {} for {}, should be {}", uuid, neo4jCei, cei.getUuid(), new Exception());
				}

				// Update binaries stored in Neo4j
				updateNeo4jBinaries(repository, cei.getCet(), customFieldTemplates, uuid, neo4jValues);

				persistedEntities.addAll(persistenceResult.getPersistedEntities());
			}
		}

		return uuid;
	}

	/**
	 * TODO: Document
	 */
	@SuppressWarnings("unchecked")
	private void updateNeo4jBinaries(Repository repository, CustomEntityTemplate cet, Map<String, CustomFieldTemplate> customFieldTemplates, String uuid, Map<String, Object> neo4jValues) throws IOException, BusinessApiException {
		List<CustomFieldTemplate> binariesInNeo4J = customFieldTemplates.values().stream().filter(f -> f.getFieldType().equals(CustomFieldTypeEnum.BINARY)).filter(f -> f.getStoragesNullSafe().contains(DBStorageType.NEO4J)).collect(Collectors.toList());

		String neo4JCode = repository.getNeo4jConfiguration().getCode();

		if (!CollectionUtils.isEmpty(binariesInNeo4J)) {

			Map<String, Object> existingBinaries = new HashMap<>();

			for (CustomFieldTemplate neo4jField : binariesInNeo4J) {
				// Retrieve binaries
				List<String> binaries = neo4jService.findBinaries(uuid, repository.getNeo4jConfiguration().getCode(), cet, neo4jField);

				if (CollectionUtils.isEmpty(binaries)) {
					continue;
				}

				if (neo4jField.getStorageType().equals(CustomFieldStorageTypeEnum.SINGLE)) {
					existingBinaries.put(neo4jField.getCode(), binaries.get(0));
				} else if (neo4jField.getStorageType().equals(CustomFieldStorageTypeEnum.LIST)) {
					existingBinaries.put(neo4jField.getCode(), binaries);
				}

			}

			// Persist binaries in file system
			final Map<CustomFieldTemplate, Object> binariesByCft = fileSystemService.updateBinaries(repository, uuid, cet, customFieldTemplates.values(), neo4jValues, existingBinaries);

			// Handle binaries references stored in Neo4J
			for (Map.Entry<CustomFieldTemplate, Object> binary : binariesByCft.entrySet()) {
				if (binary.getValue() instanceof String) {
					neo4jService.updateBinary(uuid, neo4JCode, cet, binary.getKey(), (String) binary.getValue());

				} else if (binary.getValue() instanceof Collection) {
					// Delete binaries present in previous values and not in persisted values
					List<String> previousBinaries = (List<String>) existingBinaries.get(binary.getKey().getCode());
					if (previousBinaries != null) {
						for (String previousBinary : previousBinaries) {
							// Check if existing files were deleted and remove them from neo4j if they were
							if (!new File(previousBinary).exists()) {
								neo4jService.removeBinary(uuid, neo4JCode, cet, binary.getKey(), previousBinary);
							}
						}
					}

					// Add or update remaining binaries
					neo4jService.addBinaries(uuid, neo4JCode, cet, binary.getKey(), (Collection<String>) binary.getValue());

					// All binaries were deleted
				} else if (binary.getValue() == null) {
					neo4jService.removeBinaries(uuid, repository.getNeo4jConfiguration().getCode(), cet, binary.getKey());
				}
			}

		}
	}

	private String createOrUpdateSQL(Repository repository, CustomEntityInstance cei, Collection<CustomFieldTemplate> binariesInSql, Map<String, CustomFieldTemplate> cfts) throws BusinessException, IOException, BusinessApiException, EntityDoesNotExistsException {
		cei.setRepository(repository);
		
		String sqlUUID = null;
		
		Map<String, Object> oldCfValues = new HashMap<>();
		
		if(cei.getUuid() != null) {
			try {
				oldCfValues = customTableService.findById(repository.getSqlConfigurationCode(), cei.getCet(), cei.getUuid());
				if(oldCfValues != null) {
					sqlUUID = cei.getUuid();
					oldCfValues.remove("uuid");
				}
			} catch (EntityDoesNotExistsException e) {
				log.debug("Entity with id={} does not exists", cei.getUuid());
			}
		}
		
		if(sqlUUID == null) {
			Map<String, Object> sqlValues = filterValues(cfts, cei.getCfValuesAsValues(), cei.getCet(), DBStorageType.SQL, false);
			sqlUUID = customTableService.findIdByUniqueValues(repository.getSqlConfigurationCode(), cei.getCet(), sqlValues, cfts.values());
			if (sqlUUID != null) {
				if (oldCfValues == null) {
					oldCfValues = customTableService.findById(repository.getSqlConfigurationCode(), cei.getCet(), sqlUUID);
					if(oldCfValues != null) {
						oldCfValues.remove("uuid");
					} else {
						throw new IllegalStateException("Error in CrossStarageService : An entity which match on unique values exists, can't fetch this entity with cetCode / uuid " + cei.getCetCode()+ "/" +sqlUUID);
					}
				}
			}
		}
		
		if (sqlUUID != null) {

			
			cei.setUuid(sqlUUID);
			
			CustomEntityInstance tempCei = new CustomEntityInstance();
			tempCei.setCetCode(cei.getCetCode());
			tempCei.setRepository(repository);
			customFieldInstanceService.setCfValues(tempCei, cei.getCetCode(), oldCfValues);
			cei.setCfValuesOld(tempCei.getCfValues());
			
			// Update binaries
			if (CollectionUtils.isNotEmpty(binariesInSql)) {
				List<String> binariesFieldsToFetch = binariesInSql.stream().map(CustomFieldTemplate::getCode).collect(Collectors.toList());

				Map<String, Object> existingBinariesField = customTableService.findById(repository.getSqlConfigurationCode(), cei.getCet(), sqlUUID, binariesFieldsToFetch);
				fileSystemService.updateBinaries(repository, 
						cei.getUuid(), 
						cei.getCet(), 
						binariesInSql, 
						cei.getCfValuesAsValues(), 
						existingBinariesField);

			}
			
			customTableService.update(repository.getSqlConfigurationCode(), cei.getCet(), cei);
			customEntityInstanceUpdate.fire(cei);

		} else {
			String uuid = customTableService.create(repository.getSqlConfigurationCode(), cei.getCet(), cei);
			cei.setUuid(uuid);

			// Save binaries
			if (CollectionUtils.isNotEmpty(binariesInSql)) {

				final Map<CustomFieldTemplate, Object> binariesPaths = fileSystemService.updateBinaries(repository, uuid, cei.getCet(), binariesInSql, cei.getCfValuesAsValues(), null);

				for (Map.Entry<CustomFieldTemplate, Object> binary : binariesPaths.entrySet()) {
					customTableService.updateValue(repository.getSqlConfigurationCode(), cei.getTableName(), uuid, binary.getKey().getDbFieldname(), binary.getValue());
				}
			}
			
			customEntityInstanceCreate.fire(cei);
		}

		return cei.getUuid();
	}

	/**
	 * Update an entity instance
	 *
	 * @param repository Repository code
	 * @param ceiToUpdate the instance to be updated
	 * @throws BusinessException if error occures
	 * @throws IOException  if a binary can't be updated
	 * @throws BusinessApiException  if error occures
	 * @throws EntityDoesNotExistsException if the data does not exist yet
	 */
	public void update(Repository repository, CustomEntityInstance ceiToUpdate) throws BusinessException, IOException, BusinessApiException, EntityDoesNotExistsException {
		ceiToUpdate.setRepository(repository);
		CustomEntityTemplate cet = ceiToUpdate.getCet();
		Map<String, Object> values = ceiToUpdate.getCfValuesAsValues();
		String uuid = ceiToUpdate.getUuid();
		Map<String, CustomFieldTemplate> customFieldTemplates = cache.getCustomFieldTemplates(cet.getAppliesTo());

		if (ceiToUpdate.getCfValuesOld() != null && !ceiToUpdate.getCfValuesOld().getValuesByCode().isEmpty()) {
			try {
				checkBeforeUpdate(repository, ceiToUpdate);
			} catch (EntityDoesNotExistsException | ELException e) {
				throw new BusinessException(e);
			}
		}
		
		// Neo4j storage
		if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
			Map<String, Object> neo4jValues = filterValues(customFieldTemplates, values, cet, DBStorageType.NEO4J);

			final List<String> cetLabels = cet.getNeo4JStorageConfiguration().getLabels() != null ? cet.getNeo4JStorageConfiguration().getLabels() : new ArrayList<>();
			List<String> labels = new ArrayList<>(cetLabels);
			labels.add(cet.getCode());

			updateNeo4jBinaries(repository, cet, customFieldTemplates, uuid, neo4jValues);

			neo4jDao.updateNodeByNodeId(repository.getNeo4jConfiguration().getCode(), uuid, cet.getCode(), neo4jValues, labels);
		}

		// SQL Storage
		if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
			List<CustomFieldTemplate> binariesInSql = customFieldTemplates.values().stream().filter(f -> f.getFieldType().equals(CustomFieldTypeEnum.BINARY)).filter(f -> f.getStoragesNullSafe().contains(DBStorageType.SQL)).collect(Collectors.toList());

			// Custom table
			if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
				Map<String, Object> sqlValues = filterValues(customFieldTemplates, values, cet, DBStorageType.SQL);

				if (!CollectionUtils.isEmpty(binariesInSql)) {
					List<String> binariesFieldsToFetch = binariesInSql.stream().map(CustomFieldTemplate::getCode).collect(Collectors.toList());

					final Map<String, Object> existingBinariesFields = customTableService.findById(repository.getSqlConfigurationCode(), cet, uuid, binariesFieldsToFetch);
					fileSystemService.updateBinaries(repository, uuid, cet, binariesInSql, sqlValues, existingBinariesFields);
				}

				customTableService.update(repository.getSqlConfigurationCode(), cet, ceiToUpdate);
			} else {
				// CEI storage
				final CustomEntityInstance cei = customEntityInstanceService.findByUuid(cet.getCode(), uuid);
				cei.setRepository(repository);
				
				// Update binaries
				if (CollectionUtils.isNotEmpty(binariesInSql)) {
					final Map<String, Object> existingValues = cei.getCfValuesAsValues();
					fileSystemService.updateBinaries(repository, cei.getUuid(), cet, binariesInSql, values, existingValues);
				}

				CustomFieldValues customFieldValues = new CustomFieldValues();
				values.forEach(customFieldValues::setValue);
				cei.setCfValues(customFieldValues);
				customEntityInstanceService.update(cei);
			}
		}
	}

	@Override
	public PersistenceActionResult addCRTByValues(Repository repository, String relationCode, Map<String, Object> relationValues, Map<String, Object> sourceValues, Map<String, Object> targetValues) throws ELException, BusinessException {
		CustomRelationshipTemplate crt = cache.getCustomRelationshipTemplate(relationCode);

		final CustomEntityTemplate endNode = crt.getEndNode();
		final CustomEntityTemplate startNode = crt.getStartNode();
		
		var cfts = cache.getCustomFieldTemplates(crt.getAppliesTo());

		// All Neo4j storage
		if (crt.getAvailableStorages().contains(DBStorageType.NEO4J) && startNode.getAvailableStorages().contains(DBStorageType.NEO4J) && endNode.getAvailableStorages().contains(DBStorageType.NEO4J)) {
			return neo4jService.addCRTByNodeValues(
					repository.getNeo4jConfiguration().getCode(), 
					relationCode, 
					filterValues(cfts, relationValues, crt, DBStorageType.NEO4J),
					filterValues(cfts, sourceValues, crt, DBStorageType.NEO4J),
					filterValues(cfts, targetValues, crt, DBStorageType.NEO4J));
		}

		String sourceUUID = findEntityId(repository, sourceValues, startNode);
		String targetUUUID = findEntityId(repository, targetValues, endNode);

		// SQL Storage
		if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
			String relationUuid = customTableRelationService.createOrUpdateRelation(repository, crt, sourceUUID, targetUUUID, relationValues);
			return new PersistenceActionResult(relationUuid);
		}

		// Neo4J Storage
		if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
			return neo4jService.addCRTByNodeIds(repository.getNeo4jConfiguration().getCode(), crt.getCode(), relationValues, sourceUUID, targetUUUID);
		}

		return null;
	}

	@Override
	public PersistenceActionResult addCRTByUuids(Repository repository, String relationCode, Map<String, Object> relationValues, String sourceUuid, String targetUuid) throws ELException, BusinessException {
		CustomRelationshipTemplate crt = cache.getCustomRelationshipTemplate(relationCode);
		var cfts = cache.getCustomFieldTemplates(crt.getAppliesTo());

		// All neo4j storage
		if (isEverythingStoredInNeo4J(crt)) {
			return neo4jService.addCRTByNodeIds(
					repository.getNeo4jConfiguration().getCode(), 
					relationCode, 
					filterValues(cfts, relationValues, crt, DBStorageType.NEO4J), sourceUuid, targetUuid);
		}

		// SQL Storage
		if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
			String relationUuid = customTableRelationService.createOrUpdateRelation(repository, crt, sourceUuid, targetUuid, relationValues);
			return new PersistenceActionResult(relationUuid);

		}

		// Neo4J Storage
		if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
			return neo4jService.addCRTByNodeIds(repository.getNeo4jConfiguration().getCode(), crt.getCode(), relationValues, sourceUuid, targetUuid);
		}

		return null;
	}

	/**
	 * Retrieve the uuid of an entity
	 * 
	 * @param repository repository where the entity is stored
	 * @param cei the instance holding values to get id from
	 * @return the id corresponding to the values
	 */
	public String findEntityId(Repository repository, CustomEntityInstance cei) {
		cei.setRepository(repository);
		
		String uuid = null;
		CustomEntityTemplate cet = cei.getCet();
		Map<String, Object> valuesFilters = cei.getValuesNullSafe();
		Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.getCftsWithInheritedFields(cet);

		// SQL
		if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
			// Custom table
			if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
				if(cei.getUuid() != null) {
					try {
						Map<String, Object> values = customTableService.findById(repository.getSqlConfigurationCode(), cet, cei.getUuid());
						if(values != null) {
							uuid = cei.getUuid();
						}
					} catch (EntityDoesNotExistsException e) {}
				}
				
				if(uuid == null) {
					List<CustomFieldTemplate> uniqueCfts = cfts.values().stream()
						.filter(CustomFieldTemplate::isUnique)
						.filter(cft -> cft.getStoragesNullSafe().contains(DBStorageType.SQL))
						.collect(Collectors.toList());

					if(uniqueCfts.isEmpty()) {
						throw new IllegalArgumentException("Can't retrieve SQL record by values if no unique fields are defined");
					}
					
					Map<String, Object> uniqueValues = new HashMap<>();
					uniqueCfts.forEach(cft -> {
						var value = valuesFilters.get(cft.getCode());
						if(value != null) {
							uniqueValues.put(cft.getCode(), value);
						}
					});
					
					if(uniqueValues.isEmpty()) {
						throw new IllegalArgumentException("No unique values provided");
					}
					
					uuid = customTableService.findIdByUniqueValues(repository.getSqlConfigurationCode(), cet, uniqueValues, cfts.values());
				}
				
			} else {
				if(cei.getUuid() != null) {
					CustomEntityInstance existingCei = customEntityInstanceService.findByUuid(cet.getCode(), cei.getUuid());
					if (existingCei != null) {
						uuid = existingCei.getUuid();
					}
				}
				
				if(uuid == null){
					final CustomEntityInstance customEntityInstance = getCustomEntityInstance(cet.getCode(), cei.getCode(), valuesFilters);
					if (customEntityInstance != null) {
						uuid = customEntityInstance.getUuid();
					}
				}
			}
		}

		// Neo4J
		if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
			Map<String, Object> val = neo4jDao.findNodeById(repository.getNeo4jConfiguration().getCode(), cet.getCode(), cei.getUuid());

			try {
				final String neo4JUuid;
				if (val != null && !val.isEmpty()) {
					neo4JUuid = cei.getUuid();
				} else {
					neo4JUuid = neo4jService.findNodeId(
							repository.getNeo4jConfiguration().getCode(), 
							cet, 
							filterValues(cfts, valuesFilters, cet, DBStorageType.NEO4J, false)
						);
				}

				if (uuid != null && neo4JUuid != null && !uuid.equals(neo4JUuid)) {
					log.error("Neo4J and SQL UUIDs are different for instance of {} with values {} ({} =/= {})", cet, valuesFilters, neo4JUuid, uuid);
				} else if (neo4JUuid != null) {
					uuid = neo4JUuid;
				}

			} catch (InvalidCustomFieldException e) {
				log.warn("Invalid custom field", e.getMessage());
				return null;
			} catch (ELException | BusinessException e) {
				throw new RuntimeException(e);
			}
		}

		return uuid;
	}

	/**
	 * Find an entity instance UUID
	 *
	 * @param repository    Repository to search in
	 * @param valuesFilters Filter on entity's values
	 * @param cet           Template of the entity
	 * @return UUID of the entity or nul if it was'nt found
	 * @throws BusinessException if error happens
	 */
	public String findEntityId(Repository repository, Map<String, Object> valuesFilters, CustomEntityTemplate cet) throws BusinessException {
		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setCet(cet);
		cei.setCetCode(cet.getCode());
		cei.setCode((String) valuesFilters.get("code"));
		cei.setRepository(repository);
		
		customFieldInstanceService.setCfValues(cei, cet.getCode(), valuesFilters);
		return findEntityId(repository, cei);
	}

	/**
	 * Remove an entity from database
	 *
	 * @param repository Repository
	 * @param cet        Template of the entity
	 * @param uuid       UUID of the entity
	 * @throws BusinessException if error happens
	 */
	public void remove(Repository repository, CustomEntityTemplate cet, String uuid) throws BusinessException {
		if (uuid == null) {
			throw new IllegalArgumentException("Cannot remove entity by UUID without uuid");
		}
		
		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setCet(cet);
		cei.setCetCode(cet.getCode());
		cei.setUuid(uuid);
		cei.setRepository(repository);
		
		var listener = customEntityTemplateService.loadCrudEventListener(cei.getCet());
		CustomEntity cetClassInstance = null;
		
		transaction.beginTransaction(repository);
		
		try {
			if(listener != null) {
				var cetClass =  listener.getEntityClass();
				try {
					var values = find(repository, cet, uuid, false);
					cei = CEIUtils.fromMap(values, cet);
					cetClassInstance = CEIUtils.ceiToPojo(cei, cetClass);
					listener.preRemove(cetClassInstance);
				} catch (EntityDoesNotExistsException e) {
					e.printStackTrace();
				}
			}
	
			if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
				if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
					final String dbTablename = SQLStorageConfiguration.getDbTablename(cet);
					customTableService.remove(repository.getSqlConfigurationCode(), cet, uuid);
				} else {
					final CustomEntityInstance customEntityInstance = customEntityInstanceService.findByUuid(cet.getCode(), uuid);
					customEntityInstanceService.remove(customEntityInstance);
				}
			}
	
			if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
				neo4jDao.removeNodeByUUID(repository.getNeo4jConfiguration().getCode(), cet.getCode(), uuid);
			}
	
			fileSystemService.delete(repository, cet, uuid);
			
			if (!(cet.getAvailableStorages().contains(DBStorageType.SQL) && !cet.getSqlStorageConfiguration().isStoreAsTable())) {
				customEntityInstanceDelete.fire(cei);
			}
			
			if(cetClassInstance != null) {
				listener.postRemove(cetClassInstance);
			}
			
			// Delete binaries
			fileSystemService.delete(repository, cet, uuid);
			
			transaction.commitTransaction(repository);
		
		} catch(Exception e) {
			transaction.rollbackTransaction(e);
			throw e;
		}
		
	}

	/**
	 * Replace the files references by the one in input
	 * 
	 * @param repository Repository where data is stored
	 * @param cet        Template of the data
	 * @param cft        Concerned field
	 * @param uuid       UUID of the entity
	 * @param binaries   New binaries
	 * @throws BusinessException if update fails
	 */
	public void setBinaries(Repository repository, CustomEntityTemplate cet, CustomFieldTemplate cft, String uuid, List<File> binaries) throws BusinessException {
		List<String> paths = binaries.stream().map(File::getPath).collect(Collectors.toList());

		if (cft.getStoragesNullSafe() != null && cft.getStoragesNullSafe().contains(DBStorageType.NEO4J)) {
			neo4jService.removeBinaries(uuid, repository.getNeo4jConfiguration().getCode(), cet, cft);
			neo4jService.addBinaries(uuid, repository.getNeo4jConfiguration().getCode(), cet, cft, paths);
		}

		if (cft.getStoragesNullSafe() != null && cft.getStoragesNullSafe().contains(DBStorageType.SQL) && cet.getSqlStorageConfiguration() != null) {
			Object valueToSave = binaries;
			if (cft.getStorageType().equals(CustomFieldStorageTypeEnum.SINGLE)) {
				valueToSave = paths.isEmpty() ? null : paths.get(0);
			}

			if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
				customTableService.updateValue(repository.getSqlConfigurationCode(), SQLStorageConfiguration.getDbTablename(cet), uuid, cft.getDbFieldname(), valueToSave);
			} else {
				CustomEntityInstance cei = customEntityInstanceService.findByUuid(cet.getCode(), uuid);
				CustomFieldValues cfValues = cei.getCfValues();
				cfValues.setValue(cft.getCode(), valueToSave);
				customEntityInstanceService.update(cei);
			}
		}

	}

	private Map<String, Object> filterValues(Map<String, CustomFieldTemplate> cfts, Map<String, Object> values, CustomModelObject cet, DBStorageType storageType) {
		return filterValues(cfts, values, cet, storageType, false);
	}

	private Map<String, Object> filterValues(Map<String, CustomFieldTemplate> cfts, Map<String, Object> values, CustomModelObject cet, DBStorageType storageType, boolean isRequiredOnly) {
		Map<String, Object> filteredValues = new HashMap<>();

		values.entrySet().stream().filter(entry -> {
			// Always include UUID
			if (entry.getKey().equals("uuid")) {
				return true;
			}

			// For CEI storage, always include code
			if (cet instanceof CustomEntityTemplate && entry.getKey().equals("code") && storageType == DBStorageType.SQL && !((CustomEntityTemplate) cet).getSqlStorageConfiguration().isStoreAsTable()) {
				return true;
			}

			CustomFieldTemplate cft = cfts.get(entry.getKey());

			if(cft == null) {
				return false;
			}
			
			if(isRequiredOnly) {
				if(!cft.isValueRequired() && !cft.isUnique()) {
					return false;
				}
			}

			return cft.getStoragesNullSafe().contains(storageType);
		}).forEach(v -> filteredValues.put(v.getKey(), v.getValue()));

		return filteredValues;
	}

	private List<String> filterFields(List<String> fields, CustomModelObject cet, DBStorageType storageType) {
		// If fields are null return all avaiblable fields for the given storage
		if (fields == null) {
			return new ArrayList<>();
		}

		return fields.stream().filter(entry -> {
			CustomFieldTemplate cft = cache.getCustomFieldTemplate(entry, cet.getAppliesTo());
			if (cft == null) {
				return false;
			}
			return cft.getStoragesNullSafe().contains(storageType);
		}).collect(Collectors.toList());
	}

	private Map<String, Object> createEntityReferences(Repository repository, Map<String, Object> entityValues, CustomEntityTemplate cet) throws BusinessException, IOException, BusinessApiException, EntityDoesNotExistsException {
		Map<String, Object> updatedValues = new HashMap<>(entityValues);

		List<CustomFieldTemplate> cetFields = updatedValues.keySet().stream().map(fieldName -> cache.getCustomFieldTemplate(fieldName, cet.getAppliesTo())).filter(Objects::nonNull).collect(Collectors.toList());

		for (CustomFieldTemplate customFieldTemplate : cetFields) {
			if (CustomFieldTypeEnum.ENTITY.equals(customFieldTemplate.getFieldType())) {
				final CustomEntityTemplate referencedCet = cache.getCustomEntityTemplate(customFieldTemplate.getEntityClazzCetCode());

				if(referencedCet != null) {
					createCetReference(repository, updatedValues, customFieldTemplate, referencedCet);
				
				}
			}
		}

		return updatedValues;
	}

	@SuppressWarnings("unchecked")
	private void createCetReference(Repository repository, Map<String, Object> updatedValues, CustomFieldTemplate customFieldTemplate, CustomEntityTemplate referencedCet) throws BusinessException, BusinessApiException, EntityDoesNotExistsException, IOException {
		List<Object> entitiesToCreate = new ArrayList<>();
		final Object fieldValue = updatedValues.get(customFieldTemplate.getCode());
		
		// Don't save empty nodes
		if(fieldValue == null) {
			return;
		}
		
		final Set<EntityRef> createdEntityReferences = new HashSet<>();

		if (fieldValue instanceof Collection && customFieldTemplate.getStorageType() != CustomFieldStorageTypeEnum.LIST) {
			Collection<?> collectionValue = (Collection<? extends Map<String, Object>>) fieldValue;
			if (!collectionValue.isEmpty()) {
				entitiesToCreate.add(collectionValue.iterator().next());
			}
		
		} else if (fieldValue instanceof Collection && customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
			Collection<EntityReferenceWrapper> fieldValueCol = (Collection<EntityReferenceWrapper>) fieldValue;
			if(!fieldValueCol.isEmpty()) {
				// Only references are passed so the entities are already created
				if(fieldValueCol.iterator().next() instanceof EntityReferenceWrapper) {
					Collection<EntityReferenceWrapper> entityReferences = (Collection<EntityReferenceWrapper>) fieldValueCol;
					List<EntityRef> entityRefs = entityReferences.stream()
							.map(EntityRef::new)
							.collect(Collectors.toList());
					createdEntityReferences.addAll(entityRefs);
				} else {
					entitiesToCreate.addAll((Collection<? extends Map<String, Object>>) fieldValue);
				}
			}
		
		} else if (fieldValue instanceof Map) {
			
			// Create references stored in map then update it
			if (customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.MAP) {
				Map<String, Object> referenceMap = new HashMap<>();
				for (var entry : ((Map<String, Object>) fieldValue).entrySet()) {
					if (entry.getValue() instanceof Map) {
						var map = (Map<String, Object>) entry.getValue();
						if (map.get("uuid") != null) {
							try {
								var existingData = find(repository, referencedCet, (String) map.get("uuid"), false);
								if (existingData != null) {
									referenceMap.put(entry.getKey(), map.get("uuid"));
									continue;
								}
							} catch (EntityDoesNotExistsException e) {
								//NOOP
							}
						}
						String uuid = createReferencedEntity(repository, customFieldTemplate, map).getBaseEntityUuid();
						referenceMap.put(entry.getKey(), uuid);
					} else if (entry.getValue() instanceof EntityReferenceWrapper) {
						referenceMap.put(entry.getKey(), ((EntityReferenceWrapper) entry.getValue()).getUuid());
					} else if (entry.getValue() instanceof String) {
						referenceMap.put(entry.getKey(), (String) entry.getValue());
					} else if (entry.getValue() instanceof CustomEntity) {
						var ce = (CustomEntity) entry.getValue();
						if (ce.getUuid() != null) {
							try {
								var existingData = find(repository, referencedCet, ce.getUuid(), false);
								if (existingData != null) {
									referenceMap.put(entry.getKey(), ce.getUuid());
									continue;
								}
							} catch (EntityDoesNotExistsException e) {
								//NOOP
							}
						}
						var cei = CEIUtils.pojoToCei(ce);
						cei.setRepository(repository);
						
						String uuid = createReferencedEntity(repository, customFieldTemplate, cei.getCfValuesAsValues()).getBaseEntityUuid();
						referenceMap.put(entry.getKey(), uuid);
					}
				}
				updatedValues.put(customFieldTemplate.getCode(), referenceMap);
			} else {
				entitiesToCreate.add(fieldValue);
			}
		
		} else if (referencedCet.getNeo4JStorageConfiguration() != null && referencedCet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {
			entitiesToCreate.add(Collections.singletonMap("value", fieldValue));
		
		} else if (fieldValue instanceof EntityReferenceWrapper) {
			EntityReferenceWrapper entityReferenceWrapper = (EntityReferenceWrapper) fieldValue;
			updatedValues.put(customFieldTemplate.getCode(), entityReferenceWrapper.getUuid());
			createdEntityReferences.add(new EntityRef(entityReferenceWrapper));
		} else if (fieldValue instanceof CustomEntityInstance) {
			EntityRef entityRef = new EntityRef(((CustomEntityInstance) fieldValue).getUuid(), referencedCet.getCode());
			createdEntityReferences.add(entityRef);
		}

		for (Object e : entitiesToCreate) {
			if (e instanceof Map) {
				final Set<EntityRef> createdEntities = createReferencedEntity(repository, customFieldTemplate, (Map<String, Object>) e)
						.getPersistedEntities();
				if (createdEntities.isEmpty()) {
					log.error("Failed to create reference for {} ", e, new Exception());
				}
				createdEntityReferences.addAll(createdEntities);

			} else if (e instanceof String) {
				// If entity reference is a string, then it means it refers to an existing UUID
				EntityRef entityRef = new EntityRef((String) e, referencedCet.getCode());
				entityRef.setTrustScore(100);
				createdEntityReferences.add(entityRef);
			}
		}

		List<String> uuids = getTrustedUuids(createdEntityReferences);

		// Replace with entity reference's UUID only when target is not primitive
		if (referencedCet.getNeo4JStorageConfiguration() == null || !referencedCet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {

			if (customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
				updatedValues.put(customFieldTemplate.getCode(), uuids);

			} else if (customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.SINGLE && !uuids.isEmpty()) {
				if (customFieldTemplate.getFieldType() == CustomFieldTypeEnum.ENTITY && referencedCet.getSqlStorageConfiguration() != null) {
					if (!referencedCet.isStoreAsTable()) {
						CustomEntityInstance customEntityInstance = customEntityInstanceService.findByUuid(referencedCet.getCode(), uuids.get(0));
						updatedValues.put(customFieldTemplate.getCode(), customEntityInstance);
					} else {
						Map<String, Object> map = customTableService.findById(repository.getSqlConfigurationCode(), referencedCet.getCode(), uuids.get(0));
						if (map != null && !map.isEmpty()) {
							if (map.get("code") == null) {
								map.put("code", uuids.get(0));
							}
							CustomEntityInstance cei = customEntityInstanceService.fromMap(referencedCet, map);
							cei.setRepository(repository);
							updatedValues.put(customFieldTemplate.getCode(), cei);
						}
					}
				}else {
					updatedValues.put(customFieldTemplate.getCode(), uuids.get(0));
				}
			}

		} else {
			// If entity reference is primitive, place the UUID in the map along with the
			// value
			updatedValues.put(customFieldTemplate.getCode() + "UUID", uuids.get(0));
		}

	}

	/**
	 * @param repository
	 * @param customFieldTemplate
	 * @param e
	 * @param map
	 * @return
	 * @throws BusinessException
	 * @throws IOException
	 * @throws BusinessApiException
	 * @throws EntityDoesNotExistsException
	 */
	protected PersistenceActionResult createReferencedEntity(Repository repository, CustomFieldTemplate customFieldTemplate, Map<String, Object> values) throws BusinessException, IOException, BusinessApiException, EntityDoesNotExistsException {
		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setCetCode(customFieldTemplate.getEntityClazzCetCode());
		cei.setCode((String) values.get("code"));
		cei.setRepository(repository);
		String uuid = (String) values.get("uuid");
		if (uuid != null) {
			cei.setUuid(uuid);
		}

		customFieldInstanceService.setCfValues(cei, customFieldTemplate.getEntityClazzCetCode(), values);

		return createOrUpdate(repository, cei);
	}

	private String findUniqueRelationByTargetUuid(Repository repository, String targetUuid, CustomRelationshipTemplate crt) {
		if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
			return customTableRelationService.findIdOfUniqueRelationByTargetId(crt, targetUuid);
		}

		if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
			return neo4jService.findIdOfUniqueRelationByTargetId(repository.getNeo4jConfiguration().getCode(), crt, targetUuid);
		}

		return null;
	}

	private String findIdOfSourceEntityByRelationId(Repository repository, String relationUuid, CustomRelationshipTemplate crt) {
		if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
			return customTableRelationService.findIdOfSourceEntityByRelationId(crt, relationUuid);
		}

		if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
			return neo4jService.findIdOfUniqueRelationByTargetId(repository.getNeo4jConfiguration().getCode(), crt, relationUuid);
		}

		return null;
	}

	public void fetchEntityReferences(Repository repository, CustomModelObject customModelObject, Map<String, Object> values, Map<String, Set<String>> subFields) throws EntityDoesNotExistsException {
		for (Map.Entry<String, Object> entry : new HashSet<>(values.entrySet())) {
			CustomFieldTemplate cft = cache.getCustomFieldTemplate(entry.getKey(), customModelObject.getAppliesTo());
			if (cft != null && cft.getFieldType() == CustomFieldTypeEnum.ENTITY) {
				// Check if target is not JPA entity
				try {
					var session = transaction.getHibernateSession(repository.getSqlConfigurationCode());
					Class<?> clazz = Class.forName(cft.getEntityClazzCetCode());
					values.put(
						entry.getKey(), 
						session.find(clazz, entry.getValue())
					);
					continue;
					
				} catch (ClassNotFoundException e) {
					//NOOP
				
				} catch(Exception e) {
					log.error("Cannot find referenced entity {}", e.getMessage());
					throw new RuntimeException(e);
				}
				
				CustomEntityTemplate cet = cache.getCustomEntityTemplate(cft.getEntityClazzCetCode());
				var nConf = cet.getNeo4JStorageConfiguration();
				
				// Don't fetch primitive entities
				if(nConf != null && nConf.isPrimitiveEntity()) {
					continue;
				}
				
				Set<String> fetchFields = subFields.get(cft.getCode());
				if(fetchFields == null) {
					continue;
				}
				fetchFields = new HashSet<>(fetchFields);
				
				Map<String, Set<String>> subSubFields = extractSubFields(fetchFields);

				if (fetchFields.contains("*")) {
					fetchFields = cache.getCustomFieldTemplates(cet.getAppliesTo()).keySet();
				}
				
				if(cft.getStorageType() == CustomFieldStorageTypeEnum.SINGLE) {
					if(entry.getValue() instanceof String) {
						Map<String, Object> refValues = find(repository, cet, (String) entry.getValue(), fetchFields, subSubFields, false);
						values.put(cft.getCode(), refValues);
					} else if(entry.getValue() instanceof Map) {
						values.put(cft.getCode(), entry.getValue());
					}
				} else if(cft.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
					if(entry.getValue() instanceof Collection) {
						Collection<?> collection = (Collection<?>) entry.getValue();
						if(!collection.isEmpty()) {
							var firstItem = collection.iterator().next();
							if(firstItem instanceof String) {
								// List list = find(repository, cet, null);
								//TODO
							}
						}
					}
				}

			}
		}
	}

	private void replaceKeys(CustomEntityTemplate cet, Collection<String> sqlFields, Map<String, Object> customTableValue) {
		if (sqlFields != null && !sqlFields.isEmpty()) {
			List<CustomFieldTemplate> cfts = sqlFields.stream().map(field -> cache.getCustomFieldTemplate(field, cet.getAppliesTo())).collect(Collectors.toList());

			customTableService.replaceKeys(cfts, customTableValue);
		} else {
			final Collection<CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo()).values();
			customTableService.replaceKeys(cfts, customTableValue);
		}
	}

	private CustomEntityInstance getCustomEntityInstance(String cetCode, String code, Map<String, Object> values) {
		if (code != null) {
			return customEntityInstanceService.findByCodeByCet(cetCode, code);
		}

		final List<CustomEntityInstance> list = customEntityInstanceService.list(cetCode, values);

		if (list.isEmpty()) {
			return null;
		} else if (list.size() > 1) {
			throw new NonUniqueResultException(list.size() + " results found for CEI for CET " + cetCode + " with values " + values);
		} else {
			return list.get(0);
		}

	}
	
	private Map<String, Object> deserializeData(Map<String, Object> values, Collection<CustomFieldTemplate> cfts) {
		Map<String, Object> map = new HashMap<>(values);
		
		cfts.forEach(cft -> {
			Object value = values.get(cft.getCode());
			if(value instanceof String && cft.getStorageType() == CustomFieldStorageTypeEnum.MAP) {
				map.put(cft.getCode(), JacksonUtil.fromString((String) value, Map.class));
			}
		});
		 
		return map;
	}
	
	public void checkBeforeUpdate(Repository repository, CustomEntityInstance entity) throws EntityDoesNotExistsException, ELException, IllegalTransitionException {
		entity.setRepository(repository);
		
		Map<String, Set<String>> map = customEntityInstanceService.getValueCetCodeAndWfTypeFromWF();
		Map<String, Object> values = entity.getCfValuesAsValues();
		if (values != null) {
			if (map.isEmpty()) {
				return;
			}
			
			for (String key : values.keySet()) {
				if (map.keySet().contains(entity.getCetCode()) && map.get(entity.getCetCode()).contains(key)) {
					
					// Skip if value doesn't changes
					if(Objects.equals(entity.getCfValuesOld().getValue(key), entity.getCfValues().getValue(key))) {
						continue;
					}
					
					if (customEntityInstanceService.transitionsFromPreviousState(key, entity)) {
						continue;
					} else {
						throw new IllegalTransitionException((String) entity.getCfValuesOldNullSafe().getValue(key), 
								(String) values.get(key), 
								key);
					}
				}
			}
		}
	}

}

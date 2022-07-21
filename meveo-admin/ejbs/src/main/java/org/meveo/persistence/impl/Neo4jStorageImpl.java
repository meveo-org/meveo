/**
 * 
 */
package org.meveo.persistence.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;
import javax.ejb.EJBException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.elresolver.ELException;
import org.meveo.exceptions.InvalidCustomFieldException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.PersistenceActionResult;
import org.meveo.persistence.StorageImpl;
import org.meveo.persistence.StorageQuery;
import org.meveo.persistence.graphql.GraphQLQueryBuilder;
import org.meveo.persistence.neo4j.base.Neo4jConnectionProvider;
import org.meveo.persistence.neo4j.base.Neo4jDao;
import org.meveo.persistence.neo4j.service.Neo4jService;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.storage.FileSystemService;
import org.meveo.util.PersistenceUtils;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import org.slf4j.Logger;

@RequestScoped
public class Neo4jStorageImpl implements StorageImpl {
	
	private Map<String, Session> neo4jSessions = new HashMap<>();
	private Map<String, Transaction> neo4jTransactions = new HashMap<>();
	
	@Inject
	private Neo4jService neo4jService;
	
	@Inject
	private Neo4jConnectionProvider neo4jConnectionProvider;
	
	@Inject
	private Neo4jDao neo4jDao;
	
	@Inject
	private CustomFieldInstanceService customFieldInstanceService;
	
	@Inject
	private FileSystemService fileSystemService;
	
	@Inject
	private CustomFieldsCacheContainerProvider cache;
	
	@Inject
	private Logger log;
	
	@Override
	public String findEntityIdByValues(Repository repository, CustomEntityInstance cei) {
		try {
			 return neo4jService.findNodeId(
					repository.getNeo4jConfiguration().getCode(), 
					cei.getCet(), 
					cei.getValues(getStorageType())
				);
		} catch (InvalidCustomFieldException e) {
			log.warn("Invalid custom field", e.getMessage());
			return null;
		} catch (ELException | BusinessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean exists(Repository repository, CustomEntityTemplate cet, String uuid) {
		Map<String, Object> val = neo4jDao.findNodeById(repository.getNeo4jConfiguration().getCode(), cet.getCode(), uuid);
		return val != null && !val.isEmpty();
	}

	@Override
	public Map<String, Object> findById(Repository repository, CustomEntityTemplate cet, String uuid, Map<String, CustomFieldTemplate> cfts, Collection<String> selectFields, boolean withEntityReferences) {
		Map<String, Object> values = new HashMap<>();
		boolean foundEntity = false;
		
		List<String> neo4jFields = PersistenceUtils.filterFields(selectFields, cfts, DBStorageType.NEO4J);
		if (!neo4jFields.isEmpty()) {
			try {
				String repoCode = repository.getNeo4jConfiguration().getCode();
				final Map<String, Object> existingValues = neo4jDao.findNodeById(repoCode, cet.getCode(), uuid, neo4jFields);
				if (existingValues != null) {
					foundEntity = true;
					values.putAll(existingValues);
					// We need to fetch every relationship defined as entity references
					if(withEntityReferences || selectFields != null) {
						for(CustomFieldTemplate cft : cfts.values()) {
							if(!withEntityReferences && selectFields != null) { // Skip fields not defined in fetch fields
								if(!selectFields.contains(cft.getCode())) {
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
					return null;
				}
			}
		}
		
		if (foundEntity) {
			return values;
		} else {
			return null;
		}
	}

	public DBStorageType getStorageType() {
		return DBStorageType.NEO4J;
	}
	
	@Override
	public PersistenceActionResult createOrUpdate(Repository repository, CustomEntityInstance cei, Map<String, CustomFieldTemplate> customFieldTemplates, String foundUuid) throws BusinessException {
		
		String uuid = null;
		CustomEntityInstance neo4jCei = new CustomEntityInstance();
		neo4jCei.setCetCode(cei.getCetCode());
		neo4jCei.setUuid(foundUuid);
		neo4jCei.setCet(cei.getCet());
		neo4jCei.setRepository(repository);
		cei.setRepository(repository);
		
		if (cei.getCet().getAvailableStorages().contains(DBStorageType.NEO4J)) {

			Map<String, Object> neo4jValues = PersistenceUtils.filterValues(customFieldTemplates, cei.getValuesNullSafe(), neo4jCei.getCet(), DBStorageType.NEO4J, false);
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
				try {
					updateNeo4jBinaries(repository, cei.getCet(), customFieldTemplates, uuid, neo4jValues);
				} catch (BusinessApiException | IOException e) {
					throw new BusinessException(e);
				}

				return persistenceResult;
			}
		}

		return null;
	}
	
	@Override
	public void update(Repository repository, CustomEntityInstance cei) throws BusinessException {
		Map<String, Object> neo4jValues = cei.getValues(DBStorageType.NEO4J);

		CustomEntityTemplate cet = cei.getCet();
		final List<String> cetLabels = cet.getNeo4JStorageConfiguration().getLabels() != null ? cet.getNeo4JStorageConfiguration().getLabels() : new ArrayList<>();
		List<String> labels = new ArrayList<>(cetLabels);
		labels.add(cet.getCode());

		try {
			updateNeo4jBinaries(repository, cet, cei.getFieldTemplates(), cei.getUuid(), neo4jValues);
		} catch (BusinessApiException | IOException e) {
			throw new BusinessException(e);
		}

		neo4jDao.updateNodeByNodeId(repository.getNeo4jConfiguration().getCode(), cei.getUuid(), cet.getCode(), neo4jValues, labels);
	}
	
	@Override
	public void remove(Repository repository, CustomEntityTemplate cet, String uuid) throws BusinessException {
		neo4jDao.removeNodeByUUID(repository.getNeo4jConfiguration().getCode(), cet.getCode(), uuid);
	}

	@Override
	public void setBinaries(Repository repository, CustomEntityTemplate cet, CustomFieldTemplate cft, String uuid, List<File> binaries) {
		List<String> binariesPaths = binaries.stream().map(File::getPath).collect(Collectors.toList());

		neo4jService.removeBinaries(uuid, repository.getNeo4jConfiguration().getCode(), cet, cft);
		neo4jService.addBinaries(uuid, repository.getNeo4jConfiguration().getCode(), cet, cft, binariesPaths);
	}

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

	@Override
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> find(StorageQuery query) throws EntityDoesNotExistsException {
		final Map<String, CustomFieldTemplate> fields = cache.getCustomFieldTemplates(query.getCet().getAppliesTo());

		// Check if filters contains a field not stored in Neo4J
		var dontFilterOnNeo4J = query.getFilters() != null && query.getFilters().keySet().stream()
				.anyMatch(f -> fields.get(f) != null && !fields.get(f).getStorages().contains(DBStorageType.NEO4J));
		
		if (dontFilterOnNeo4J) {
			return null;
		}
		
		String graphQlQuery;
		List<Map<String, Object>> valueList = new ArrayList<>();

		// Find by graphql if query provided
		if (query.getPaginationConfiguration() != null && query.getPaginationConfiguration().getGraphQlQuery() != null) {
			graphQlQuery = query.getPaginationConfiguration().getGraphQlQuery();
			graphQlQuery = graphQlQuery.replaceAll("([\\w)]\\s*\\{)(\\s*\\w*)", "$1meveo_uuid,$2");
		} else {
			graphQlQuery = generateGraphQlFromPagination(query.getCet().getCode(), query.getPaginationConfiguration(), query.getFetchFields(), query.getFilters(), query.getSubFields())
					.toString();
		}
		
		Map<String, Object> result = neo4jDao.executeGraphQLQuery(query.getRepository().getNeo4jConfiguration().getCode(), graphQlQuery, null, null);
		
		if(result != null) {
			List<Map<String, Object>> values = (List<Map<String, Object>>) result.get(query.getCet().getCode());
			values = values != null ? values : new ArrayList<>();

			values.forEach(map -> {
				final HashMap<String, Object> resultMap = new HashMap<>(map);
				map.forEach((key, mapValue) -> {
					if (!key.equals("uuid") && !key.equals("meveo_uuid") && query.getFetchFields() != null && !query.getFetchFields().contains(key)) {
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
				
				valueList.add(resultMap);
			});
		}
		
		return valueList;
	}
	
	@Override
	public Integer count(Repository repository, CustomEntityTemplate cet, PaginationConfiguration paginationConfiguration) {
		return neo4jService.count(repository, cet, paginationConfiguration);
	}

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
			Map<String, Set<String>> subSubFields = PersistenceUtils.extractSubFields(subFetchFields);
			GraphQLQueryBuilder subQuery = generateGraphQlFromPagination(null, null, subFetchFields, null, subSubFields);
			builder.field(subField.getKey(), subQuery);
		}
		
		return builder;
	}

	@Override
	public void cetCreated(CustomEntityTemplate cet) {
        if (cet.getAvailableStorages() != null && cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
        	neo4jService.addUUIDIndexes(cet);
        }
		
	}

	@Override
	public void crtCreated(CustomRelationshipTemplate crt) {
		// NOOP
		
	}

	@Override
	public void cftCreated(CustomModelObject template, CustomFieldTemplate cft) {
		// NOOP
		
	}

	@Override
	public void removeCft(CustomModelObject template, CustomFieldTemplate cft) {
		// NOOP
	}

	@Override
	public void removeCet(CustomEntityTemplate cet) {
        if (cet.getNeo4JStorageConfiguration() != null && cet.getAvailableStorages() != null && cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            neo4jService.removeCet(cet);
            neo4jService.removeUUIDIndexes(cet);
        }
	}

	@Override
	public void removeCrt(CustomRelationshipTemplate crt) {
		// NOOP
	}

	@Override
	public void cetUpdated(CustomEntityTemplate oldCet, CustomEntityTemplate cet) {
        // Synchronize neoj4 indexes
        if (cet.getAvailableStorages() != null && cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            neo4jService.addUUIDIndexes(cet);
        } else {
            neo4jService.removeUUIDIndexes(cet);
        }
	}

	@Override
	public void crtUpdated(CustomRelationshipTemplate cet) {
		// NOOP
	}

	@Override
	public void cftUpdated(CustomModelObject template, CustomFieldTemplate oldCft, CustomFieldTemplate cft) {
		// NOOP
	}

	@Override
	public PersistenceActionResult addCRTByUuids(Repository repository, CustomRelationshipTemplate crt, Map<String, Object> relationValues, String sourceUuid, String targetUuid) throws BusinessException {
		// Neo4J Storage
		if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
			try {
				return neo4jService.addCRTByNodeIds(repository.getNeo4jConfiguration().getCode(), crt.getCode(), relationValues, sourceUuid, targetUuid);
			} catch (BusinessException e) {
				throw e;
			} catch (ELException e) {
				throw new BusinessException(e);
			}
		}
		
		return null;
	}

	@Override
	public void init() {
	}

	@Override
	public <T> T beginTransaction(Repository repository, int stackedCalls) {
		return (T) getNeo4jTransaction(repository.getNeo4jConfiguration().getCode());
	}

	@Override
	public void commitTransaction(Repository repository) {
		if(repository.getNeo4jConfiguration() != null) {
			Transaction neo4jTx = neo4jTransactions.remove(repository.getNeo4jConfiguration().getCode());
			if(neo4jTx == null) {
				throw new IllegalStateException("No running transaction for " + repository.getCode());
			}
			neo4jTx.success();
			neo4jTx.close();
		}
	}

	@Override
	public void rollbackTransaction(int stackedCalls) {
		neo4jTransactions.values().forEach(Transaction::failure);
		
		if(stackedCalls == 0) {
			neo4jTransactions.values().forEach(Transaction::close);
		}
		
	}

	@Override
	@PreDestroy
	public void destroy() {
		neo4jTransactions.values().forEach(s -> s.close());
		neo4jSessions.values().forEach(Session::close);
		
		neo4jTransactions.clear();
		neo4jSessions.clear();
	}
	
	public Transaction getNeo4jTransaction(String repository) {
		Session session = neo4jSessions.computeIfAbsent(repository, neo4jConnectionProvider::getSession);
		if(session == null) {
			throw new RuntimeException("Can't get session for repository " + repository);
		}
		return neo4jTransactions.computeIfAbsent(repository, code -> session.beginTransaction());
	}
	
	public Transaction getUserManagedTx(String repository) {
		Session session = neo4jSessions.computeIfAbsent(repository, neo4jConnectionProvider::getSession);
		if(session == null) {
			throw new RuntimeException("Can't get session for repository " + repository);
		}
		return session.beginTransaction();
	}


}

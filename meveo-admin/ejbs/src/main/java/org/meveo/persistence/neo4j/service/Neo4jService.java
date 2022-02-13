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

package org.meveo.persistence.neo4j.service;

import static org.meveo.persistence.neo4j.base.Neo4jDao.NODE_ID;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJBException;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ElementNotFoundException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.CETUtils;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.StringUtils;
import org.meveo.elresolver.ELException;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.exceptions.InvalidCustomFieldException;
import org.meveo.export.RemoteAuthenticationException;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.JpaAmpNewTx;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.BusinessEntity;
import org.meveo.model.crm.CustomEntityTemplateUniqueConstraint;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.EntityReferenceWrapper;
import org.meveo.model.crm.Provider;
import org.meveo.model.crm.custom.CustomFieldIndexTypeEnum;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValue;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageTransaction;
import org.meveo.persistence.CustomPersistenceService;
import org.meveo.persistence.PersistenceActionResult;
import org.meveo.persistence.impl.Neo4jStorageImpl;
import org.meveo.persistence.neo4j.base.Neo4jDao;
import org.meveo.persistence.neo4j.graph.Neo4jEntity;
import org.meveo.persistence.neo4j.graph.Neo4jRelationship;
import org.meveo.persistence.scheduler.EntityRef;
import org.meveo.service.base.MeveoValueExpressionWrapper;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateUtils;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.storage.FileSystemService;
import org.meveo.service.storage.RepositoryService;
import org.meveo.util.ApplicationProvider;
import org.meveo.util.PersistenceUtils;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * @author Rachid AITYAAZZA
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @lastModifiedVersion 6.4.0
 */
public class Neo4jService implements CustomPersistenceService {
    private static final Comparator<CustomEntityTemplateUniqueConstraint> CONSTRAINT_COMPARATOR = Comparator
	        .comparingInt(CustomEntityTemplateUniqueConstraint::getTrustScore)
	        .reversed()
	        .thenComparingInt(CustomEntityTemplateUniqueConstraint::getPosition);

	public static final String REPOSITORY_CODE = "$$repositoryCode$$";

	private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jService.class);

    private static final Logger log = LoggerFactory.getLogger(Neo4jService.class);
    private static final String FIELD_KEYS = "fieldKeys";
    private static final String FIELDS = "fields";
    public static final String ID = "id";
    private static final String MEVEO_UUID = "meveo_uuid";
    private static final ConcurrentHashMap<String, Future> mergeTasks = new ConcurrentHashMap<>();
    
    @Inject
    @MeveoJpa
    private EntityManagerWrapper emWrapper;

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    private ScriptInstanceService scriptInstanceService;

    @Inject
    private Neo4jDao neo4jDao;

    @Inject
    @Removed
    private Event<Neo4jEntity> nodeRemovedEvent;

    @Inject
    @Updated
    private Event<Neo4jEntity> nodeUpdatedEvent;

    @Inject
    @Created
    private Event<Neo4jRelationship> edgeCreatedEvent;

    @Inject
    @Updated
    private Event<Neo4jRelationship> edgeUpdatedEvent;

    @Inject
    @ApplicationProvider
    protected Provider appProvider;
    
    @Inject
    private CrossStorageTransaction crossStorageTransaction;

    @Inject
    private CustomFieldsCacheContainerProvider customFieldsCache;
    
    @Inject
    private RepositoryService repositoryService;
    
	@Inject
	private CustomFieldInstanceService customFieldInstanceService;
	
	@Inject
	private FileSystemService fileSystemService;
	
	@Inject
	private CustomFieldsCacheContainerProvider cache;
	
    @Inject
    private Neo4jStorageImpl neo4jStorageImpl;
    
    /**
     * Remove all data concerned with the CET
     *
     * @param cet Template of the data to remove
     */
    public void removeCet(CustomEntityTemplate cet) {
        for (String repositoryCode : getRepositoriesCode()) {
            neo4jDao.removeByLabel(repositoryCode, cet.getCode());
        }
    }

    /**
     * Add an index and unique constraint on the CET for the meveo_uuid property
     *
     * @param customEntityTemplate  {@link CustomEntityTemplate}
     */
    public void addUUIDIndexes(CustomEntityTemplate customEntityTemplate){
        Set<String> labels = new HashSet<>();

        for(CustomEntityTemplate cet : customEntityTemplate.ascendance()){
            labels.add(cet.getCode());
            if(cet.getNeo4JStorageConfiguration() != null && cet.getNeo4JStorageConfiguration().getLabels() != null){
                labels.addAll(cet.getNeo4JStorageConfiguration().getLabels());
            }
        }

        for (String repositoryCode : getRepositoriesCode()) {
            for (String label : labels) {
            	try {
            		neo4jDao.addUniqueConstraint(repositoryCode, label, MEVEO_UUID);
            	} catch (Exception e) {
            		log.error("Failed to add unique constraint on {}(meveo_uuid) for repository {}", label, repositoryCode, e);
            	}
            }
        }
    }

    /**
     * Drop an index and unique constraint on the CET for the meveo_uuid property
     */
    public void removeUUIDIndexes(CustomEntityTemplate customEntityTemplate) {
        if(customEntityTemplate.getAvailableStorages() == null || !customEntityTemplate.getAvailableStorages().contains(DBStorageType.NEO4J)){
            return;
        }

        Set<String> labels = new HashSet<>();
        labels.add(customEntityTemplate.getCode());

        for(CustomEntityTemplate cet : customEntityTemplate.ascendance()){
        	// If super-templates are stored in Neo4J we don't remove indexes
            if(!cet.getAvailableStorages().contains(DBStorageType.NEO4J)){
                labels.add(cet.getCode());
                labels.addAll(cet.getNeo4JStorageConfiguration().getLabels());
            }
        }

        for (String repositoryCode : getRepositoriesCode()) {
            for (String label : labels) {
                neo4jDao.dropUniqueConstraint(repositoryCode, label, MEVEO_UUID);
            }
        }
    }

    /**
     * Retrieves the repostories from db records
     *
     * @return the list of neo4j repositories available
     */
    public Set <String> getRepositoriesCode(){
        List<String> codeList = emWrapper.getEntityManager()
                .createQuery("SELECT c.code from Neo4JConfiguration c WHERE c.disabled = false", String.class)
                .getResultList();
        
        Set<String> codes = new HashSet<>(codeList);
        Repository defaultRepository = repositoryService.findDefaultRepository();
        if (defaultRepository.getNeo4jConfiguration() != null) {
        	codes.add(defaultRepository.getNeo4jConfiguration().getCode());
        }
        
        return codes;
    }


    /**
     * Retrieves the UUID of the source node of an unique relationship
     *
     * @param neo4JConfiguration Repository code
     * @param crt        Template of the relationship from which to retrieve the source entity
     * @param relationUuid UUID of the relationship from which to retrieve the source entity
     * @return the uuid of the source node of the relation
     */
    public String findIdOfSourceEntityByRelationId(String neo4JConfiguration, CustomRelationshipTemplate crt, String relationUuid){
        return neo4jDao.findSourceNodeId(
                neo4JConfiguration,
                crt.getStartNode().getCode(),
                crt.getName(),
                relationUuid
        );

    }

    /**
     * Retrieve the unique relationship instance for the given CRT and target node
     *
     * @param neo4JConfiguration    Repository code
     * @param crt                   Unique CRT to look for
     * @param targetUuid            Target node's UUID
     * @return the UUID of the relationship, or null if none was found
     */
    public String findIdOfUniqueRelationByTargetId(String neo4JConfiguration, CustomRelationshipTemplate crt, String targetUuid) {
        if(!crt.isUnique()){
            throw new IllegalArgumentException("CRT must be unique !");
        }

        final List<String> relationsIds = neo4jDao.findRelationIdByTargetId(
                neo4JConfiguration,
                crt.getEndNode().getCode(),
                crt.getName(),
                targetUuid
        );

        if(relationsIds.isEmpty()){
            return null;
        }

        if(relationsIds.size() > 1){
            log.error("Multiple relationship instances found for relationship {} with target node {}. Please check for data integrity.", crt.getCode(), targetUuid);
        }

        return relationsIds.get(0);
    }
    
	public String findNodeId(String neo4JConfiguration, CustomEntityTemplate cet, CustomEntityInstance cei) throws ELException, BusinessException {

		Map<String, Object> fields = JacksonUtil.convertToMap(cei);
		return findNodeId(neo4JConfiguration, cet, fields);
	}

    public String findNodeId(String neo4JConfiguration, CustomEntityTemplate cet, Map<String, Object> fields) throws ELException, BusinessException {
        final Set<CustomEntityTemplateUniqueConstraint> trustedQueries = cet.getNeo4JStorageConfiguration().getUniqueConstraints()
                .stream()
                .filter(customEntityTemplateUniqueConstraint -> customEntityTemplateUniqueConstraint.getTrustScore().equals(100))
                .filter(uniqueConstraint -> isApplicableConstraint(fields, uniqueConstraint))
                .collect(Collectors.toSet());

        for (CustomEntityTemplateUniqueConstraint trustedQuery : trustedQueries) {
            final Set<String> strings = neo4jDao.executeUniqueConstraint(neo4JConfiguration, trustedQuery, fields, cet.getCode());
            if(strings.size() == 1){
                return strings.iterator().next();
            }
        }

        final Map<String, CustomFieldTemplate> cfts = fields.keySet().stream()
                .map(code -> customFieldsCache.getCustomFieldTemplate(code, cet.getAppliesTo()))
                .collect(
                        Collectors.toMap(BusinessEntity::getCode, Function.identity())
                );

        Map<String, Object> uniqueFields = new HashMap<>();
        validateAndConvertCustomFields(cfts, fields, uniqueFields, true);

        if(uniqueFields.isEmpty()) {
        	return null;
        }
        
        return neo4jDao.findNodeId(neo4JConfiguration, cet.getCode(), uniqueFields);
    }
    
    public PersistenceActionResult addCetNode(String neo4JConfiguration, CustomEntityInstance cei) {
    	return addCetNode(neo4JConfiguration, cei.getCet(), cei.getCfValuesAsValues(), cei.getUuid());
    }

    public PersistenceActionResult addCetNode(String neo4JConfiguration, String cetCode, Map<String, Object> fieldValues) {
        final CustomEntityTemplate cet = customFieldsCache.getCustomEntityTemplate(cetCode);
        return addCetNode(neo4JConfiguration, cet, fieldValues, null);
    }

    public PersistenceActionResult addCetNode(String neo4JConfiguration, CustomEntityTemplate cet, Map<String, Object> fieldValues) {
		return addCetNode(neo4JConfiguration, cet, fieldValues, null);
	}

	public PersistenceActionResult addCetNode(String neo4JConfiguration, CustomEntityTemplate cet, Map<String, Object> fieldValues, String uuid) {

        Set<EntityRef> persistedEntities = new HashSet<>();
        String nodeUuid = null;

        try {

            /* Find unique fields and validate data */
            Map<String, CustomFieldTemplate> cetFields = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());

            // Fallback to when entity is defined as primitive but does not have associated CFT
            if (cet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {
                CustomFieldTemplate valueCft = cetFields.get("value");
                if (valueCft == null || valueCft.getStoragesNullSafe() == null || !valueCft.getStoragesNullSafe().contains(DBStorageType.NEO4J)) {
                    valueCft = new CustomFieldTemplate();
                    CustomEntityTemplateUtils.turnIntoPrimitive(cet, valueCft);
                    customFieldTemplateService.create(valueCft);
                    cetFields.put("value", valueCft);
                }
            }

            Map<String, Object> uniqueFields = new HashMap<>();
            Map<String, Object> fields = validateAndConvertCustomFields(cetFields, fieldValues, uniqueFields, true);

            Map<EntityRef, String> relationshipsToCreate = createEntityReferences(neo4JConfiguration, cet, fieldValues, cetFields, fields);

            /* If pre-persist script was defined, execute it. fieldValues map may be modified by the script */
            executePrePersist(neo4JConfiguration, cet, fields);
            
            // Populate unique fields again after pre presit script as they might have been computed
            validateAndConvertCustomFields(cetFields, fields, uniqueFields, true);

            // Let's make sure that the unique constraints are well sorted by trust score and then sort by their position
            List<CustomEntityTemplateUniqueConstraint> applicableConstraints = cet.getNeo4JStorageConfiguration().getUniqueConstraints()
                    .stream()
                    .filter(uniqueConstraint -> isApplicableConstraint(fields, uniqueConstraint))
                    .sorted(Neo4jService.CONSTRAINT_COMPARATOR)
                    .collect(Collectors.toList());

            final List<String> labels = getAdditionalLabels(cet);
            if (applicableConstraints.isEmpty()) {
                var existingNode = neo4jDao.findNodeById(neo4JConfiguration, cet.getCode(), uuid);
				if (uniqueFields.isEmpty() && (existingNode == null || existingNode.isEmpty())) {
                    String nodeId = neo4jDao.createNode(neo4JConfiguration, cet.getCode(), fields, labels, uuid);
                    
                    if(nodeId != null) {
                    	persistedEntities.add(new EntityRef(nodeId, cet.getCode()));
                    	nodeUuid = nodeId;
                    }
                    
                } else {
                    Map<String, Object> editableFields = getEditableFields(cetFields, fields);
                    String nodeId = neo4jDao.mergeNode(neo4JConfiguration, cet.getCode(), uniqueFields, fields, editableFields, labels, uuid);
                    
                    if(nodeId != null) {
                    	persistedEntities.add(new EntityRef(nodeId, cet.getCode()));
                    	nodeUuid = nodeId;
                    }
                }
                
            } else {
                /* Apply unique constraints */
                boolean appliedUniqueConstraint = false;
                for (CustomEntityTemplateUniqueConstraint uniqueConstraint : applicableConstraints) {
                    Set<String> ids = neo4jDao.executeUniqueConstraint(neo4JConfiguration, uniqueConstraint, fields, cet.getCode());

                    if (uniqueConstraint.getTrustScore() == 100 && ids.size() > 1) {
                        String joinedIds = ids.stream() .map(Object::toString).collect(Collectors.joining(", "));
                        LOGGER.warn("UniqueConstraints with 100 trust score shouldn't return more than 1 ID : duplicated nodes will be merged (code = {}; IDs = {})", uniqueConstraint.getCode(), joinedIds);
                        String id = mergeNodes(neo4JConfiguration, cet, ids);
                        LOGGER.info("Nodes {} were merge into node {}", joinedIds, id);
                        ids = Collections.singleton(id);
                    }

                    for (String id : ids) {
                        appliedUniqueConstraint = true;

                        if (uniqueConstraint.getTrustScore() < 100) {
                            // If the trust rating is lower than 100%, we create the entity and create a relationship between the found one and the created one
                            // XXX: Update the found node too?
                            //TODO: Handle case where the unique constraint query return more than one elements and that the trust score is below 100
                            String createdNodeId = neo4jDao.createNode(neo4JConfiguration, cet.getCode(), fields, labels, uuid);
                            if(createdNodeId != null) {
                                neo4jDao.createRelationBetweenNodes(
                                        neo4JConfiguration,
                                        createdNodeId, cet.getCode(),
                                        "SIMILAR_TO",
                                        id, cet.getCode(),
                                        ImmutableMap.of(
                                                "trustScore", uniqueConstraint.getTrustScore(),
                                                "constraintCode", uniqueConstraint.getCode()
                                        ));
	                            persistedEntities.add(new EntityRef(createdNodeId, cet.getCode()));
	                            persistedEntities.add(new EntityRef(id, uniqueConstraint.getTrustScore(), uniqueConstraint.getCode(), cet.getCode()));
                            }
                            
                        } else {
                            Map<String, Object> updatableFields = new HashMap<>(getEditableFields(cetFields, fields));
                            uniqueFields.keySet().forEach(updatableFields::remove);

                            neo4jDao.updateNodeByNodeId(neo4JConfiguration, id, cet.getCode(), updatableFields, labels);
                            persistedEntities.add(new EntityRef(id, cet.getCode()));
                        }
                        
                        nodeUuid = id;
                    }

                    if (appliedUniqueConstraint) {
                        break;
                    }
                }

                if (!appliedUniqueConstraint) {
                    if (uniqueFields.isEmpty()) {
                        String nodeId = neo4jDao.createNode(neo4JConfiguration, cet.getCode(), fields, labels, uuid);
                        if(nodeId != null) {
                        	persistedEntities.add(new EntityRef(nodeId, cet.getCode()));
                            nodeUuid = nodeId;
                        }
                    } else {
                        Map<String, Object> editableFields = getEditableFields(cetFields, fields);

                        String nodeId = neo4jDao.mergeNode(neo4JConfiguration, cet.getCode(), uniqueFields, fields, editableFields, labels, uuid);
                        if(nodeId != null) {
                        	persistedEntities.add(new EntityRef(nodeId, cet.getCode()));
                        	nodeUuid = nodeId;
                        }
                    }
                }
            }

            /* Create relationships collected in the relationshipsToCreate map */
            for (EntityRef entityRef : persistedEntities) {
                if (!entityRef.isTrusted()) {
                    continue;
                }

                for (Entry<EntityRef, String> relationshipsEntry : relationshipsToCreate.entrySet()) {
                    EntityRef relatedEntityRef = relationshipsEntry.getKey();
                    if (!relatedEntityRef.isTrusted()) {
                        continue;
                    }

                    String relationshipType = relationshipsEntry.getValue();
                    final Map<String, Object> values = Collections.emptyMap();
                    neo4jDao.createRelationBetweenNodes(
                            neo4JConfiguration,
                            entityRef.getUuid(), entityRef.getLabel(),
                            relationshipType, relatedEntityRef.getUuid(),
                            relatedEntityRef.getLabel(),
                            values
                    );
                }
            }
            
        } catch (BusinessException e) {
            log.error("addCetNode cet={}, errorMsg={}", cet, e.getMessage(), e);
            
        } catch (ELException e) {
            log.error("Error while resolving EL : ", e);
        }
        
        return new PersistenceActionResult(persistedEntities, nodeUuid);
    }

	private void executePrePersist(String neo4JConfiguration, CustomEntityTemplate cet, Map<String, Object> fieldValues) throws BusinessException {
		if (cet.getPrePersistScript() != null) {
			log.warn("Pre persist script usage will be dropped in future releases. Please use the crud event listener script instead");
			fieldValues.put(Neo4jService.REPOSITORY_CODE, neo4JConfiguration);
			scriptInstanceService.execute(cet.getPrePersistScript().getCode(), fieldValues);
			fieldValues.remove(Neo4jService.REPOSITORY_CODE);
		}
	}

	/**
	 * @param neo4JConfiguration
	 * @param cet
	 * @param fieldValues
	 * @param cetFields
	 * @param fields
	 * @return
	 * @throws BusinessException
	 */
	private Map<EntityRef, String> createEntityReferences(String neo4JConfiguration, CustomEntityTemplate cet, Map<String, Object> fieldValues, Map<String, CustomFieldTemplate> cetFields, Map<String, Object> fields) throws BusinessException {
		/* Collect entity references */
		final List<CustomFieldTemplate> entityReferences = cetFields.values().stream()
		        .filter(customFieldTemplate -> customFieldTemplate.getFieldType().equals(CustomFieldTypeEnum.ENTITY))   // Entity references
		        .filter(customFieldTemplate -> fieldValues.get(customFieldTemplate.getCode()) != null)                  // Value is provided
		        .collect(Collectors.toList());

		/* Create referenced nodes and collect relationships to create */
		Map<EntityRef, String> relationshipsToCreate = new HashMap<>();  // Map where the id of the target node is the key and the label of relationship is the value
		for (CustomFieldTemplate entityReference : entityReferences) {
		    Object referencedCetValue = fieldValues.get(entityReference.getCode());
		    String referencedCetCode = entityReference.getEntityClazzCetCode();
		    CustomEntityTemplate referencedCet = customFieldsCache.getCustomEntityTemplate(referencedCetCode);
		    
		    if(referencedCetValue instanceof EntityReferenceWrapper) {
		    	EntityReferenceWrapper wrapper = (EntityReferenceWrapper) referencedCetValue;
		    	if(wrapper.getUuid() == null) {
		    		continue;
		    	}
		    }

		    Collection<Object> values;
		    if (entityReference.getStorageType().equals(CustomFieldStorageTypeEnum.LIST)) {
		        if (!(referencedCetValue instanceof Collection)) {
		            throw new BusinessException("Value for CFT " + entityReference.getCode() + " of CET " + cet.getCode() + " should be a collection");
		        }

		        values = ((Collection<Object>) referencedCetValue);
		        if (referencedCet.getNeo4JStorageConfiguration() != null && referencedCet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {
		            fields.put(entityReference.getCode(), new ArrayList<>());
		        }
		    } else {
		        values = Collections.singletonList(referencedCetValue);
		    }

		    for (Object value : values) {
		        Set<EntityRef> relatedPersistedEntities = new HashSet<>();
		        if (referencedCet.getNeo4JStorageConfiguration() != null && referencedCet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {
		            Map<String, Object> valueMap = new HashMap<>();
		            valueMap.put("value", value);

		            // If there is no unique constraints defined, directly merge node
		            if (referencedCet.getNeo4JStorageConfiguration().getUniqueConstraints().isEmpty()) {
		                List<String> additionalLabels = getAdditionalLabels(referencedCet);
	                	executePrePersist(neo4JConfiguration, referencedCet, valueMap);
		                String createdNodeId = neo4jDao.mergeNode(neo4JConfiguration, referencedCetCode, valueMap, valueMap, valueMap, additionalLabels, null);
		                if(createdNodeId != null) {
		                	relatedPersistedEntities.add(new EntityRef(createdNodeId, referencedCet.getCode()));
		                }
		            } else {
		                PersistenceActionResult persistenceResult = addCetNode(neo4JConfiguration, referencedCetCode, valueMap);
		                relatedPersistedEntities.addAll(persistenceResult.getPersistedEntities());
		            }

		            if (entityReference.getStorageType().equals(CustomFieldStorageTypeEnum.LIST)) {
		                ((List<Object>) fields.get(entityReference.getCode())).add(valueMap.get("value"));
		            } else {
		                fields.put(entityReference.getCode(), valueMap.get("value"));
		            }

		        } else {
		            // Referenced CET is not primitive
		            if (value instanceof Map && referencedCet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
		                Map<String, Object> valueMap = (Map<String, Object>) value;
		                PersistenceActionResult persistenceResult = addCetNode(neo4JConfiguration, referencedCet, valueMap);
						relatedPersistedEntities.addAll(persistenceResult.getPersistedEntities());

		            } else if(value instanceof String){ 
		                // If entity reference's value is a string and the entity reference is not primitive, then the value is likely the UUID of the referenced node
		                handleUuidReference(neo4JConfiguration, cet, relationshipsToCreate, entityReference, referencedCet, value);

		            } else if(value instanceof EntityReferenceWrapper) {
		                handleUuidReference(neo4JConfiguration, cet, relationshipsToCreate, entityReference, referencedCet, ((EntityReferenceWrapper) value).getUuid());
		            
		        	} else if(value instanceof Collection) {
		            	for(Object item : (Collection<?>) value) {
		            		if(item instanceof String) {
		            			handleUuidReference(neo4JConfiguration, cet, relationshipsToCreate, entityReference, referencedCet, value);
		            		}
		            	}
		            	
		            } else if(referencedCet.getAvailableStorages().contains(DBStorageType.NEO4J)){
		                throw new IllegalArgumentException("CET " + referencedCetCode + " should be a primitive entity");
		            }
		        }

		        if (relatedPersistedEntities != null) {
		            String relationshipName = Optional.ofNullable(entityReference.getRelationshipName())
		            		.orElseGet(() -> entityReference.getRelationship() != null ? entityReference.getRelationship().getName() : null);
		            
		            if(relationshipName == null) {
		            	throw new BusinessException(entityReference.getAppliesTo() + "#" + entityReference.getCode() + ": Relationship name must be provided !");
		            }
		            
		            for (EntityRef entityRef : relatedPersistedEntities) {
		                relationshipsToCreate.put(entityRef, relationshipName);
		            }
		        }
		    }
		}
		return relationshipsToCreate;
	}

	/**
	 * @param neo4JConfiguration
	 * @param cet
	 * @param relationshipsToCreate
	 * @param entityReference
	 * @param referencedCet
	 * @param value
	 * @throws IllegalArgumentException
	 */
	public void handleUuidReference(String neo4JConfiguration, CustomEntityTemplate cet, Map<EntityRef, String> relationshipsToCreate, CustomFieldTemplate entityReference, CustomEntityTemplate referencedCet, Object value) throws IllegalArgumentException {
		UUID.fromString((String) value);

		if(StringUtils.isBlank(entityReference.getRelationshipName())){
		    String errorMessage  = String.format("Attribute relationshipName of CFT %s#%s should not be null", cet.getCode(), entityReference.getCode());
		    throw new IllegalArgumentException(errorMessage);
		}
		relationshipsToCreate.put(new EntityRef((String) value, referencedCet.getCode()), entityReference.getRelationshipName());

		// Create a node reprensenting the value if the target is not stored in Neo4J
		if(!referencedCet.getAvailableStorages().contains(DBStorageType.NEO4J)){
		    neo4jDao.mergeNode(
		            neo4JConfiguration,
		            referencedCet.getCode(),
		            Collections.singletonMap(MEVEO_UUID, value),
		            Collections.singletonMap(MEVEO_UUID, value),
		            Collections.emptyMap(),
		            Collections.emptyList(), null
		    );
		}
	}

    /**
     * Persist an instance of {@link CustomRelationshipTemplate}
     *
     * @param neo4JConfiguration Neo4J coordinates
     * @param crtCode            Code of the CustomRelationshipTemplate instance
     * @param crtValues          Properties of the link
     * @param startFieldValues   Filters on start node
     * @param endFieldValues     Filters on end node
     * @throws BusinessException If error happens
     */
    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public PersistenceActionResult addCRTByNodeValues(
            String neo4JConfiguration,
            String crtCode,
            Map<String, Object> crtValues,
            Map<String, Object> startFieldValues,
            Map<String, Object> endFieldValues)
            throws BusinessException, ELException {

        log.info("Persisting link with crtCode = {}", crtCode);

        /* Try to retrieve the associated CRT */
        CustomRelationshipTemplate customRelationshipTemplate = customFieldsCache.getCustomRelationshipTemplate(crtCode);
        if (customRelationshipTemplate == null) {
            log.error("Can't find CRT with code {}", crtCode);
            throw new ElementNotFoundException(crtCode, CustomRelationshipTemplate.class.getName());
        }

        /* If pre-persist script was defined, execute it. fieldValues map may be modified by the script */
    	executePrePersist(neo4JConfiguration, customRelationshipTemplate.getStartNode(), startFieldValues);
    	executePrePersist(neo4JConfiguration, customRelationshipTemplate.getEndNode(), endFieldValues);

        /* Recuperation of the custom fields of the CRT */
        Map<String, CustomFieldTemplate> crtCustomFields = customFieldTemplateService.findByAppliesTo(customRelationshipTemplate.getAppliesTo());
        log.info("Custom fields are : {}", crtCustomFields);

        /* Recuperation of the custom fields of the source node */
        Map<String, CustomFieldTemplate> startNodeFields = customFieldTemplateService.findByAppliesTo(customRelationshipTemplate.getStartNode().getAppliesTo());
        Map<String, Object> startNodeFieldValues = validateAndConvertCustomFields(startNodeFields, startFieldValues, null, true);
        log.info("Filters on start node :" + startNodeFieldValues);
        Map<String, Object> startNodeKeysMap = getNodeKeys(customRelationshipTemplate.getStartNode().getAppliesTo(), startNodeFieldValues);

        /* Recuperation of the custom fields of the target node */
        Map<String, CustomFieldTemplate> endNodeFields = customFieldTemplateService.findByAppliesTo(customRelationshipTemplate.getEndNode().getAppliesTo());
        Map<String, Object> endNodeFieldValues = validateAndConvertCustomFields(endNodeFields, endFieldValues, null, true);
        log.info("Filters on end node : " + endNodeFieldValues);
        Map<String, Object> endNodeKeysMap = getNodeKeys(customRelationshipTemplate.getEndNode().getAppliesTo(), endNodeFieldValues);

        log.info("startNodeKeysMap:" + startNodeKeysMap);
        log.info("endNodeKeysMap:" + endNodeKeysMap);

        /* If matching source and target exists, persist the link */
        if (startNodeKeysMap.size() > 0 && endNodeKeysMap.size() > 0) {
            Map<String, Object> crtFields = validateAndConvertCustomFields(crtCustomFields, crtValues, null, true);
            final List<String> relationIds = saveCRT2Neo4j(neo4JConfiguration, customRelationshipTemplate, startNodeKeysMap, endNodeKeysMap, crtFields, false);
            if(!relationIds.isEmpty()) {
                return new PersistenceActionResult(relationIds.get(0));
            }
        }

        return null;

    }

    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public PersistenceActionResult addCRTByNodeIds(
            String neo4JConfiguration,
            String crtCode,
            Map<String, Object> crtValues,
            String startNodeId,
            String endNodeId
    ) throws BusinessException, ELException {

        log.info("Persisting link with crtCode = {}", crtCode);

        /* Try to retrieve the associated CRT */
        CustomRelationshipTemplate customRelationshipTemplate = customFieldsCache.getCustomRelationshipTemplate(crtCode);
        if (customRelationshipTemplate == null) {
            log.error("Can't find CRT with code {}", crtCode);
            throw new ElementNotFoundException(crtCode, CustomRelationshipTemplate.class.getName());
        }

        /* Recuperation of the custom fields of the CRT */
        Map<String, CustomFieldTemplate> crtCustomFields = customFieldTemplateService.findByAppliesTo(customRelationshipTemplate.getAppliesTo());
        log.info("Custom fields are : {}", crtCustomFields);

        Map<String, Object> crtFields = validateAndConvertCustomFields(crtCustomFields, crtValues, null, true);

        final List<String> relationIds = saveCRT2Neo4jByNodeIds(neo4JConfiguration, customRelationshipTemplate, startNodeId, endNodeId, crtFields, false);
        if(!relationIds.isEmpty()) {
            return new PersistenceActionResult(relationIds.get(0));
        }

        return null;
    }

    /**
     * Save CRT to Neo4j
     *
     * @param neo4JConfiguration         Neo4J coordinates
     * @param customRelationshipTemplate Template of the CRT
     * @param startNodeKeysMap           Unique fields values of the start node
     * @param endNodeKeysMap             Unique fields values of the start node
     * @param crtFields                  Fields values of the relationship
     */
    public List<String> saveCRT2Neo4j(String neo4JConfiguration,
                              CustomRelationshipTemplate customRelationshipTemplate, Map<String, Object> startNodeKeysMap,
                              Map<String, Object> endNodeKeysMap, Map<String, Object> crtFields, boolean isTemporaryCET) {

        final String relationshipAlias = "relationship";    // Alias to use in query

        // Build values map
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("startAlias", Neo4JRequests.START_NODE_ALIAS);
        valuesMap.put("endAlias", Neo4JRequests.END_NODE_ALIAS);
        valuesMap.put("startNode", customRelationshipTemplate.getStartNode().getCode());
        valuesMap.put("endNode", customRelationshipTemplate.getEndNode().getCode());
        valuesMap.put("relationType", customRelationshipTemplate.getName());
        valuesMap.put("starNodeKeys", Values.value(startNodeKeysMap));
        valuesMap.put("endNodeKeys", Values.value(endNodeKeysMap));
        valuesMap.put("updateDate", isTemporaryCET ? -1 : System.currentTimeMillis());
        final String fieldsString = neo4jDao.getFieldsString(crtFields.keySet());
        valuesMap.put(FIELDS, fieldsString);
        valuesMap.putAll(crtFields);
        valuesMap.put(NODE_ID, UUID.randomUUID().toString());

        // Build the statement
        StringBuffer statement = neo4jDao.appendReturnStatement(Neo4JRequests.crtStatement, relationshipAlias, valuesMap);
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String resolvedStatement = sub.replace(statement);

        // Begin Neo4J transaction
        final Transaction transaction = neo4jStorageImpl.getNeo4jTransaction(neo4JConfiguration);

        List<Record> recordList = new ArrayList<>();

        try {
            /* Execute query and parse result inside a relationship.
            If relationship was created fire creation event, fire update event when updated. */

            final StatementResult result = transaction.run(resolvedStatement, valuesMap);  // Execute query

            // Fire notification for each relation created or updated
            recordList = result.list();

            transaction.success();  // Commit transaction

        } catch (Exception e) {
            log.error("Failed to save relationship", e);
            transaction.failure();
        }

        List<String> relationUuids = new ArrayList<>();

        for (Record record : recordList) {
            final Neo4jRelationship relationship = new Neo4jRelationship(record.get(relationshipAlias).asRelationship(), neo4JConfiguration);  // Parse relationship
            if(relationship.containsKey(MEVEO_UUID)){
                relationUuids.add(relationship.get(MEVEO_UUID).asString());
            }

            if (relationship.containsKey("update_date") || relationship.containsKey("updateDate")) {  // Check if relationship contains the "update_date" key
                edgeUpdatedEvent.fire(relationship);        // Fire update event if contains the key
            } else {
                edgeCreatedEvent.fire(relationship);        // Fire creation event if does not contains the key
            }
        }

        return relationUuids;
    }

    public List<String> saveCRT2Neo4jByNodeIds(String neo4JConfiguration, CustomRelationshipTemplate customRelationshipTemplate, String startNodeId,
                                       String endNodeId, Map<String, Object> crtFields, boolean isTemporaryCET) {

        final String relationshipAlias = "relationship";    // Alias to use in query

        // Build values map
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("startAlias", Neo4JRequests.START_NODE_ALIAS);
        valuesMap.put("endAlias", Neo4JRequests.END_NODE_ALIAS);
        valuesMap.put("startNode", customRelationshipTemplate.getStartNode().getCode());
        valuesMap.put("endNode", customRelationshipTemplate.getEndNode().getCode());
        valuesMap.put("relationType", customRelationshipTemplate.getName());
        valuesMap.put("startNodeId", startNodeId);
        valuesMap.put("endNodeId", endNodeId);
        valuesMap.put("updateDate", isTemporaryCET ? -1 : System.currentTimeMillis());
        final String fieldsString = neo4jDao.getFieldsString(crtFields.keySet());
        valuesMap.put(FIELDS, fieldsString);
        valuesMap.putAll(crtFields);
        valuesMap.put(NODE_ID, UUID.randomUUID().toString());

        // Build the statement
        StringBuffer statement;

        if (customRelationshipTemplate.isUnique()) {
            statement = neo4jDao.appendReturnStatement(Neo4JRequests.uniqueCrtStatementByNodeIds, relationshipAlias, valuesMap);
        } else {
            statement = neo4jDao.appendReturnStatement(Neo4JRequests.crtStatementByNodeIds, relationshipAlias, valuesMap);
        }
        
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String resolvedStatement = sub.replace(statement);

        // Begin Neo4J transaction
        final Transaction transaction = neo4jStorageImpl.getNeo4jTransaction(neo4JConfiguration);

        List<Record> recordList = new ArrayList<>();

        try {
            /* Execute query and parse result inside a relationship.
            If relationship was created fire creation event, fire update event when updated. */
            LOGGER.info(resolvedStatement);

            final StatementResult result = transaction.run(resolvedStatement, valuesMap);  // Execute query

            // Fire notification for each relation created or updated
            recordList = result.list();

            transaction.success();  // Commit transaction

        } catch (Exception e) {
            log.error("Failed to save relationship", e);
            transaction.failure();
        }

        List<String> relationUuids = new ArrayList<>();

        for (Record record : recordList) {
            final Neo4jRelationship relationship = new Neo4jRelationship(record.get(relationshipAlias).asRelationship(), neo4JConfiguration);  // Parse relationship
            if(relationship.containsKey(MEVEO_UUID)){
                relationUuids.add(relationship.get(MEVEO_UUID).asString());
            }

            if (relationship.containsKey("update_date") || relationship.containsKey("updateDate")) {  // Check if relationship contains the "update_date" key
                edgeUpdatedEvent.fire(relationship);        // Fire update event if contains the key
            } else {
                edgeCreatedEvent.fire(relationship);        // Fire creation event if does not contains the key
            }
        }

        return relationUuids;
    }

    /**
     * Persist a source node of an unique relationship.
     * If a relationship that targets the target node exists, then we merge the fields of the start in parameter to
     * the fields of the source node of the relationship.
     * If such a relation does not exists, we create the source node with it fields.
     *
     * @param neo4JConfiguration Neo4J coordinates
     * @param crtCode            Code of the unique relation
     * @param startNodeValues    Values to assign to the start node
     * @param endNodeValues      Filters on the target node values
     */
    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public PersistenceActionResult addSourceNodeUniqueCrt(String neo4JConfiguration,
                                       String crtCode,
                                       Map<String, Object> startNodeValues,
                                       Map<String, Object> endNodeValues) throws BusinessException, ELException {

        // Get relationship template
        final CustomRelationshipTemplate customRelationshipTemplate = customFieldsCache.getCustomRelationshipTemplate(crtCode);

        final CustomEntityTemplate endNode = customRelationshipTemplate.getEndNode();
        
        // Extract unique fields values for the start node
        Map<String, CustomFieldTemplate> endNodeCfts = customFieldTemplateService.findByAppliesTo(endNode.getAppliesTo());
        Map<String, CustomFieldTemplate> startNodeCfts = customFieldTemplateService.findByAppliesTo(customRelationshipTemplate.getStartNode().getAppliesTo());
        final Map<String, Object> endNodeUniqueFields = new HashMap<>();
        Map<String, Object> endNodeConvertedValues = validateAndConvertCustomFields(endNodeCfts, endNodeValues, endNodeUniqueFields, true);
        Map<String, Object> startNodeConvertedValues = validateAndConvertCustomFields(startNodeCfts, startNodeValues, null, true);

        // Map the variables declared in the statement
        Map<String, Object> valuesMap = new HashMap<>();
        final String cetCode = customRelationshipTemplate.getStartNode().getCode();
        valuesMap.put("cetCode", cetCode);
        valuesMap.put("crtCode", crtCode);
        valuesMap.put("endCetcode", customRelationshipTemplate.getEndNode().getCode());

        // Prepare the key maps for unique fields and start node fields
        final String uniqueFieldStatements = neo4jDao.getFieldsString(endNodeConvertedValues.keySet());
        final String startNodeValuesStatements = neo4jDao.getFieldsString(startNodeConvertedValues.keySet());

        // No unique fields has been found
        if (endNodeUniqueFields.isEmpty()) {
        	// If no unique fields are provided / defined, retrieve the meveo_uuid of the target node using unicity rules
        	Set<String> ids = endNode.getNeo4JStorageConfiguration().getUniqueConstraints()
                .stream()
                .filter(uniqueConstraint -> uniqueConstraint.getTrustScore() == 100)
                .filter(uniqueConstraint -> isApplicableConstraint(endNodeValues, uniqueConstraint))
                .sorted(Neo4jService.CONSTRAINT_COMPARATOR)
                .map(uniqueConstraint -> neo4jDao.executeUniqueConstraint(neo4JConfiguration, uniqueConstraint, endNodeValues, endNode.getCode()))
                .findFirst()
                .orElse(Set.of());
        	
        	if(ids.isEmpty()) {
	            log.error("At least one unique field must be provided for target entity [code = {}, fields = {}]. " +
	                    "Unique fields are : {}", customRelationshipTemplate.getEndNode().getCode(), endNodeValues, endNodeUniqueFields);
	            throw new BusinessException("Unique field must be provided");
        	}
        	
        	if (ids.size() > 1) {
        		throw new BusinessException(String.format("Multiple targets for unique relationship %s : %s.", crtCode, ids));
        	}
        	
        	String id = ids.iterator().next();
        	endNodeValues.put("meveo_uuid", id);
        	endNodeUniqueFields.put("meveo_uuid", id);
        }

        // Assign the keys names
        valuesMap.put(FIELD_KEYS, uniqueFieldStatements);
        valuesMap.put(FIELDS, startNodeValuesStatements);

        // Create the substitutor
        StrSubstitutor sub = new StrSubstitutor(valuesMap);

        // Values of the keys defined in valuesMap
        Map<String, Object> parametersValues = new HashMap<>();
        parametersValues.putAll(startNodeConvertedValues);
        parametersValues.putAll(endNodeConvertedValues);

        final Transaction transaction = neo4jStorageImpl.getNeo4jTransaction(neo4JConfiguration);

        // Try to find the id of the source node
        String findStartNodeStatement = getStatement(sub, Neo4JRequests.findStartNodeId);
        final StatementResult run = transaction.run(findStartNodeStatement, parametersValues);

        Neo4jEntity startNode = null;

        try {
            try {

                /* Update the source node with the found id */

                final String startNodeAlias = "startNode";   // Alias to use in queries

                // Retrieve ID of the node
                final Record idRecord = run.single();
                final Value id = idRecord.get(0);
                parametersValues.put(NODE_ID, id);

                // Create statement
                CustomEntityTemplate startCet = customRelationshipTemplate.getStartNode();
                List<String> additionalLabels = getAdditionalLabels(startCet);

                final Map<String, Object> updatableValues = valuesMap.entrySet().stream()
                        .filter(s -> startNodeCfts.get(s.getKey()).isAllowEdit())
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

                StringBuffer statement = neo4jDao.appendAdditionalLabels(Neo4JRequests.updateNodeWithId, additionalLabels, startNodeAlias, updatableValues);
                statement = neo4jDao.appendReturnStatement(statement, startNodeAlias, valuesMap);
                String updateStatement = getStatement(sub, statement);

                final StatementResult result = transaction.run(updateStatement, parametersValues);  // Execute query

                // Fire node update event
                startNode = new Neo4jEntity(result.single().get(startNodeAlias).asNode(), neo4JConfiguration);

            } catch (NoSuchRecordException e) {

                /* Create the source node */

                addCetNode(neo4JConfiguration, customRelationshipTemplate.getStartNode(), startNodeValues, null);

            }

            transaction.success();

        } catch (Exception e) {

            transaction.failure();
            log.error("Transaction for persisting entity with code {} and fields {} was rolled back", cetCode, startNodeValues, e);
            throw new BusinessException(e);

        }

        if (startNode != null) {
            nodeUpdatedEvent.fire(startNode);
            return new PersistenceActionResult(startNode.get("meveo_uuid").asString());
        } else {
            return null;
        }
    }
    
    public void deleteEntity(String neo4jConfiguration, String cetCode, CustomEntityInstance cei) throws BusinessException {
    	
    	deleteEntity(neo4jConfiguration, cetCode, cei.getCfValuesAsValues());
    }

    public void deleteEntity(String neo4jConfiguration, String cetCode, Map<String, Object> values) throws BusinessException {

        /* Get entity template */
        final CustomEntityTemplate customEntityTemplate = customFieldsCache.getCustomEntityTemplate(cetCode);

        /* Extract unique fields values for node */
        final Map<String, Object> uniqueFields = getNodeKeys(customEntityTemplate.getAppliesTo(), values);

        /* No unique fields has been found */
        if (uniqueFields.isEmpty()) {
            throw new BusinessException("At least one unique field must be provided for cet to delete");
        }

        final String uniqueFieldStatement = neo4jDao.getFieldsString(uniqueFields.keySet());

        /* Map the variables declared in the statement */
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("cetCode", cetCode);
        valuesMap.put("uniqueFields", uniqueFieldStatement);

        String deleteStatement = getStatement(new StrSubstitutor(valuesMap), Neo4JRequests.deleteCet);

        final Transaction transaction = neo4jStorageImpl.getNeo4jTransaction(neo4jConfiguration);

        InternalNode internalNode = null;

        try {

            /* Delete the node and all its associated relationships and fire node deletion event */

            // Execute query
            final StatementResult result = transaction.run(deleteStatement, values);
            for (Record record : result.list()) {

                // Fire deletion event
                final Map<String, Value> properties = record.get("properties").asMap(e -> e);   // Parse properties
                final List<String> labels = record.get("labels").asList(Value::asString);       // Parse labels
                final long id = record.get("id").asLong();                                      // Parse id
                internalNode = new InternalNode(id, labels, properties);     // Create Node object
            }
            transaction.success();

        } catch (Exception e) {

            log.error("Cannot delete node with code {} and values {}", cetCode, values, e);
            transaction.failure();
            throw new BusinessException(e);

        }

        if (internalNode != null) {
            nodeRemovedEvent.fire(new Neo4jEntity(internalNode, neo4jConfiguration));    // Fire notification
        }

    }

    public Response callNeo4jRest(String baseurl, String url, String username, String password, String body) {
        try {
            ResteasyClient client = new ResteasyClientBuilder().build();
            ResteasyWebTarget target = client.target(baseurl + url);
            log.info("callNeo4jRest {} with body : {}", baseurl + url, body);
            BasicAuthentication basicAuthentication = new BasicAuthentication(username, password);
            target.register(basicAuthentication);
            Response response = target.request().post(Entity.json(body));
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED || response.getStatus() == HttpURLConnection.HTTP_FORBIDDEN) {
                    throw new RemoteAuthenticationException("Http status " + response.getStatus() + ", info " + response.getStatusInfo().getReasonPhrase());
                } else {
                    log.debug("Http status " + response.getStatus() + ", info " + response.getStatusInfo().getReasonPhrase());
                }
            }
            return response;
        } catch (Exception e) {
            log.error("Failed to communicate with neo4j. Reason {}", (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()), e);
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings({"rawtypes"})
    public Map<String, Object> validateAndConvertCustomFields(Map<String, CustomFieldTemplate> customFieldTemplates, Map<String, Object> fieldValues, Map<String, Object> uniqueFields, boolean checkCustomFields) throws BusinessException, ELException {
        Map<String, Object> convertedFields = new HashMap<>();
        for (Entry<String, CustomFieldTemplate> cftEntry : customFieldTemplates.entrySet()) {
            CustomFieldTemplate cft = cftEntry.getValue();
            if (checkCustomFields && cft == null) {
                log.error("No custom field template found with code={} for entity {}. Value will be ignored.", cftEntry.getKey(), CustomFieldTemplate.class);
                throw new InvalidCustomFieldException("Custom field template with code " + cftEntry.getKey() + " not found.");
            }
            Object fieldValue = fieldValues.get(cftEntry.getKey());
            try {

                // Check that field should be stored in Neo4J
                if(cft.getStoragesNullSafe() == null){
                    log.error("No storages configured for CFT {}#{}", cft.getAppliesTo(), cft.getCode());
                    continue;
                }

                if(!cft.getStoragesNullSafe().contains(DBStorageType.NEO4J)){
                    continue;
                }

                // Set default value
                if (fieldValue == null && cft.getDefaultValue() != null) {
                    if (cft.getFieldType() == CustomFieldTypeEnum.EXPRESSION) {
                        fieldValue = MeveoValueExpressionWrapper.evaluateExpression(cft.getDefaultValue(), (Map<Object, Object>) (Map) fieldValues, String.class);
                    } else {
                        fieldValue = MeveoValueExpressionWrapper.evaluateExpression(cft.getDefaultValue(), (Map<Object, Object>) (Map) fieldValues, Object.class);
                    }
                }

                // Validate that value is not empty when field is mandatory
                boolean isEmpty = fieldValue == null && (cft.getFieldType() != CustomFieldTypeEnum.EXPRESSION);
                if (cft.isValueRequired() && isEmpty && cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
                    final String cetCode = CustomEntityTemplate.getCodeFromAppliesTo(cft.getAppliesTo());
                    final CustomEntityTemplate customEntityTemplate = customFieldsCache.getCustomEntityTemplate(cetCode);

                    // If entity is not a custom relation or is a cusotm entity but is not a primitive entity, throw exception
                    if(customEntityTemplate == null || !customEntityTemplate.getNeo4JStorageConfiguration().isPrimitiveEntity()){
                        final String message = customEntityTemplate.getCode() + "#" + cft.getCode() + " is not provided. Non-primitive Neo4j storage.";
                        throw new IllegalArgumentException(message);
                    }

                }
                // Validate that value is valid (min/max, regexp). When
                // value is a list or a map, check separately each value
                if (fieldValue != null
                        && (cft.getFieldType() == CustomFieldTypeEnum.STRING || cft.getFieldType() == CustomFieldTypeEnum.SECRET || cft.getFieldType() == CustomFieldTypeEnum.DOUBLE ||
                        cft.getFieldType() == CustomFieldTypeEnum.LONG || cft.getFieldType() == CustomFieldTypeEnum.BOOLEAN ||
                        cft.getFieldType() == CustomFieldTypeEnum.EXPRESSION || cft.getFieldType() == CustomFieldTypeEnum.LIST)) {
                    List valuesToCheck = new ArrayList<>();
                    if (fieldValue instanceof Map) {
                        // Skip Key item if Storage type is Matrix
                        if (cft.getStorageType() == CustomFieldStorageTypeEnum.MATRIX) {
                            for (Entry<String, Object> mapEntry : ((Map<String, Object>) fieldValue).entrySet()) {
                                if (CustomFieldValue.MAP_KEY.equals(mapEntry.getKey())) {
                                    continue;
                                }
                                valuesToCheck.add(mapEntry.getValue());
                            }
                        } else {
                            valuesToCheck.add(fieldValue);
                        }
                    } else if (fieldValue instanceof List) {
                        convertedFields.put(cft.getCode(), fieldValue);
                        continue;
                    } else {
                        valuesToCheck.add(fieldValue);
                    }
                    //TODO: Make CF Values validation at higher level : maybe in CustomFieldInstanceService#setCfValues
                    for (Object valueToCheck : valuesToCheck) {
                    	
                    	// Validate LIST type
                    	if(cft.getFieldType() == CustomFieldTypeEnum.LIST && !"null".equals(valueToCheck)) {
                    		String stringValue = String.valueOf(valueToCheck);
                    		if(!cft.getListValues().containsKey(stringValue)) {
                    			throw new InvalidCustomFieldException("Value for field " + cft.getCode() + " is not in the list " + cft.getListValues().keySet());
                    		}
                            fieldValue = ((String) fieldValue).trim().replaceAll("'", "’").replaceAll("\"", "").trim();
                    	}
                    	
                    	// Validate STRING type
                    	else if (cft.getFieldType() == CustomFieldTypeEnum.STRING && !"null".equals(valueToCheck)) {
                            String stringValue;
                            if (valueToCheck instanceof Number) {
                                stringValue = valueToCheck.toString();
                            } else if (valueToCheck instanceof Map) {
                                String mapToJson = JacksonUtil.toString(valueToCheck);
                                convertedFields.put(cft.getCode(), mapToJson);
                                continue;
                            } else {
                                stringValue = String.valueOf(valueToCheck);
                            }
                            stringValue = stringValue.trim().replaceAll("'", "’").replaceAll("\"", "");
                            stringValue = stringValue.replaceAll("\n", " ");

                            if (cft.getIndexType()==CustomFieldIndexTypeEnum.INDEX_NEO4J ){
                        		convertedFields.put(cft.getCode() + "_IDX", CETUtils.stripAndFormatFields(stringValue));
                            }
                            
                            if (cft.getMaxValue() == null) {
                                cft.setMaxValue(CustomFieldTemplate.DEFAULT_MAX_LENGTH_STRING);
                            }
                            // Validate String length
                            if (stringValue.length() > cft.getMaxValue()) {
                                throw new InvalidCustomFieldException("Custom field " + cft.getCode() + " value " + stringValue + " length is longer then " + cft.getMaxValue() + " symbols");
                                // Validate String regExp
                            } else if (cft.getRegExp() != null) {
                                try {
                                    Pattern pattern = Pattern.compile(cft.getRegExp());
                                    Matcher matcher = pattern.matcher(stringValue);
                                    if (!matcher.matches()) {
                                        throw new InvalidCustomFieldException("Custom field " + cft.getCode() + " value " + stringValue + " does not match regular expression "
                                                + cft.getRegExp());
                                    }
                                } catch (PatternSyntaxException pse) {
                                    throw new InvalidCustomFieldException("Custom field " + cft.getCode() + " definition specifies an invalid regular expression " + cft.getRegExp());
                                }
                            }
                            if (fieldValue instanceof String) {
                                fieldValue = ((String) fieldValue).trim().replaceAll("'", "’").replaceAll("\"", "").trim();
                            }

                        } 
                        
                        // Validate LONG type
                        else if (cft.getFieldType() == CustomFieldTypeEnum.LONG) {
                            Long longValue;
                            if (valueToCheck instanceof Number) {
                                longValue = ((Number) valueToCheck).longValue();
                            } else if (valueToCheck instanceof String) {
                                longValue = Long.parseLong((String) valueToCheck);
                            } else {
                                longValue = (Long) valueToCheck;
                            }

                            if (cft.getMaxValue() != null && longValue.compareTo(cft.getMaxValue()) > 0) {
                                throw new InvalidCustomFieldException("Custom field " + cft.getCode() + " value " + longValue + " is bigger then " + cft.getMaxValue()
                                        + ". Allowed value range is from " + (cft.getMinValue() == null ? "unspecified" : cft.getMinValue()) + " to "
                                        + (cft.getMaxValue() == null ? "unspecified" : cft.getMaxValue()) + ".");

                            } else if (cft.getMinValue() != null && longValue.compareTo(cft.getMinValue()) < 0) {
                                throw new InvalidCustomFieldException("Custom field " + cft.getCode() + " value " + longValue + " is smaller then " + cft.getMinValue()
                                        + ". Allowed value range is from " + (cft.getMinValue() == null ? "unspecified" : cft.getMinValue()) + " to "
                                        + (cft.getMaxValue() == null ? "unspecified" : cft.getMaxValue()) + ".");
                            }
                            
                            fieldValue = longValue;
                        } 
                        
                    	// Validate DOUBLE type
                        else if (cft.getFieldType() == CustomFieldTypeEnum.DOUBLE) {
                            Double doubleValue;
                            if (valueToCheck instanceof Number) {
                                doubleValue = ((Number) valueToCheck).doubleValue();
                            } else if (valueToCheck instanceof String) {
                                doubleValue = Double.parseDouble((String) valueToCheck);

                            } else {
                                doubleValue = (Double) valueToCheck;
                            }
                            if (cft.getMaxValue() != null && doubleValue.compareTo(cft.getMaxValue().doubleValue()) > 0) {
                                throw new InvalidCustomFieldException("Custom field " + cft.getCode() + " value " + doubleValue + " is bigger then " + cft.getMaxValue()
                                        + ". Allowed value range is from " + (cft.getMinValue() == null ? "unspecified" : cft.getMinValue()) + " to "
                                        + (cft.getMaxValue() == null ? "unspecified" : cft.getMaxValue()) + ".");
                            } else if (cft.getMinValue() != null && doubleValue.compareTo(cft.getMinValue().doubleValue()) < 0) {
                                throw new InvalidCustomFieldException("Custom field " + cft.getCode() + " value " + doubleValue + " is smaller then " + cft.getMinValue()
                                        + ". Allowed value range is from " + (cft.getMinValue() == null ? "unspecified" : cft.getMinValue()) + " to "
                                        + (cft.getMaxValue() == null ? "unspecified" : cft.getMaxValue()) + ".");
                            }
                            
                            fieldValue = doubleValue;
                        }
                    	
                        if (cft.isUnique() && uniqueFields != null) {
                            uniqueFields.put(cft.getCode(), fieldValue);
                        }
                        
                        convertedFields.put(cft.getCode(), fieldValue);
                    }
                }
                
                // Validate EXPRESSION type
                if (cft.getFieldType() == CustomFieldTypeEnum.EXPRESSION) {
                    fieldValue = setExpressionField(fieldValues, cft, convertedFields);
                    convertedFields.put(cft.getCode(), fieldValue);
                    if (cft.isUnique() && uniqueFields != null) {
                        uniqueFields.put(cft.getCode(), fieldValue);
                    }
                }
                
                if (cft.getFieldType() == CustomFieldTypeEnum.DATE) {
                	if (fieldValue instanceof Number) {
                        convertedFields.put(cft.getCode(), ((Number) fieldValue).longValue());
                	} else if (fieldValue instanceof String) {
                        convertedFields.put(cft.getCode(), Long.parseLong((String) fieldValue));
                	} else if (fieldValue instanceof Date) {
                        convertedFields.put(cft.getCode(), ((Date) fieldValue).getTime());
                	} else if(fieldValue instanceof Instant) {
                        convertedFields.put(cft.getCode(), ((Instant) fieldValue).toEpochMilli());
                	}
                	
                	if (fieldValue != null) {
                        if (cft.isUnique() && uniqueFields != null) {
                            uniqueFields.put(cft.getCode(), convertedFields.get(cft.getCode()));
                        }
                	}
                	
                }
                
                
            } catch (NumberFormatException e) {
                if (fieldValue != null) {
                    LOGGER.error(
                            "Wrong data type format for {}#{}. Expected type : {}, value is : {}. Skipping field value",
                            cft.getAppliesTo(),
                            cft.getCode(),
                            cft.getFieldType(),
                            fieldValue
                    );
                }
            }
        }

        return convertedFields;

    }

    @Override
    public PersistenceActionResult addSourceEntityUniqueCrt(Repository repository, String relationCode, Map<String, Object> sourceValues, Map<String, Object> targetValues) throws ELException, BusinessException {
        return addSourceNodeUniqueCrt(repository.getNeo4jConfiguration().getCode(), relationCode, sourceValues, targetValues);
    }

    @Override
    public PersistenceActionResult createOrUpdate(Repository repository, CustomEntityInstance cei) throws BusinessException {
    	return addCetNode(repository.getNeo4jConfiguration().getCode(), cei);
    }

    @Override
    public PersistenceActionResult addCRTByValues(Repository repository, String relationCode, Map<String, Object> relationValues, Map<String, Object> sourceValues, Map<String, Object> targetValues) throws ELException, BusinessException {
        return addCRTByNodeValues(repository.getNeo4jConfiguration().getCode(), relationCode, relationValues, sourceValues, targetValues);
    }

    @Override
    public PersistenceActionResult addCRTByUuids(Repository repository, String relationCode, Map<String, Object> relationValues, String sourceUuid, String targetUuid) throws ELException, BusinessException {
        return addCRTByNodeIds(repository.getNeo4jConfiguration().getCode(), relationCode, relationValues, sourceUuid, targetUuid);
    }

    /**
     * Merge all nodes properties and relationships onto the oldest node, and delete the other.
     *
     * @param configurationCode    Code of the neo4j instance
     * @param customEntityTemplate Template of the entities to merge
     * @param uuids                UUIDs of the nodes to merge
     * @return UUID of the merged node
     */
    public String mergeNodes(String configurationCode, CustomEntityTemplate customEntityTemplate, Collection<String> uuids) {
        List<Node> nodesToTreat = neo4jDao.orderNodesAscBy(Neo4JRequests.CREATION_DATE, uuids, configurationCode, customEntityTemplate.getCode());

        Node persistentNode = nodesToTreat.remove(0);

        for (Node nodeToMerge : nodesToTreat) {
            String hash = nodeToMerge.get(MEVEO_UUID).asString() + "/"  + persistentNode.get(MEVEO_UUID).asString();
            Future mergeTask = mergeTasks.computeIfAbsent(hash, k -> mergeNode(configurationCode, customEntityTemplate, persistentNode, nodeToMerge));

            try {
                mergeTask.get();
            } catch (Exception e) {
                log.error("Merge task for merging {} into {} has been aborted", nodeToMerge, persistentNode, e);
            } finally {
                mergeTasks.remove(hash);
            }
        }

        return persistentNode.get(MEVEO_UUID).asString();
    }

    @Asynchronous
    public AsyncResult<Void> mergeNode(String configurationCode, CustomEntityTemplate customEntityTemplate, Node persistentNode, Node nodeToMerge) {
        LOGGER.info("Merging node {} into node {}", nodeToMerge.get(MEVEO_UUID), persistentNode.get(MEVEO_UUID));

        final List<Relationship> relationships = neo4jDao.findRelationships(configurationCode, nodeToMerge.id(), customEntityTemplate.getCode());
        List<Long> relIds = relationships.stream().map(org.neo4j.driver.v1.types.Entity::id).collect(Collectors.toList());

        LOGGER.info("Merging relationships {} of node {} into node {}", relIds, nodeToMerge.id(), persistentNode.id());
        for (Relationship relationship : relationships) {
            Map<String, Object> uniqueFields = new HashMap<>();
            final Map<String, Object> relationProperties = relationship.asMap();
            final String type = relationship.type();

            final List<String> correspondingCrts = customRelationshipTemplateService.findByCetAndName(customEntityTemplate, type);

            try {
                // Determine unique fields
                if (correspondingCrts.isEmpty()) {
                    log.warn("No CRT available for relation type {} and node label {}", type, customEntityTemplate.getCode());
                    uniqueFields = relationProperties;  // If we do not find the CRT, we consider every field as unique
                } else {
                    final Map<String, CustomFieldTemplate> customFieldTemplates = customFieldsCache.getCustomFieldTemplates(CustomRelationshipTemplate.getAppliesTo(correspondingCrts.get(0)));
                    validateAndConvertCustomFields(customFieldTemplates, relationProperties, uniqueFields, true);
                    if (correspondingCrts.size() > 1) {
                        log.warn("Multiple CRT available for relation type {} and nodeToMerge label {} : {}", type, customEntityTemplate.getCode(), correspondingCrts);
                    }
                }
            }catch(Exception e){
                log.error("Cannot determine unique fields for relation {}", type, e);
                uniqueFields = relationProperties;
            }

            Long creationOrUpdateDate = (Long) (relationProperties.containsKey(Neo4JRequests.INTERNAL_UPDATE_DATE) ?
                    relationProperties.get(Neo4JRequests.INTERNAL_UPDATE_DATE) :
                    relationProperties.get(Neo4JRequests.CREATION_DATE));

            Long sourceId = relationship.startNodeId() == nodeToMerge.id() ? persistentNode.id() : relationship.startNodeId();
            Long targetId = relationship.endNodeId() == nodeToMerge.id() ? persistentNode.id() : relationship.endNodeId();

            neo4jDao.mergeRelationshipById(
                    configurationCode,
                    sourceId,
                    targetId,
                    relationship.get(MEVEO_UUID).asString(),
                    type,
                    uniqueFields,
                    relationProperties,
                    creationOrUpdateDate
            );

        }

        LOGGER.info("Merging properties of node {} into node {} then removing node {}", nodeToMerge.id(), persistentNode.id(), nodeToMerge.id());

        List<String> updatableKeys = customFieldsCache.getCustomFieldTemplates(customEntityTemplate.getAppliesTo())
                .values()
                .stream()
                .filter(customFieldTemplate -> customFieldTemplate.isAllowEdit() && !customFieldTemplate.isUnique())
                .map(CustomFieldTemplate::getCode)
                .collect(Collectors.toList());

        neo4jDao.mergeAndRemoveNodes(configurationCode, persistentNode.id(), nodeToMerge.id(), updatableKeys);

        return new AsyncResult<>(null);
    }

    /**
	 * Remove the binaries attached to the source node
	 *
	 * @param neo4jConfigurationCode Code of the configuration to update
	 * @param sourceNodeUuid         Source node id
	 * @param cet                    Template of the source node
	 * @param customFieldTemplate    Field holding the binary
	 */
    public void removeBinaries(String sourceNodeUuid, String neo4jConfigurationCode, CustomEntityTemplate cet, CustomFieldTemplate customFieldTemplate) {
    	if(customFieldTemplate.getRelationshipName() == null) {
    		throw new IllegalArgumentException("Relationship name of field template " + cet.getCode() + "." + customFieldTemplate.getCode() + " is null");
    	}

    	neo4jDao.detachDeleteTargets(
    			neo4jConfigurationCode,
    			sourceNodeUuid,
    			cet.getCode(),
    			customFieldTemplate.getRelationshipName(),
    			Neo4JConstants.FILE_LABEL
		);
    }

    //TODO: Document
    public void removeBinary(String uuid, String neo4jConfigurationCode, CustomEntityTemplate cet, CustomFieldTemplate customFieldTemplate, String binaryPath) {
    	if(customFieldTemplate.getRelationshipName() == null) {
    		throw new IllegalArgumentException("Relationship name of field template " + cet.getCode() + "." + customFieldTemplate.getCode() + " is null");
    	}

    	neo4jDao.detachDeleteTargets(
    			neo4jConfigurationCode,
    			uuid,
    			cet.getCode(),
    			customFieldTemplate.getRelationshipName(),
    			Neo4JConstants.FILE_LABEL,
    			Collections.singletonMap("value", binaryPath)
		);
    }

	/**
	 * Remove all previous binaries and add the given one to the specified node
	 *
	 * @param uuid                   Id of the node to attach the binary
	 * @param neo4jConfigurationCode Neo4J instance to use
	 * @param cet                    Template of the entity to attach the binary
	 * @param customFieldTemplate    Field holding the binary
	 * @param binaryPath             Path of the binary on the file system
	 */
	public void updateBinary(String uuid, String neo4jConfigurationCode, CustomEntityTemplate cet, CustomFieldTemplate customFieldTemplate, String binaryPath) {
		removeBinaries(uuid, neo4jConfigurationCode, cet, customFieldTemplate);
		addBinaries(uuid, neo4jConfigurationCode, cet, customFieldTemplate, Collections.singletonList(binaryPath));
	}

    /**
     * Retrieve all binaries references associated to a given entity for a given property
     *
     * @param uuid                   UUID of the entity
     * @param neo4jConfigurationCode Code of the Neo4J Instance
     * @param cet                    Template of the entity
     * @param customFieldTemplate    Template of the entity's field where the binary is referenced
     */
    public List<String> findBinaries(String uuid, String neo4jConfigurationCode, CustomEntityTemplate cet, CustomFieldTemplate customFieldTemplate) {
        List<Node> binaryNodes = neo4jDao.findNodesBySourceNodeIdAndRelationships(neo4jConfigurationCode, uuid, cet.getCode(), customFieldTemplate.getRelationshipName(), Neo4JConstants.FILE_LABEL);
        return binaryNodes.stream().map(n -> n.get("value").asString()).collect(Collectors.toList());
    }

	/**
	 * Remove all previous binaries and add the given one to the specified node
	 *
	 * @param uuid                   Id of the node to attach the binary
	 * @param neo4jConfigurationCode Neo4J instance to use
	 * @param cet                    Template of the entity to attach the binary
	 * @param customFieldTemplate    Field holding the binary
	 * @param binariesPath           Paths of the binaries on the file system
	 */
    public void addBinaries(String uuid, String neo4jConfigurationCode, CustomEntityTemplate cet, CustomFieldTemplate customFieldTemplate, Collection<String> binariesPath) {
        for(String binaryPath : binariesPath) {
            final String fileUuid = neo4jDao.mergeNode(
                    neo4jConfigurationCode,
                    Neo4JConstants.FILE_LABEL,
                    Collections.singletonMap("value", binaryPath),
                    Collections.singletonMap("value", binaryPath),
                    Collections.singletonMap("value", binaryPath),
                    null, null
            );

            neo4jDao.createRelationBetweenNodes(
                    neo4jConfigurationCode,
                    uuid,
                    cet.getCode(),
                    customFieldTemplate.getRelationshipName(),
                    fileUuid,
                    Neo4JConstants.FILE_LABEL,
                    Collections.emptyMap()
            );
        }
    }
    
    public int count(Repository repository, CustomEntityTemplate cet, PaginationConfiguration paginationConfiguration) {
		if(repository.getNeo4jConfiguration() == null) {
			return 0;
		}
	
		String graphQlQuery;

		// Find by graphql if query provided
		if (paginationConfiguration != null && paginationConfiguration.getGraphQlQuery() != null) {
			graphQlQuery = paginationConfiguration.getGraphQlQuery();
		} else {
			graphQlQuery = "{ " + cet.getCode() + " { } }";
		}

		graphQlQuery = graphQlQuery.replaceAll("([\\w)]\\s*\\{)(\\s*\\w*)", "$1meveo_uuid,$2");

		final Map<String, Object> result = neo4jDao.executeGraphQLQuery(repository.getNeo4jConfiguration().getCode(), graphQlQuery, null, null);
		if(result == null) {
			return 0;
		}
		return result.size();
    }

    private Object setExpressionField(Map<String, Object> fieldValues, CustomFieldTemplate cft, Map<String, Object> convertedFields) throws ELException {

        Object evaluatedExpression = MeveoValueExpressionWrapper.evaluateExpression(cft.getDefaultValue(), fieldValues.entrySet().stream()
                   .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()!=null?e.getValue():"")), String.class);

        if (evaluatedExpression != null) {

            log.info("validateAndConvertCustomFields {} ExpressionFieldValue1={}", cft.getCode(), evaluatedExpression);

            evaluatedExpression = evaluatedExpression.toString().replaceAll("'", "’").replaceAll("\"", "").replaceAll("-null", "").replaceAll("-", "");
            evaluatedExpression = evaluatedExpression.toString().replaceAll("\n", " ");
            if (cft.getExpressionSeparator() != null) {
                String duplicateSeparator = cft.getExpressionSeparator() + cft.getExpressionSeparator();
                while (evaluatedExpression.toString().contains(duplicateSeparator)) {
                    evaluatedExpression = evaluatedExpression.toString().replaceAll(duplicateSeparator, cft.getExpressionSeparator());
                }
                evaluatedExpression = evaluatedExpression.toString().endsWith(cft.getExpressionSeparator()) ? evaluatedExpression.toString().substring(0, evaluatedExpression.toString().length() - 1) : evaluatedExpression.toString();
                evaluatedExpression = evaluatedExpression.toString().startsWith(cft.getExpressionSeparator()) ? evaluatedExpression.toString().substring(1) : evaluatedExpression.toString();
            }
            log.info("validateAndConvertCustomFields {} ExpressionFieldValue2={}", cft.getCode(), evaluatedExpression);
            Object fieldValue = !StringUtils.isBlank(evaluatedExpression) ? evaluatedExpression : fieldValues.get(cft.getCode());
            if (fieldValue != null) {
                if (cft.getIndexType() == CustomFieldIndexTypeEnum.INDEX_NEO4J) {
                    convertedFields.put(cft.getCode() + "_IDX", CETUtils.stripAndFormatFields(fieldValue.toString().toLowerCase()));
                } else if (cft.isUnique()) {
                    fieldValue = CETUtils.stripAndFormatFields(fieldValue.toString());
                }
                fieldValues.put(cft.getCode(), fieldValue);
            }
            return fieldValue;
        } else {
            return null;
        }
    }

    private Map<String, Object> getEditableFields(Map<String, CustomFieldTemplate> cetFields, Map<String, Object> convertedFields) {
        Map<String, Object> editableFields = new HashMap<>(convertedFields);
        cetFields.values()
            .stream()
            .filter(customFieldTemplate -> !customFieldTemplate.isAllowEdit() || customFieldTemplate.isUnique())
            .map(CustomFieldTemplate::getCode)
            .forEach(editableFields::remove);
        return editableFields;
    }

    private boolean isApplicableConstraint(Map<String, Object> fields, CustomEntityTemplateUniqueConstraint uniqueConstraint) {

        Map<Object, Object> userMap = new HashMap<>();
        userMap.put("entity", fields);

        if (StringUtils.isBlank(uniqueConstraint.getApplicableOnEl())) {
            return true;
        }

        try {
            Object isApplicable = MeveoValueExpressionWrapper.evaluateExpression(uniqueConstraint.getApplicableOnEl(), userMap, Boolean.class);
            if (isApplicable != null && !(isApplicable instanceof Boolean)) {
                LOGGER.error("Expression " + uniqueConstraint.getApplicableOnEl() + " do not evaluate to boolean but " + isApplicable.getClass());
                return false;
            } else if (isApplicable != null) {
                return (boolean) isApplicable;
            }
        } catch (ELException e) {
            LOGGER.error("Cannot evaluate expression", e);
            return false;
        }

        return false;
    }

    private Map<String, Object> getNodeKeys(String appliesTo, Map<String, Object> convertedFieldValues) {
        Map<String, Object> nodeKeysMap = new HashMap<>();
        List<CustomFieldTemplate> retrievedCft = customFieldTemplateService.findCftUniqueFieldsByApplies(appliesTo);
        for (CustomFieldTemplate cf : retrievedCft) {
            if (!StringUtils.isBlank(convertedFieldValues.get(cf.getCode()))) {
                nodeKeysMap.put(cf.getCode(), convertedFieldValues.get(cf.getCode()));
            }
        }
        return nodeKeysMap;
    }

    private List<String> getAdditionalLabels(CustomEntityTemplate cet) {
        List<String> additionalLabels = new ArrayList<>(cet.getNeo4JStorageConfiguration().getLabels());
        additionalLabels.addAll(getAllSuperTemplateLabels(cet));
        return additionalLabels;
    }

    private List<String> getAllSuperTemplateLabels(CustomEntityTemplate customEntityTemplate) {
        List<String> labels = new ArrayList<>();
        CustomEntityTemplate parent = customEntityTemplate.getSuperTemplate();
        while (parent != null) {
            labels.add(parent.getCode());
            parent = parent.getSuperTemplate();
        }
        return labels;
    }

    private String getStatement(StrSubstitutor sub, StringBuffer findStartNodeId) {
        return sub.replace(findStartNodeId).replace('"', '\'');
    }
    
}

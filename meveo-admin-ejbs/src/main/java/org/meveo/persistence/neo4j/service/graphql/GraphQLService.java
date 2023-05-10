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

package org.meveo.persistence.neo4j.service.graphql;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.customEntities.GraphQLQueryField;
import org.meveo.model.customEntities.Mutation;
import org.meveo.model.neo4j.GraphQLRequest;
import org.meveo.model.neo4j.Neo4JConfiguration;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.persistence.neo4j.base.Neo4jDao;
import org.meveo.persistence.neo4j.service.Neo4JConstants;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.service.neo4j.Neo4jConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class GraphQLService {

    @Inject
    private Neo4jDao neo4jDao;

    private static Logger log = LoggerFactory.getLogger(GraphQLService.class);

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;
    
    @Inject
    private Neo4jConfigurationService neo4jConfigurationService;
    
    @Inject
    @MeveoJpa
    private EntityManagerWrapper entityManagerWrapper;
    
    @Inject
    private GraphQlClient graphQlClient;
    
    private static String getRelationshipDirective(Neo4JConfiguration neo4jConfiguration) {
    	if (neo4jConfiguration.getDbVersion().startsWith("3")) {
    		return "@relation(name: \"";
    	} else {
    		return "@relationship(type: \"";
    	}
    }

    public Map<String, Object> executeGraphQLRequest(GraphQLRequest graphQLRequest, String neo4jConfiguration) {
    	Neo4JConfiguration neo4jRepo = neo4jConfigurationService.findByCode(neo4jConfiguration);
    	if (neo4jRepo.getGraphqlApiUrl() != null) {
    		return graphQlClient.executeGraphQlRequest(graphQLRequest, neo4jRepo.getGraphqlApiUrl());
    	} else {
    		return neo4jDao.executeGraphQLQuery(
    				neo4jConfiguration,
    				graphQLRequest.getQuery(),
    				graphQLRequest.getVariables(),
    				graphQLRequest.getOperationName()
				);
    	}
    }

    public Map<String, Object> executeGraphQLRequest(String query, String neo4jConfiguration) {
    	Neo4JConfiguration neo4jRepo = neo4jConfigurationService.findByCode(neo4jConfiguration);
    	if (neo4jRepo.getGraphqlApiUrl() != null) {
    		GraphQLRequest graphQLRequest = new GraphQLRequest();
    		graphQLRequest.setQuery(query);
    		return graphQlClient.executeGraphQlRequest(graphQLRequest, neo4jRepo.getGraphqlApiUrl());
    	} else {
    		return neo4jDao.executeGraphQLQuery(neo4jConfiguration, query, null, null);
    	}
    }

    /**
     * Update the IDL for every neo4j repositories
     */
    public void updateIDL() {
        final List<String> neo4jConfigurations = entityManagerWrapper.getEntityManager()
                .createQuery("SELECT c.code from Neo4JConfiguration c WHERE disabled = false", String.class)
                .getResultList();

        for (String neo4jConfiguration : neo4jConfigurations) {
        	updateIDL(neo4jConfiguration);
        }

    }

    /**
     * Update the IDL for the specified neo4j repository
     *
     * @param neo4jConfiguration Repository to update
     */
    public void updateIDL(String neo4jConfiguration) {
    	Neo4JConfiguration neo4jRepo = neo4jConfigurationService.findByCode(neo4jConfiguration);
    	
    	Instant start = Instant.now();
    	log.debug("Computing IDL ...");
        final Collection<GraphQLEntity> entities = getEntities(neo4jRepo);
        String idl = getIDL(entities, neo4jRepo);
    	log.debug("IDL computation took {}ms", start.until(Instant.now(), ChronoUnit.MILLIS));
        List<String> missingEntities = validateIdl(idl, neo4jRepo);
        if (CollectionUtils.isEmpty(missingEntities)) {
        	if (neo4jRepo.getGraphqlApiUrl() != null) {
        		graphQlClient.updateIdl(idl, neo4jRepo.getGraphqlApiUrl());
        	} else {
        		neo4jDao.updateIDL(neo4jConfiguration, idl);
        	}
        } else{
            log.error("Cannot update IDL, missing entities : {} in IDL \n{}", missingEntities, idl);
        }
    }

    public String getIDL(Neo4JConfiguration neo4jConfiguration) {
        final Collection<GraphQLEntity> entities = getEntities(neo4jConfiguration);
        return getIDL(entities, neo4jConfiguration);
    }

    private String getIDL(Collection<GraphQLEntity> graphQLEntities, Neo4JConfiguration neo4jConfiguration) {
        StringBuilder idl = new StringBuilder();
        List<GraphQLEntity> sortedEntities = new ArrayList<>(graphQLEntities);
        sortedEntities.sort((e1, e2) -> {
        	if (e1.isInterface() && !e2.isInterface()) {
        		return -1;
        	}
        	
        	if (e2.isInterface() && !e1.isInterface()) {
        		return 1;
        	}
        	
        	return 0;
        });
        
        for (GraphQLEntity graphQLEntity : sortedEntities) {
            // Skip if entity ended not having fields
            if (graphQLEntity.getGraphQLFields().isEmpty()) {
                continue;
            }
            
        	idl.append(graphQLEntity.isInterface() ? "interface " : "type ").append(graphQLEntity.getName()).append(" {\n");
            
            for (GraphQLField graphQLField : graphQLEntity.getGraphQLFields()) {
                idl.append("\t").append(graphQLField.getFieldName()).append(": ");

                if (graphQLField.isMultivialued()) {
                    idl.append("[");
                }
                idl.append(graphQLField.getFieldType());

                if (neo4jConfiguration.getDbVersion().startsWith("3")) {
                    if (graphQLField.isMultivialued()) {
                        idl.append("]");
                    }
                    
                    if (graphQLField.isRequired()) {
                        idl.append("!");
                    }
                } else {
                	 if (graphQLField.isMultivialued()) {
                         idl.append("!]!");
                     } else if (graphQLField.isRequired()) {
                         idl.append("!");
                     }
                }
  

                if (graphQLField.getQuery() != null) {
                    idl.append(" ").append(graphQLField.getQuery());
                }
                
                if (StringUtils.isNoBlank(graphQLField.getDefaultValue())) {
                	if (graphQLField.getFieldType().equals("Boolean") || graphQLField.getFieldType().equals("Int")) {
                		idl.append(" ").append("@coalesce(value: " + graphQLField.getDefaultValue() + ")");
                	} else if (!graphQLField.getFieldType().equals("BigInt")){
                		idl.append(" ").append("@coalesce(value: \"" + graphQLField.getDefaultValue() + "\")");
                	}
                }

                idl.append("\n");

            }

            idl.append("}\n\n");
        }
        
        idl.append(getMutations(neo4jConfiguration));

        return idl.toString();
    }

    private Collection<GraphQLEntity> getEntities(Neo4JConfiguration neo4jConfiguration) {
        Map<String, GraphQLEntity> graphQLEntities = new TreeMap<>();

        // Binary entity
        add(graphQLEntities, Neo4JConstants.BINARY_ENTITY);

        // Entities
        final List<CustomEntityTemplate> ceTsWithSubTemplates = customEntityTemplateService.getCETsWithSubTemplates();
        final Map<String, CustomEntityTemplate> cetsByName = ceTsWithSubTemplates
                .stream()
                .collect(Collectors.toMap(CustomEntityTemplate::getCode, Function.identity()));

        for (CustomEntityTemplate cet : cetsByName.values()) {

            final Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.getCftsWithInheritedFields(cet);
            GraphQLEntity graphQLEntity = new GraphQLEntity();
            graphQLEntity.setName(cet.getCode());

            SortedSet<GraphQLField> graphQLFields = getGraphQLFields(cfts, neo4jConfiguration);

            // Always add "meveo_uuid" field
            graphQLFields.add(new GraphQLField("meveo_uuid", "String", true));

            if (cet.getAvailableStorages() != null && cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
                // Additional queries defined
                for (GraphQLQueryField graphqlQueryField : Optional.ofNullable(cet.getNeo4JStorageConfiguration().getGraphqlQueryFields()).orElse(Collections.emptyList())) {
                    GraphQLField graphQLField = new GraphQLField();
                    graphQLField.setQuery(graphqlQueryField.getQuery());
                    graphQLField.setFieldType(graphqlQueryField.getFieldType());
                    graphQLField.setFieldName(graphqlQueryField.getFieldName());
                    graphQLField.setMultivalued(graphqlQueryField.isMultivalued());
                    graphQLFields.add(graphQLField);
                }

                // Primitive type
                if (cet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {
                    final boolean valueExists = graphQLFields.stream().anyMatch(f -> f.getFieldName().equals("value"));
                    if (!valueExists) {
                        GraphQLField value = new GraphQLField();
                        switch (cet.getNeo4JStorageConfiguration().getPrimitiveType()) {
                            case STRING:
                                value.setFieldType("String");
                                break;
                            case LONG:
                            case DATE:
                                value.setFieldType("GraphQLLong");
                                break;
                            case DOUBLE:
                                value.setFieldType("GraphQLBigDecimal");
                                break;
                        }
                        value.setFieldName("value");
                        value.setMultivalued(false);
                        value.setRequired(true);
                        graphQLFields.add(value);
                    }
                }
            }

            graphQLEntity.setGraphQLFields(graphQLFields);
            add(graphQLEntities, graphQLEntity);
        }

        // Relationships
        final List<CustomRelationshipTemplate> customRelationshipTemplates = customRelationshipTemplateService.list();

        for (CustomRelationshipTemplate relationshipTemplate : customRelationshipTemplates) {

            final CustomEntityTemplate endNode = relationshipTemplate.getEndNode();
            final CustomEntityTemplate startNode = relationshipTemplate.getStartNode();

            // IF either the relation, the start entity or the end entity is not configured to be stored in Neo4J, don't include it in the generated graphql
            final Set<DBStorageType> relationStorages = relationshipTemplate.getAvailableStorages() != null ? relationshipTemplate.getAvailableStorages() : new HashSet<>();
            final Set<DBStorageType> endNodeStorages = endNode.getAvailableStorages() != null ? endNode.getAvailableStorages() : new HashSet<>();
            final Set<DBStorageType> startNodeStorages = startNode.getAvailableStorages() != null ? startNode.getAvailableStorages() : new HashSet<>();

            if (!relationStorages.contains(DBStorageType.NEO4J) || !endNodeStorages.contains(DBStorageType.NEO4J) || !startNodeStorages.contains(DBStorageType.NEO4J)) {
                continue;
            }

            // Create Graphql relationship type
            final Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(relationshipTemplate.getAppliesTo());
            GraphQLEntity graphQLEntity = new GraphQLEntity();

            String typeName = relationshipTemplate.getGraphQlTypeName() == null ? endNode.getCode() + "Relation" : relationshipTemplate.getGraphQlTypeName();
            if (neo4jConfiguration.getDbVersion().startsWith("3")) {
            	graphQLEntity.setName(typeName);
            } else {
            	graphQLEntity.setName(typeName + " @relationshipProperties");
            }
            
            graphQLEntity.setInterface(true);
            
            String propertiesInterface = cfts == null || cfts.isEmpty() ? ")" : ", properties: \"" + typeName + "\")";

            SortedSet<GraphQLField> graphQLFields = getGraphQLFields(cfts, neo4jConfiguration);

            graphQLEntity.setGraphQLFields(graphQLFields);

            // Add fields to sources (and sub-sources)
            cetsByName.get(startNode.getCode())
                    .descendance()
                    .stream()
                    .map(sourceCet -> graphQLEntities.get(sourceCet.getCode()))
                    .forEach(source -> {

                        // Scan for entity references
                        final Map<String, CustomFieldTemplate> cftsSource = customFieldTemplateService.findByAppliesTo(startNode.getAppliesTo());
                        cftsSource.values()
                                .stream()
                                .filter(customFieldTemplate -> customFieldTemplate.getFieldType() == CustomFieldTypeEnum.ENTITY)
                                .filter(customFieldTemplate -> customFieldTemplate.getEntityClazzCetCode().equals(endNode.getCode()))
                                .filter(customFieldTemplate -> customFieldTemplate.getRelationshipName() != null && customFieldTemplate.getRelationshipName().equals(relationshipTemplate.getName()))
                                .forEach(customFieldTemplate -> {
                                    GraphQLField entityRefField = new GraphQLField();
                                    entityRefField.setFieldName(customFieldTemplate.getCode());
                                    entityRefField.setMultivalued(customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.LIST);
                                    entityRefField.setFieldType(endNode.getCode());
                                    entityRefField.setQuery(getRelationshipDirective(neo4jConfiguration) + relationshipTemplate.getName() + "\", direction: OUT" + propertiesInterface);
                                    
                                    source.getGraphQLFields().add(entityRefField);
                                });

                        // Source singular field
                        if (relationshipTemplate.getSourceNameSingular() != null) {
                            GraphQLField sourceNameSingular = new GraphQLField();
                            sourceNameSingular.setFieldName(relationshipTemplate.getSourceNameSingular());
                            sourceNameSingular.setMultivalued(false);
                            sourceNameSingular.setFieldType(endNode.getCode());
                            sourceNameSingular.setQuery(getRelationshipDirective(neo4jConfiguration) + relationshipTemplate.getName() + "\", direction: OUT" + propertiesInterface);
                            source.getGraphQLFields().add(sourceNameSingular);
                        }

                        // Source plural field
                        if (relationshipTemplate.getSourceNamePlural() != null) {
                            GraphQLField sourceNamePlural = new GraphQLField();
                            sourceNamePlural.setFieldName(relationshipTemplate.getSourceNamePlural());
                            sourceNamePlural.setMultivalued(true);
                            sourceNamePlural.setFieldType(endNode.getCode());
                            sourceNamePlural.setQuery(getRelationshipDirective(neo4jConfiguration) + relationshipTemplate.getName() + "\", direction: OUT" + propertiesInterface);
                            
                            source.getGraphQLFields().add(sourceNamePlural);
                        }

                    });

            // Add fields to target (and sub-targets)
            cetsByName.get(endNode.getCode())
                    .descendance()
                    .stream()
                    .map(targetCet -> graphQLEntities.get(targetCet.getCode()))
                    .forEach(target -> {
                        // Target singular field
                        if (relationshipTemplate.getTargetNameSingular() != null) {
                            GraphQLField targetNameSingular = new GraphQLField();
                            targetNameSingular.setFieldName(relationshipTemplate.getTargetNameSingular());
                            targetNameSingular.setMultivalued(false);
                            targetNameSingular.setFieldType(startNode.getCode());
                            targetNameSingular.setQuery(getRelationshipDirective(neo4jConfiguration) + relationshipTemplate.getName() + "\", direction: IN" + propertiesInterface);
                            target.getGraphQLFields().add(targetNameSingular);
                        }

                        // Target plural field
                        if (relationshipTemplate.getTargetNamePlural() != null) {
                            GraphQLField targetNamePlural = new GraphQLField();
                            targetNamePlural.setFieldName(relationshipTemplate.getTargetNamePlural());
                            targetNamePlural.setMultivalued(true);
                            targetNamePlural.setFieldType(startNode.getCode());
                            targetNamePlural.setQuery(getRelationshipDirective(neo4jConfiguration) + relationshipTemplate.getName() + "\", direction: IN" + propertiesInterface);
                            target.getGraphQLFields().add(targetNamePlural);
                        }

                    });

            add(graphQLEntities, graphQLEntity);

        }

        return graphQLEntities.values();
    }

    private SortedSet<GraphQLField> getGraphQLFields(Map<String, CustomFieldTemplate> cfts, Neo4JConfiguration neo4jConfiguration) {
        SortedSet<GraphQLField> graphQLFields = new TreeSet<>();
        for (CustomFieldTemplate customFieldTemplate : cfts.values()) {

            // Skip the field if it is not configured to be stored in neo4j
            if (customFieldTemplate.getStoragesNullSafe() == null || !customFieldTemplate.getStoragesNullSafe().contains(DBStorageType.NEO4J)) {
                continue;
            }

            GraphQLField graphQLField = new GraphQLField();
            graphQLField.setFieldName(customFieldTemplate.getCode());
            graphQLField.setDefaultValue(customFieldTemplate.getDefaultValue());
            graphQLField.setMultivalued(customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.LIST);
            graphQLField.setRequired(customFieldTemplate.isValueRequired());

            switch (customFieldTemplate.getFieldType()) {
                case LONG:
                case DATE:
            		graphQLField.setFieldType("BigInt");
                    break;
                case DOUBLE:
                    graphQLField.setFieldType("Float");
                    break;
                case BOOLEAN:
                    graphQLField.setFieldType("Boolean");
                    break;
                case ENTITY:
                	continue;
                case BINARY:
                    if(StringUtils.isBlank(customFieldTemplate.getRelationshipName())) {
                        log.warn("CFT " + customFieldTemplate.getAppliesTo() + "#" + customFieldTemplate.getCode() + " has no relationship name defined");
                        continue;
                    }

                    graphQLField.setFieldType(Neo4JConstants.FILE_LABEL);
                    graphQLField.setQuery(getRelationshipDirective(neo4jConfiguration) + customFieldTemplate.getRelationshipName() + "\", direction: OUT)");
                    break;
                case CHILD_ENTITY:
                case EMBEDDED_ENTITY:
                case LIST:
                case SECRET:
                case STRING:
                case TEXT_AREA:
                case LONG_TEXT:
                default:
                    graphQLField.setFieldType("String");
                    break;
            }

            graphQLFields.add(graphQLField);
        }
        return graphQLFields;
    }
    
    @SuppressWarnings("unchecked")
	private String getMutations(Neo4JConfiguration configuration) {
    	// Retrieve all existing mutations
    	List<List<Mutation>> mutationsLists = customEntityTemplateService.getEntityManager()
    			.createQuery("SELECT DISTINCT cet.neo4JStorageConfiguration.mutations "
    					+ "FROM CustomEntityTemplate cet "
    					+ "WHERE cet.neo4JStorageConfiguration.mutations IS NOT NULL")
    			.getResultList();

    	if(!mutationsLists.isEmpty()) {
    		// Flatten the lists
            Set<Mutation> mutations = (Set<Mutation>) mutationsLists.stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());

            StringBuilder mutationsStr = new StringBuilder("schema {\n\tmutation: Mutations\n} \n");

            StringJoiner mutationsJoiner = new StringJoiner("\n\t", "\ntype Mutations {\n\t", "\n}");

            for (Mutation mutation : mutations) {
            	if (configuration.getDbVersion().startsWith("3")) {
            		mutationsJoiner.add(mutation.toString3());
            	} else {
            		mutationsJoiner.add(mutation.toString4());
            	}
            }

            mutationsStr.append(mutationsJoiner.toString());

            return mutationsStr.toString();
        } else {
    	    return "";
        }
    	
    }

    private static void add(Map<String, GraphQLEntity> graphQLEntityMap, GraphQLEntity graphQLEntity) {
        graphQLEntityMap.merge(graphQLEntity.getName(), graphQLEntity, (oldEntity, newEntity) -> {
            oldEntity.getGraphQLFields().addAll(newEntity.getGraphQLFields());
            return oldEntity;
        });
    }

    public List<String> validateIdl(String idl, Neo4JConfiguration neo4jRepo) {
        List<String> result = new ArrayList<>();
        String pattern = neo4jRepo.getDbVersion().startsWith("3") ? 
        		"\\t\\w+: \\[?(?!(?:String|Boolean|BigInt|ID|Float)!?)(\\w*)\\]?!?\\s" :
    			"\\t\\w+: \\[?(?!(?:String|Boolean|Int|BigInt|ID|Float)!?)(\\w*)\\]?!?\\s";
        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);
        // Now create matcher object.
        Matcher m = r.matcher(idl);
        while(m.find()) {
            if (m.start() == m.end()) {
                continue;
            }
            boolean typeExists = idl.contains("type " + m.group(1));
            if (!typeExists) {
                result.add(m.group(1));
            }
        }
        return result;
    }
}




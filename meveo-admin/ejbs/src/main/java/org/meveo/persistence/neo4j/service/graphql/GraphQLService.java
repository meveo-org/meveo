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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.commons.utils.StringUtils;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.customEntities.GraphQLQueryField;
import org.meveo.model.neo4j.GraphQLRequest;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.persistence.neo4j.base.Neo4jDao;
import org.slf4j.Logger;

@Stateless
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class GraphQLService {

    @Inject
    private Neo4jDao neo4jDao;

    @Inject
    private Logger log;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    @MeveoJpa
    private EntityManagerWrapper entityManagerWrapper;

    public Map<String, Object> executeGraphQLRequest(GraphQLRequest graphQLRequest, String neo4jConfiguration) {

        return neo4jDao.executeGraphQLQuery(
                neo4jConfiguration,
                graphQLRequest.getQuery(),
                graphQLRequest.getVariables(),
                graphQLRequest.getOperationName()
        );
    }

    public Map<String, Object> executeGraphQLRequest(String query, String neo4jConfiguration) {
        return neo4jDao.executeGraphQLQuery(neo4jConfiguration, query, null, null);
    }

    /**
     * Update the IDL for every neo4j repositories
     */
    public void updateIDL() {
        final Collection<GraphQLEntity> entities = getEntities();
        String idl = getIDL(entities);

        final List<String> neo4jConfigurations = entityManagerWrapper.getEntityManager()
                .createQuery("SELECT c.code from Neo4JConfiguration c", String.class)
                .getResultList();

        for (String neo4jConfiguration : neo4jConfigurations) {
            neo4jDao.updateIDL(neo4jConfiguration, idl);
        }

    }

    /**
     * Update the IDL for the specified neo4j repository
     * @param neo4jConfiguration Repository to update
     */
    public void updateIDL(String neo4jConfiguration) {
        final Collection<GraphQLEntity> entities = getEntities();
        String idl = getIDL(entities);
        neo4jDao.updateIDL(neo4jConfiguration, idl);
    }

    public String getIDL() {
        final Collection<GraphQLEntity> entities = getEntities();
        return getIDL(entities);
    }

    private String getIDL(Collection<GraphQLEntity> graphQLEntities) {
        StringBuilder idl = new StringBuilder();

        idl.append("scalar GraphQLLong\n");
        idl.append("scalar GraphQLBigDecimal\n\n");

        for (GraphQLEntity graphQLEntity : graphQLEntities) {
            // Skip if entity ended not having fields
            if(graphQLEntity.getGraphQLFields().isEmpty()){
                continue;
            }

            idl.append("type ").append(graphQLEntity.getName()).append(" {\n");
            for (GraphQLField graphQLField : graphQLEntity.getGraphQLFields()) {
                idl.append("\t").append(graphQLField.getFieldName()).append(": ");

                if (graphQLField.isMultivialued()) {
                    idl.append("[");
                }
                idl.append(graphQLField.getFieldType());

                if (graphQLField.isMultivialued()) {
                    idl.append("]");
                }

                if (graphQLField.isRequired()) {
                    idl.append("!");
                }

                if (graphQLField.getQuery() != null) {
                    idl.append(" ").append(graphQLField.getQuery());
                }

                idl.append("\n");

            }

            idl.append("}\n\n");
        }

        return idl.toString();
    }

    private Collection<GraphQLEntity> getEntities() {

        Map<String, GraphQLEntity> graphQLEntities = new TreeMap<>();

        // Entities
        final List<CustomEntityTemplate> ceTsWithSubTemplates = customEntityTemplateService.getCETsWithSubTemplates();
        final Map<String, CustomEntityTemplate> cetsByName = ceTsWithSubTemplates
                .stream()
                .collect(Collectors.toMap(CustomEntityTemplate::getCode, Function.identity()));

        for (CustomEntityTemplate cet : cetsByName.values()) {

            final Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());
            GraphQLEntity graphQLEntity = new GraphQLEntity();
            graphQLEntity.setName(cet.getCode());

            SortedSet<GraphQLField> graphQLFields = getGraphQLFields(cfts);

            // Always add "meveo_uuid" field
            graphQLFields.add(new GraphQLField("meveo_uuid", "String", true));

            if(cet.getAvailableStorages() != null && cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
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
            final List<DBStorageType> relationStorages = relationshipTemplate.getAvailableStorages() != null ? relationshipTemplate.getAvailableStorages() : new ArrayList<>();
            final List<DBStorageType> endNodeStorages = endNode.getAvailableStorages() != null ? endNode.getAvailableStorages() : new ArrayList<>();
            final List<DBStorageType> startNodeStorages = startNode.getAvailableStorages() != null ? startNode.getAvailableStorages() : new ArrayList<>();

            if(!relationStorages.contains(DBStorageType.NEO4J) || !endNodeStorages.contains(DBStorageType.NEO4J) || !startNodeStorages.contains(DBStorageType.NEO4J)){
                continue;
            }

            // Create Graphql relationship type
            final Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(relationshipTemplate.getAppliesTo());
            GraphQLEntity graphQLEntity = new GraphQLEntity();

            String typeName = relationshipTemplate.getGraphQlTypeName() == null ? endNode.getCode() + "Relation" : relationshipTemplate.getGraphQlTypeName();
            graphQLEntity.setName(typeName);

            SortedSet<GraphQLField> graphQLFields = getGraphQLFields(cfts);

            GraphQLField to = new GraphQLField();
            to.setFieldName("to");
            to.setFieldType(endNode.getCode());
            to.setQuery("@cypher(statement: \"MATCH ()-[this]->(to) RETURN to\")");
            graphQLFields.add(to);

            GraphQLField from = new GraphQLField();
            from.setFieldName("from");
            from.setFieldType(startNode.getCode());
            from.setQuery("@cypher(statement: \"MATCH (from)-[this]->() RETURN from\")");
            graphQLFields.add(from);

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
                                .forEach(customFieldTemplate -> {
                                    GraphQLField entityRefField = new GraphQLField();
                                    entityRefField.setFieldName(customFieldTemplate.getCode());
                                    entityRefField.setMultivalued(customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.LIST);
                                    entityRefField.setFieldType(endNode.getCode());
                                    entityRefField.setQuery("@relation(name: \"" + relationshipTemplate.getName() + "\", direction: OUT)");
                                    source.getGraphQLFields().add(entityRefField);
                                });

                        // Source singular field
                        if (relationshipTemplate.getSourceNameSingular() != null) {
                            GraphQLField sourceNameSingular = new GraphQLField();
                            sourceNameSingular.setFieldName(relationshipTemplate.getSourceNameSingular());
                            sourceNameSingular.setMultivalued(false);
                            sourceNameSingular.setFieldType(endNode.getCode());
                            sourceNameSingular.setQuery("@relation(name: \"" + relationshipTemplate.getName() + "\", direction: OUT)");
                            source.getGraphQLFields().add(sourceNameSingular);
                        }

                        // Source plural field
                        if (relationshipTemplate.getSourceNamePlural() != null) {
                            GraphQLField sourceNamePlural = new GraphQLField();
                            sourceNamePlural.setFieldName(relationshipTemplate.getSourceNamePlural());
                            sourceNamePlural.setMultivalued(true);
                            sourceNamePlural.setFieldType(endNode.getCode());
                            sourceNamePlural.setQuery("@relation(name: \"" + relationshipTemplate.getName() + "\", direction: OUT)");
                            source.getGraphQLFields().add(sourceNamePlural);
                        }

                        // Relationships field
                        if (relationshipTemplate.getRelationshipsFieldSource() != null) {
                            GraphQLField outgoingRelationship = new GraphQLField();
                            outgoingRelationship.setFieldName(relationshipTemplate.getRelationshipsFieldSource());
                            outgoingRelationship.setMultivalued(true);
                            outgoingRelationship.setFieldType(typeName);

                            final String query = String.format(
                                    "@cypher(statement: \"MATCH (this)-[rel:%s]->(n:%s) RETURN rel\")",
                                    relationshipTemplate.getName(),
                                    endNode.getCode()
                            );

                            outgoingRelationship.setQuery(query);

                            source.getGraphQLFields().add(outgoingRelationship);
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
                            targetNameSingular.setQuery("@relation(name: \"" + relationshipTemplate.getName() + "\", direction: IN)");
                            target.getGraphQLFields().add(targetNameSingular);
                        }

                        // Target plural field
                        if (relationshipTemplate.getTargetNamePlural() != null) {
                            GraphQLField targetNamePlural = new GraphQLField();
                            targetNamePlural.setFieldName(relationshipTemplate.getTargetNamePlural());
                            targetNamePlural.setMultivalued(true);
                            targetNamePlural.setFieldType(startNode.getCode());
                            targetNamePlural.setQuery("@relation(name: \"" + relationshipTemplate.getName() + "\", direction: IN)");
                            target.getGraphQLFields().add(targetNamePlural);
                        }

                        // Relationships field
                        if (relationshipTemplate.getRelationshipsFieldTarget() != null) {
                            GraphQLField relationship = new GraphQLField();
                            relationship.setFieldName(relationshipTemplate.getRelationshipsFieldTarget());
                            relationship.setMultivalued(true);
                            relationship.setFieldType(typeName);

                            final String query = String.format(
                                    "@cypher(statement: \"MATCH (n:%s)-[rel:%s]->(this) RETURN rel\")",
                                    endNode.getCode(),
                                    relationshipTemplate.getName()
                            );

                            relationship.setQuery(query);

                            target.getGraphQLFields().add(relationship);
                        }
                    });

            add(graphQLEntities, graphQLEntity);

        }

        return graphQLEntities.values();
    }

    private SortedSet<GraphQLField> getGraphQLFields(Map<String, CustomFieldTemplate> cfts) {
        SortedSet<GraphQLField> graphQLFields = new TreeSet<>();
        for (CustomFieldTemplate customFieldTemplate : cfts.values()) {

            // Skip the field if it is not configured to be stored in neo4j
            if(customFieldTemplate.getStorages() == null || !customFieldTemplate.getStorages().contains(DBStorageType.NEO4J)) {
            	continue;
            }

            GraphQLField graphQLField = new GraphQLField();
            graphQLField.setFieldName(customFieldTemplate.getCode());

            if (customFieldTemplate.isIdentifier()) {
                graphQLField.setRequired(true);
                graphQLField.setFieldType("ID");
                graphQLField.setMultivalued(false);
            } else {
                graphQLField.setMultivalued(customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.LIST);
                graphQLField.setRequired(customFieldTemplate.isValueRequired());

                switch (customFieldTemplate.getFieldType()) {
                    case LONG:
                    case DATE:
                        graphQLField.setFieldType("GraphQLLong");
                        break;
                    case DOUBLE:
                        graphQLField.setFieldType("GraphQLBigDecimal");
                        break;
                    case BOOLEAN:
                        graphQLField.setFieldType("Boolean");
                        break;
                    case ENTITY:
                        if(StringUtils.isBlank(customFieldTemplate.getRelationshipName())) {
                            throw new NullPointerException("CFT " + customFieldTemplate.getAppliesTo() + "#" + customFieldTemplate.getCode() + " has no relationship name defined");
                        }

                        graphQLField.setFieldType(customFieldTemplate.getEntityClazzCetCode());
                        graphQLField.setQuery("@relation(name: \"" + customFieldTemplate.getRelationshipName() + "\", direction: OUT)");
                        break;
    				case CHILD_ENTITY:
					case EMBEDDED_ENTITY:
                    case LIST:
                    case STRING:
                    case TEXT_AREA:
                    default:
                        graphQLField.setFieldType("String");
                        break;
                }
            }

            graphQLFields.add(graphQLField);
        }
        return graphQLFields;
    }

    private static void add(Map<String, GraphQLEntity> graphQLEntityMap, GraphQLEntity graphQLEntity){
        graphQLEntityMap.merge(graphQLEntity.getName(), graphQLEntity, (oldEntity, newEntity) -> {
            oldEntity.getGraphQLFields().addAll(newEntity.getGraphQLFields());
            return oldEntity;
        });
    }

}

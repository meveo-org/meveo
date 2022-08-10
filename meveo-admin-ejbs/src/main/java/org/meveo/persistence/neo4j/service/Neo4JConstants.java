package org.meveo.persistence.neo4j.service;

import java.util.Collections;
import java.util.TreeSet;

import org.meveo.persistence.neo4j.service.graphql.GraphQLEntity;
import org.meveo.persistence.neo4j.service.graphql.GraphQLField;

public class Neo4JConstants {

	public static final String FILE_LABEL = "Binary";
	
	/**
	 * {@link GraphQLEntity} representing the Binary type
	 */
	public static final GraphQLEntity BINARY_ENTITY = getBinaryEntity();
	
	private static GraphQLEntity getBinaryEntity() {
        GraphQLEntity binaryEntity = new GraphQLEntity();
        binaryEntity.setName(Neo4JConstants.FILE_LABEL);
        
        GraphQLField binaryValueField = new GraphQLField();
        binaryValueField.setFieldName("value");
        binaryValueField.setFieldType("String");
        
        GraphQLField meveoUuid = new GraphQLField();
        meveoUuid.setFieldName("meveo_uuid");
        meveoUuid.setFieldType("String");
        
        TreeSet<GraphQLField> fields = new TreeSet<>();
        fields.add(binaryValueField);
        fields.add(meveoUuid);
        
        binaryEntity.setGraphQLFields(fields);
        
        return binaryEntity;
	}

}

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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;

import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.model.BaseEntity;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.service.base.NativePersistenceService;

@Stateless
public class CustomTableRelationService extends NativePersistenceService {

    @EJB
    private CustomTableRelationService customTableService;

    @Inject
    protected ParamBeanFactory paramBeanFactory;
    
	/**
	 * Create a single row in the table associated to the {@link CustomRelationshipTemplate}
	 * 
	 * @param crt         CustomRelationshipTemplate associated to the table to insert values
	 * @param startUuid   First part of the table's primary key
	 * @param endUuid     Second part of the table's primary key
	 * @param fieldValues Row values to insert
	 */
    public String createRelation(CustomRelationshipTemplate crt, String startUuid, String endUuid, Map<String, Object> fieldValues) throws BusinessException {
    	checkParameters(crt, startUuid, endUuid);
    	
    	Map<String, Object> values = new HashMap<>();
    	values.put(SQLStorageConfiguration.getDbTablename(crt.getStartNode()), startUuid);
    	values.put(SQLStorageConfiguration.getDbTablename(crt.getEndNode()), endUuid);
    	if(fieldValues != null) {
    		values.putAll(fieldValues);
    	}
    	
    	return super.create(SQLStorageConfiguration.getDbTablename(crt), values, false);
    }

	/**
	 * Update a single row of the table associated to the {@link CustomRelationshipTemplate}
	 * 
	 * @param crt         CustomRelationshipTemplate associated to the table to update
	 * @param startUuid   First part of the table's primary key
	 * @param endUuid     Second part of the table's primary key
	 * @param fieldValues Field values to update on the row. Non-provided fields will not be updated.
	 */
	public void updateRelation(CustomRelationshipTemplate crt, String startUuid, String endUuid, Map<String, Object> fieldValues) {
    	checkParameters(crt, startUuid, endUuid);

    	// If no fields values are supplied, there is nothing to update
		if (fieldValues != null && !fieldValues.keySet().isEmpty()) {
			String dbTablename = SQLStorageConfiguration.getDbTablename(crt);
			String startColumn = SQLStorageConfiguration.getDbTablename(crt.getStartNode());
			String endColumn = SQLStorageConfiguration.getDbTablename(crt.getEndNode());
			
			// TODO: Current implementation states that all the CRTs are unique. Handle cases where they are not.
			// TODO: We currently don't take into account the uniqueness of custom fields and we use the start and end uuids to target row to update
			StringBuilder queryBuilder = new StringBuilder("UPDATE ").append(dbTablename).append("\n");

			queryBuilder.append("SET ");
			for (String key : fieldValues.keySet()) {
				String dbKey = BaseEntity.cleanUpAndLowercaseCodeOrId(key);
				queryBuilder.append(dbKey).append(" = :").append(key).append(", ");
			}
			queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length());	// Delete the last ', ' part of the SET statement
			queryBuilder.append("\n");
			
			queryBuilder.append("WHERE ").append(startColumn).append(" = :startColumn \n")
				.append("AND ").append(endColumn).append(" = :endColumn ;");
			
			// Set source and target uuids
			Query updateQuery = getEntityManager().createNativeQuery(queryBuilder.toString())
					.setParameter("startColumn", startUuid)
					.setParameter("endColumn", endUuid);
			
			setQueryParameterFields(fieldValues, updateQuery);
			
			updateQuery.executeUpdate();
		}

	}
	
	/**
	 * Remove a relation instance using its source uuid, target uuid and field values.
	 * <br>TODO: Current implementation states that all the CRTs are unique. Handle cases where they are not.
	 * <br>TODO: We currently don't take into account the uniqueness of custom fields and we use the start and end uuids to check row
	 * 
	 * @param crt         CustomRelationshipTemplate associated to the table where to search
	 * @param startUuid   First part of the table's primary key
	 * @param endUuid     Second part of the table's primary key
	 * @param fieldValues Field values to be taken into account during the search
	 */
	public void removeRelation(CustomRelationshipTemplate crt, String startUuid, String endUuid, Map<String, Object> fieldValues) {
		checkParameters(crt, startUuid, endUuid);
		
		String dbTablename = SQLStorageConfiguration.getDbTablename(crt);
		String startColumn = SQLStorageConfiguration.getDbTablename(crt.getStartNode());
		String endColumn = SQLStorageConfiguration.getDbTablename(crt.getEndNode());
		
		StringBuilder queryBuilder = new StringBuilder("DELETE \n")
				.append("FROM ").append(dbTablename).append("\n")
				.append("WHERE ").append(startColumn).append(" = :startColumn \n")
				.append("AND ").append(endColumn).append(" = :endColumn \n");
		
		Query deleteQuery = getEntityManager().createNativeQuery(queryBuilder.toString())
			.setParameter("startColumn", startUuid)
			.setParameter("endColumn", endUuid);
		
		deleteQuery.executeUpdate();
	}

	/**
	 * Check if a relation exists using its source uuid, target uuid and field values.
	 * <br>TODO: Current implementation states that all the CRTs are unique. Handle cases where they are not.
	 * <br>TODO: We currently don't take into account the uniqueness of custom fields and we use the start and end uuids to check row
	 * 
	 * @param crt         CustomRelationshipTemplate associated to the table where to search
	 * @param startUuid   First part of the table's primary key
	 * @param endUuid     Second part of the table's primary key
	 * @param fieldValues Field values to be taken into account during the search
	 * @return {@code true} if the record exists in the table
	 */
	public boolean exists(CustomRelationshipTemplate crt, String startUuid, String endUuid, Map<String, Object> fieldValues) {
		checkParameters(crt, startUuid, endUuid);
		
		String dbTablename = SQLStorageConfiguration.getDbTablename(crt);
		String startColumn = SQLStorageConfiguration.getDbTablename(crt.getStartNode());
		String endColumn = SQLStorageConfiguration.getDbTablename(crt.getEndNode());
		
		StringBuilder queryBuilder = new StringBuilder("SELECT EXISTS (\n")
				.append("SELECT 1 \n")
				.append("FROM ").append(dbTablename).append("\n")
				.append("WHERE ").append(startColumn).append(" = :startColumn \n")
				.append("AND ").append(endColumn).append(" = :endColumn \n");
		
		queryBuilder.append(");");
		
		Query existQuery = getEntityManager().createNativeQuery(queryBuilder.toString())
			.setParameter("startColumn", startUuid)
			.setParameter("endColumn", endUuid);
		
		return (boolean) existQuery.getSingleResult();
		
	}
	
	/**
	 * Verifies that the given parameters are not null
	 * 
	 * @throws IllegalArgumentException if one of the argument is null
	 */
	private void checkParameters(CustomRelationshipTemplate crt, String startUuid, String endUuid) throws IllegalArgumentException {
		if(crt == null) {
    		throw new IllegalArgumentException("Custom relationship template must be provided");
    	}
    	
    	if(startUuid == null) {
    		throw new IllegalArgumentException("Start entity uuid must be provided");
    	}
    	
    	if(endUuid == null) {
    		throw new IllegalArgumentException("End entity uuid must be provided");
    	}
	}
	
	/**
	 * Set the values of the parameters corresponding to fields in the given query
	 * 
	 * @param fieldValues Key-Value map of the fields used in the query
	 * @param query       Request whose parameters has to be set
	 */
	private void setQueryParameterFields(Map<String, Object> fieldValues, Query query) {
		for (Entry<String, Object> field : fieldValues.entrySet()) {
			query.setParameter(field.getKey(), field.getValue());
		}
	}

}
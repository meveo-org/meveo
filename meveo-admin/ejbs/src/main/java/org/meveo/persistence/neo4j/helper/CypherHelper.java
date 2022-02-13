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

package org.meveo.persistence.neo4j.helper;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.meveo.model.persistence.DBStorageType;
import org.meveo.persistence.CrossStorageTransaction;
import org.meveo.service.storage.RepositoryService;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.slf4j.Logger;

public class CypherHelper {

    @Inject
    private CrossStorageTransaction crossStorageTransaction;
    
    @Inject
    private RepositoryService repositoryService;
    
    @Inject
    private Logger log;

    @SuppressWarnings("javadoc")
	public <T> T execute(
            String neo4jConfiguration,
            String request,
            Map<String, Object> parameters,
            CypherResultTransformer<T> resultAction,
            CypherExceptionHandler cypherExceptionHandler
    ){
    	
    	Transaction transaction = crossStorageTransaction.beginTransaction(repositoryService.findByCode(neo4jConfiguration), DBStorageType.NEO4J);
    			
        try {

            final StatementResult result = transaction.run(request, parameters);

            if(resultAction != null){
                return resultAction.execute(transaction, result);
                
            } else {
            	result.consume();
                transaction.success();
            }

        } catch (Exception e) {
            
            if(cypherExceptionHandler != null){
                cypherExceptionHandler.handle(e);
            } else {
            	log.error("Error executing query \n{}\nwith parameters {}", request, parameters, e);
            	crossStorageTransaction.rollbackTransaction(e, List.of(DBStorageType.NEO4J));
            }
            
        }

        return null;
    }

    public <T> T execute(String neo4jConfiguration,  String request, Map<String, Object> parameters, CypherResultTransformer<T> resultAction){
        return execute(neo4jConfiguration, request, parameters, resultAction, null);
    }

    public void execute(String neo4jConfiguration,  String request, Map<String, Object> parameters, CypherExceptionHandler cypherExceptionHandler){
        execute(neo4jConfiguration, request, parameters, null, cypherExceptionHandler);
    }

    public void execute(String neo4jConfiguration,  String request, Map<String, Object> parameters){
        execute(neo4jConfiguration, request, parameters, null, null);
    }

    @SuppressWarnings("javadoc")
	public void update(
            String neo4jConfiguration,
            String request,
            Map<String, Object> parameters,
            CypherExceptionHandler cypherExceptionHandler, 
            Transaction transaction
    ){
    	
    	if(transaction == null) {
        	transaction = crossStorageTransaction.beginTransaction(repositoryService.findByCode(neo4jConfiguration), DBStorageType.NEO4J);
    	}

        try {
            StatementResult run = transaction.run(request, parameters);
            run.consume();
            transaction.success();
            
        } catch (Exception e) {
            if(cypherExceptionHandler != null){
                cypherExceptionHandler.handle(e);
            } else {
	            log.error("Can't run update query", e);
            	crossStorageTransaction.rollbackTransaction(e, List.of(DBStorageType.NEO4J));
            }
        }
    }

    public void update(String neo4jConfiguration,  String request){
        update(neo4jConfiguration, request, null, null, null);
    }
    
    public void update(String neo4jConfiguration,  String request, Map<String, Object> parameters){
        update(neo4jConfiguration, request, parameters, null, null);
    }
    
    public void update(String neo4jConfiguration,  String request, CypherExceptionHandler cypherExceptionHandler){
        update(neo4jConfiguration, request, null, cypherExceptionHandler, null);
    }
}

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

import java.util.Map;

import javax.inject.Inject;

import org.meveo.persistence.neo4j.base.Neo4jConnectionProvider;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.slf4j.Logger;

public class CypherHelper {

    @Inject
    private Neo4jConnectionProvider neo4jSessionFactory;
    
    @Inject
    private Logger log;

    public <T> T execute(
            String neo4jConfiguration,
            String request,
            Map<String, Object> parameters,
            CypherResultTransformer<T> resultAction,
            CypherExceptionHandler cypherExceptionHandler
    ){
        Transaction transaction = null;
        try (Session session = neo4jSessionFactory.getSession(neo4jConfiguration)){
            transaction = session.beginTransaction();
            final StatementResult result = transaction.run(request, parameters);

            if(resultAction != null){
                return resultAction.execute(transaction, result);
            } else {
            	result.consume();
                transaction.success();
            }

        } catch (Exception e) {
            if (transaction != null) {
                transaction.failure();
            }
            
            if(cypherExceptionHandler != null){
                cypherExceptionHandler.handle(e);
            }else {
            	log.error("Error executing query \n{}\nwith parameters {}", request, parameters, e);
            }
            
        } finally {
            if (transaction != null) {
                transaction.close();
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

    public void update(
            String neo4jConfiguration,
            String request,
            Map<String, Object> parameters,
            CypherExceptionHandler cypherExceptionHandler
    ){
        Transaction transaction = null;
        try (Session session = neo4jSessionFactory.getSession(neo4jConfiguration)){
            transaction = session.beginTransaction();
            StatementResult run = transaction.run(request, parameters);
            run.consume();
            transaction.success();
        } catch (Exception e) {
            if(cypherExceptionHandler != null){
                cypherExceptionHandler.handle(e);
            }
            if (transaction != null) {
                transaction.failure();
            }
        } finally {
            if (transaction != null) {
                transaction.close();
            }
        }
    }

    public void update(String neo4jConfiguration,  String request){
        update(neo4jConfiguration, request, null, null);
    }
    
    public void update(String neo4jConfiguration,  String request, Map<String, Object> parameters){
        update(neo4jConfiguration, request, parameters, null);
    }
    
    public void update(String neo4jConfiguration,  String request, CypherExceptionHandler cypherExceptionHandler){
        update(neo4jConfiguration, request, null, cypherExceptionHandler);
    }
}

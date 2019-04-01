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

package org.meveo.observers;

import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.service.neo4j.service.graphql.GraphQLService;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Observer that updates IDL definitions when a CET, CRT or CFT changes
 * @author Clément Bareth
 */
@Singleton
@Startup
public class OntologyObserver {

    @Inject
    private GraphQLService graphQLService;

    @Inject
    private Logger log;

    private boolean hasChange = true;

    /**
     * At startup, update the IDL definitions
     */
    @PostConstruct
    public void init(){
        updateIDL();
    }

    /**
     * Every 5 minutes, check if some element of ontology have changed. If it does, update the IDL definitions.
     */
    @Schedule(minute = "*/5", hour = "*", persistent = false)
    @Asynchronous
    public void updateIDL(){
        log.debug("Checking for ontology changes");
        if(hasChange){
            hasChange = false;
            log.info("Ontology has changed, updating IDL definitions");
            graphQLService.updateIDL();
        }
    }

    /**
     * Get notified when a CET changes
     */
    public void cetChange(@Observes CustomEntityTemplate customEntityTemplate){
        hasChange = true;
    }

    /**
     * Get notified when a CRT changes
     */
    public void crtChange(@Observes CustomRelationshipTemplate customEntityTemplate){
        hasChange = true;
    }

    /**
     * Get notified when a CFT changes
     */
    public void cftChange(@Observes CustomFieldTemplate customEntityTemplate){
        hasChange = true;
    }

}

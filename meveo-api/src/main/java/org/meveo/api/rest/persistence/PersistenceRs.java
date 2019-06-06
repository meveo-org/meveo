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

package org.meveo.api.rest.persistence;

import org.jboss.logging.Logger;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.BaseApi;
import org.meveo.api.dto.PersistenceDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.elresolver.ELException;
import org.meveo.interfaces.Entity;
import org.meveo.interfaces.EntityOrRelation;
import org.meveo.interfaces.EntityRelation;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.persistence.CrossStorageService;
import org.meveo.persistence.neo4j.service.Neo4jService;
import org.meveo.persistence.scheduler.AtomicPersistencePlan;
import org.meveo.persistence.scheduler.CyclicDependencyException;
import org.meveo.persistence.scheduler.ScheduledPersistenceService;
import org.meveo.persistence.scheduler.SchedulingService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Path("/{configuration}/persistence")
public class PersistenceRs {

    protected static final Logger LOGGER = Logger.getLogger(PersistenceRs.class);

    @Inject
    private SchedulingService schedulingService;

    @Inject
    private ScheduledPersistenceService<CrossStorageService> scheduledPersistenceService;

    @Inject
    private CrossStorageService crossStorageService;

    @Inject
    private CustomFieldsCacheContainerProvider cache;

    @PathParam("configuration")
    private String configuration;

    @POST
    @Path("/{cetCode}/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> list(@PathParam("cetCode") String cetCode, PaginationConfiguration paginationConfiguration){
        final CustomEntityTemplate customEntityTemplate = cache.getCustomEntityTemplate(cetCode);
        if(customEntityTemplate == null){
            throw new NotFoundException();
        }

        if(paginationConfiguration == null){
            paginationConfiguration = new PaginationConfiguration();
        }

        return crossStorageService.find(configuration, customEntityTemplate, paginationConfiguration);
    }

    @DELETE
    @Path("/{cetCode}/{uuid}")
    public Response delete(@PathParam("cetCode") String cetCode, @PathParam("uuid") String uuid) throws BusinessException {
        final CustomEntityTemplate customEntityTemplate = cache.getCustomEntityTemplate(cetCode);
        if(customEntityTemplate == null){
            throw new NotFoundException();
        }

        crossStorageService.remove(configuration, customEntityTemplate, uuid);

        return Response.noContent().build();
    }

    @POST
    public Response persist(Collection<PersistenceDto> dtos) throws CyclicDependencyException {

        /* Extract the entities */
        final List<Entity> entities = dtos.stream()
                .filter(persistenceDto -> persistenceDto.getDiscriminator().equals(EntityOrRelation.ENTITY))
                .map(persistenceDto -> new Entity.Builder()
                        .type(persistenceDto.getType())
                        .name(persistenceDto.getName())
                        .properties(persistenceDto.getProperties())
                        .build())
                        .collect(Collectors.toList());

        /* Extract the relationships */
        final List<EntityRelation> relations = dtos.stream()
                .filter(persistenceDto -> persistenceDto.getDiscriminator().equals(EntityOrRelation.RELATION))
                .map(persistenceDto -> {
                    final Optional<Entity> source = entities.stream()
                            .filter(entity -> entity.getName().equals(persistenceDto.getSource()))
                            .findAny();
                    final Optional<Entity> target = entities.stream().filter(entity -> entity.getName().equals(persistenceDto.getTarget()))
                            .findAny();
                    if (source.isPresent() && target.isPresent()) {
                        return new EntityRelation.Builder()
                                .type(persistenceDto.getType())
                                .source(source.get())
                                .target(target.get())
                                .properties(persistenceDto.getProperties())
                                .build();
                    }
                    LOGGER.warnv("Relationship of type {} between {} and {} will not be persisted because of missing source or target", persistenceDto.getType(), persistenceDto.getSource(), persistenceDto.getTarget());
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        /* Create the persistence schedule */
        List<EntityOrRelation> entityOrRelations = new ArrayList<>(entities);
        entityOrRelations.addAll(relations);
        AtomicPersistencePlan atomicPersistencePlan = schedulingService.schedule(entityOrRelations);

        try {

            /* Persist the entities and return 201 created response */
            scheduledPersistenceService.persist(configuration, atomicPersistencePlan);
            return Response.status(201).build();

        } catch (BusinessException | ELException e) {

            /* An error happened */
            return Response.serverError().entity(e).build();
        }

    }
}

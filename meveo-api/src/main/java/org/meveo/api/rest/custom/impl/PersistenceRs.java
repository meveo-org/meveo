package org.meveo.api.rest.custom.impl;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.PersistenceDto;
import org.meveo.api.dto.neo4j.Entity;
import org.meveo.api.dto.neo4j.EntityOrRelation;
import org.meveo.api.dto.neo4j.EntityRelation;
import org.meveo.elresolver.ELException;
import org.meveo.model.neo4j.HTTPGraphQLRequest;
import org.meveo.service.neo4j.scheduler.CyclicDependencyException;
import org.meveo.service.neo4j.scheduler.AtomicPersistencePlan;
import org.meveo.service.neo4j.scheduler.ScheduledPersistenceService;
import org.meveo.service.neo4j.scheduler.SchedulingService;
import org.meveo.service.neo4j.service.GraphQLService;
import org.meveo.service.neo4j.service.Neo4jService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Path("/neo4j/persist")
public class PersistenceRs {

    protected static final Logger LOGGER = Logger.getLogger(PersistenceRs.class);

    @Inject
    protected SchedulingService schedulingService;

    @Inject
    protected ScheduledPersistenceService scheduledPersistenceService;

    @Inject
    protected Neo4jService neo4jService;

    @Inject
    protected GraphQLService graphQLService;

    @QueryParam("neo4jConfiguration")
    private String neo4jConfiguration;

    @DELETE
    public Response delete(Collection<PersistenceDto> dtos){

        for (PersistenceDto persistenceDto : dtos) {
            if (persistenceDto.getDiscriminator().equals(EntityOrRelation.ENTITY)) {
                try {
                    neo4jService.deleteEntity(neo4jConfiguration, persistenceDto.getType(), persistenceDto.getProperties());
                } catch (BusinessException e) {
                    Response.serverError();
                }
            }
        }

        return Response.noContent().build();
    }

    @GET
    @Path("/graphql")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response executeGraphQLRequest(@QueryParam("query") String graphQL, @QueryParam("neo4jConfiguration") String neo4jConfiguration){
        List<Map> records = graphQLService.executeGraphQLRequest(graphQL, neo4jConfiguration);
        return Response.ok(records).build();
    }

    @POST
    @Path("/graphql")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response executeGraphQLRequest(@QueryParam("query") String graphQL, HTTPGraphQLRequest httpGraphQLRequest){
        List<Map> records;
        if(StringUtils.isNotEmpty(graphQL)) {
            records = graphQLService.executeGraphQLRequest(graphQL, neo4jConfiguration);
        }else{
            records = graphQLService.executeGraphQLRequest(httpGraphQLRequest);
        }
        return Response.ok(records).build();
    }

    @POST
    @Path("/entities")
    public Response persistEntities(Collection<PersistenceDto> dtos) throws CyclicDependencyException {

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
            scheduledPersistenceService.persist(neo4jConfiguration, atomicPersistencePlan);
            return Response.status(201).build();

        } catch (BusinessException | ELException e) {

            /* An error happened */
            return Response.serverError().entity(e).build();
        }

    }
}

package org.meveo.api.rest.custom.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.PersistenceDto;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.elresolver.ELException;
import org.meveo.interfaces.Entity;
import org.meveo.interfaces.EntityOrRelation;
import org.meveo.interfaces.EntityRelation;
import org.meveo.persistence.neo4j.base.Neo4jDao;
import org.meveo.persistence.neo4j.service.Neo4jService;
import org.meveo.persistence.scheduler.AtomicPersistencePlan;
import org.meveo.persistence.scheduler.CyclicDependencyException;
import org.meveo.persistence.scheduler.OrderedPersistenceService;
import org.meveo.persistence.scheduler.SchedulingService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @lastModifiedVersion 6.4.0
 */
@Path("/neo4j/persist")
@Api("Neo4j persistence")
public class Neo4JPersistenceRs {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Neo4JPersistenceRs.class);
    
    @Inject
    protected CustomRelationshipTemplateService crtService;

    @Inject
    protected SchedulingService schedulingService;

    @Inject
    protected OrderedPersistenceService<Neo4jService> scheduledPersistenceService;

    @Inject
    protected Neo4jService neo4jService;
    
    @Inject
    protected Neo4jDao neo4jDao;

    @QueryParam("neo4jConfiguration")
    private String neo4jConfiguration;

    @DELETE
    @ApiOperation(value="Delete data to be persisted")
    public Response delete(@ApiParam("Data to be persisted") Collection<PersistenceDto> dtos) throws BusinessException {

        for (PersistenceDto persistenceDto : dtos) {
            if (persistenceDto.getDiscriminator().equals(EntityOrRelation.ENTITY)) {
                neo4jService.deleteEntity(neo4jConfiguration, persistenceDto.getType(), persistenceDto.getProperties());
            }
        }

        return Response.noContent().build();
    }
    
    @DELETE
    @Path("/relations/{code}/{uuid}")
    public void deleteRelation(@PathParam("code") String code, @PathParam("uuid") String uuid) {
    	//TODO: Make sure the user has the permission to delete the relation
    	var crt = crtService.findByCode(code, List.of("startNode", "endNode"));
    	if(crt == null) {
    		throw new BadRequestException("CRT does not exists");
    	}
    	
    	neo4jDao.removeRelation(neo4jConfiguration, crt.getStartNode().getCode(), crt.getName(), crt.getEndNode().getCode(), uuid);
    }

    @POST
    @Path("/entities")
    @ApiOperation(value="Persist entity data to be persisted")
    public Response persistEntities(@ApiParam("Data to be persisted") Collection<PersistenceDto> dtos) throws CyclicDependencyException, ELException, EntityDoesNotExistsException, IOException, BusinessApiException, BusinessException {

        /* Extract the entities */
        final List<Entity> entities = dtos.stream()
                .filter(persistenceDto -> persistenceDto.getSource() == null && persistenceDto.getTarget() == null)
                .map(persistenceDto -> new Entity.Builder()
                        .type(persistenceDto.getType())
                        .name(persistenceDto.getName())
                        .properties(persistenceDto.getProperties())
                        .build())
                        .collect(Collectors.toList());

        /* Extract the relationships */
        final List<EntityRelation> relations = dtos.stream()
                .filter(persistenceDto -> persistenceDto.getSource() != null || persistenceDto.getTarget() != null)
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
                    LOGGER.warn("Relationship of type {} between {} and {} will not be persisted because of missing source or target", persistenceDto.getType(), persistenceDto.getSource(), persistenceDto.getTarget());
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        /* Create the persistence schedule */
        List<EntityOrRelation> entityOrRelations = new ArrayList<>(entities);
        entityOrRelations.addAll(relations);
        
        LOGGER.info("Will persist {} entities and {} relations", entities.size(), relations.size());
        AtomicPersistencePlan atomicPersistencePlan = schedulingService.schedule(entityOrRelations);

        /* Persist the entities and return 201 created response */
        scheduledPersistenceService.persist(neo4jConfiguration, atomicPersistencePlan);
        return Response.status(201).build();

    }
    
}

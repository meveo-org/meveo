package org.meveo.service.neo4j.scheduler;

import org.meveo.api.dto.neo4j.Entity;
import org.meveo.api.dto.neo4j.EntityOrRelation;
import org.meveo.api.dto.neo4j.EntityRelation;
import org.meveo.service.custom.CustomRelationshipTemplateService;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public class SchedulingService {

    private final CustomRelationshipTemplateService customRelationshipTemplateService;

    @Inject
    public SchedulingService(CustomRelationshipTemplateService customRelationshipTemplateService) {
        this.customRelationshipTemplateService = customRelationshipTemplateService;
    }

    /**
     * @param entityOrRelations Relations and entities to persist. Collection will be modified - must not be final.
     * @return The persistence schedule built
     */
    public AtomicPersistencePlan schedule(Collection<EntityOrRelation> entityOrRelations) throws CyclicDependencyException {

        AtomicPersistencePlan atomicPersistencePlan = new AtomicPersistencePlan();

        /* Extract leafs, add them to the leaf nodes and remove them from the initial list */
        final Set<Entity> leafs = getLeafs(entityOrRelations);
        final Set<EntityToPersist> leafNodes = leafs.stream()
                .map(SchedulingService::entityToNode)
                .collect(Collectors.toSet());
        entityOrRelations.removeAll(leafs);

        /* Extract targets that are not source and add them to the leaf nodes*/
        Set<Entity> targetsNoSources = getTargetsNoSources(entityOrRelations);
        final Set<EntityToPersist> targetsNoSourcesNodes = targetsNoSources.stream()
                .map(SchedulingService::entityToNode)
                .collect(Collectors.toSet());

        /* Initial targets only are considered as leafs */
        leafNodes.addAll(targetsNoSourcesNodes);
        atomicPersistencePlan.addEntities(leafNodes);

        /* Enforce unity on node name name */
        final Comparator<EntityToPersist> codeComparator = Comparator.comparing(EntityToPersist::getName);

        /* Iterate over source nodes until there is no one left */
        while (!targetsNoSources.isEmpty()) {
            entityOrRelations.removeAll(targetsNoSources);
            final List<EntityRelation> relationsWithTargetsOnly = getRelationsWithTargetsOnly(entityOrRelations, targetsNoSources);

            /* Extract source nodes */
            Set<EntityToPersist> sourceNodeSet = relationsWithTargetsOnly
                    .stream()
                    .map(entityRelation -> {
                        Relation r = entityRelationToRelation(entityRelation);
                        Entity source = entityRelation.getSource();
                        return new SourceNode(source.getType(), source.getCompoundName(), source.getProperties(), r);
                    }).collect(Collectors.toCollection(() -> new TreeSet<>(codeComparator)));

            atomicPersistencePlan.addEntities(sourceNodeSet);

            /* Persist unique relationships right after their targets */
            Set<EntityToPersist> relationsWithTargetsOnlyToPersist = relationsWithTargetsOnly.stream()
                    .map(SchedulingService::entityRelationToRelation)
                    .collect(Collectors.toSet());
            atomicPersistencePlan.addEntities(relationsWithTargetsOnlyToPersist);
            entityOrRelations.removeAll(relationsWithTargetsOnly);

            /* Update the targetsNoSources */
            targetsNoSources = relationsWithTargetsOnly.stream()
                    .map(EntityRelation::getSource)
                    .collect(Collectors.toSet());
        }

        /* Persist remaining relationships */
        Set<EntityRelation> remainingRels = entityOrRelations.stream()
                .filter(EntityRelation.class::isInstance)
                .map(EntityRelation.class::cast)
                .collect(Collectors.toSet());

        final Set<EntityToPersist> relations = remainingRels.stream()
                .map(SchedulingService::entityRelationToRelation)
                .collect(Collectors.toSet());

        atomicPersistencePlan.addEntities(relations);
        entityOrRelations.removeAll(remainingRels);

        /* If there is entities left, throw an exception, it probably had a cyclic dependency between some nodes */
        if(!entityOrRelations.isEmpty()){
            throw new CyclicDependencyException(entityOrRelations);
        }

        return atomicPersistencePlan;
    }

    private static Node entityToNode(Entity e) {
        return new Node(e.getType(), e.getCompoundName(), e.getProperties());
    }

    private static Relation entityRelationToRelation(EntityRelation e) {
        return new Relation(e.getType(), e.getCompoundName(), e.getProperties(), entityToNode(e.getSource()), entityToNode(e.getTarget()));
    }

    private List<EntityRelation> getRelationsWithTargetsOnly(Collection<EntityOrRelation> entityOrRelations, Set<Entity> targetsNoSources) {
        return getUniqueRelations(entityOrRelations)
                .stream()
                .filter(relation -> targetsNoSources.contains(relation.getTarget()))
                .collect(Collectors.toList());
    }

    private Set<Entity> getTargetsNoSources(Collection<EntityOrRelation> entityOrRelations) {
        List<Entity> sources = getUniqueRelations(entityOrRelations).stream()
                .map(EntityRelation::getSource)
                .collect(Collectors.toList());
        List<Entity> targets = getUniqueRelations(entityOrRelations).stream()
                .map(EntityRelation::getTarget)
                .collect(Collectors.toList());
        return targets.stream()
                .filter(entity -> !sources.contains(entity))
                .collect(Collectors.toSet());
    }

    private Set<Entity> getLeafs(Collection<EntityOrRelation> entityOrRelations) {
        List<Entity> sources = getUniqueRelations(entityOrRelations).stream()
                .map(EntityRelation::getSource)
                .collect(Collectors.toList());
        List<Entity> targets = getUniqueRelations(entityOrRelations).stream()
                .map(EntityRelation::getTarget)
                .collect(Collectors.toList());
        return entityOrRelations.stream()
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .filter(entity -> !sources.contains(entity) && !targets.contains(entity))
                .collect(Collectors.toSet());
    }

    private List<EntityRelation> getUniqueRelations(Collection<EntityOrRelation> entityOrRelations) {
        return entityOrRelations.stream()
                .filter(EntityRelation.class::isInstance)
                .map(EntityRelation.class::cast)
                .filter(relation -> customRelationshipTemplateService.isUnique(relation.getType()))
                .collect(Collectors.toList());
    }

}

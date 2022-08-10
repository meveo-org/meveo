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

package org.meveo.persistence.scheduler;

import org.meveo.interfaces.Entity;
import org.meveo.interfaces.EntityOrRelation;
import org.meveo.interfaces.EntityRelation;
import org.meveo.service.custom.CustomRelationshipTemplateService;

import com.google.common.collect.Lists;

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
        final Set<ItemToPersist> leafNodes = leafs.stream()
                .map(SchedulingService::entityToNode)
                .collect(Collectors.toSet());
        entityOrRelations.removeAll(leafs);

        /* Extract targets that are not source and add them to the leaf nodes*/
        Set<Entity> targetsNoSources = getTargetsNoSources(entityOrRelations);
        final Set<ItemToPersist> targetsNoSourcesNodes = targetsNoSources.stream()
                .map(SchedulingService::entityToNode)
                .collect(Collectors.toSet());

        /* Initial targets only are considered as leafs */
        leafNodes.addAll(targetsNoSourcesNodes);
        
        /* To improve performance by having smaller transactions, split leaf nodes in chunks */
        List<List<ItemToPersist>> chunks = Lists.partition(List.copyOf(leafNodes), 1);
        chunks.forEach(chunk -> atomicPersistencePlan.addEntities(Set.copyOf(chunk)));

        /* Enforce unity on node name name */
        final Comparator<ItemToPersist> codeComparator = Comparator.comparing(ItemToPersist::getName);

        /* Iterate over source nodes until there is no one left */
        while (!targetsNoSources.isEmpty()) {
            entityOrRelations.removeAll(targetsNoSources);
            final List<EntityRelation> relationsWithTargetsOnly = getRelationsWithTargetsOnly(entityOrRelations, targetsNoSources);

            /* Extract source nodes */
            Set<ItemToPersist> sourceNodeSet = relationsWithTargetsOnly
                    .stream()
                    .map(entityRelation -> {
                        RelationToPersist r = entityRelationToRelation(entityRelation);
                        Entity source = entityRelation.getSource();
                        return new SourceEntityToPersist(source.getType(), source.getCompoundName(), source.getProperties(), r);
                    }).collect(Collectors.toCollection(() -> new TreeSet<>(codeComparator)));

            atomicPersistencePlan.addEntities(sourceNodeSet);

            /* Persist unique relationships right after their targets */
            Set<ItemToPersist> relationsWithTargetsOnlyToPersist = relationsWithTargetsOnly.stream()
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

        final Set<ItemToPersist> relations = remainingRels.stream()
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

    private static EntityToPersist entityToNode(Entity e) {
        return new EntityToPersist(e.getType(), e.getCompoundName(), e.getProperties());
    }

    private static RelationToPersist entityRelationToRelation(EntityRelation e) {
        return new RelationToPersist(e.getType(), e.getCompoundName(), e.getProperties(), entityToNode(e.getSource()), entityToNode(e.getTarget()));
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

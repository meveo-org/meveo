package org.meveo.service.neo4j.scheduler;

import org.meveo.admin.exception.BusinessException;
import org.meveo.elresolver.ELException;
import org.meveo.service.neo4j.service.Neo4jService;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Set;

public class ScheduledPersistenceService {

    @Inject
    private Neo4jService neo4jService;

    /**
     * Iterate over the persistence schedule and persist the provided entities
     *
     * @param atomicPersistencePlan The schedule to follow
     * @throws BusinessException If the relation cannot be persisted
     */
    public void persist(AtomicPersistencePlan atomicPersistencePlan) throws BusinessException, ELException {

        /* Iterate over persistence schedule and persist the node */

        final Iterator<Set<EntityToPersist>> iterator = atomicPersistencePlan.iterator();

        while(iterator.hasNext()){

            for (EntityToPersist entityToPersist : iterator.next()) {
                if (entityToPersist instanceof SourceNode) {

                    /* Node is a source node */
                    final SourceNode sourceNode = (SourceNode) entityToPersist;
                    neo4jService.addSourceNodeUniqueCrt(sourceNode.getRelation().getCode(),
                            sourceNode.getValues(),
                            sourceNode.getRelation().getEndNode().getValues(),
                            sourceNode.getCode(), sourceNode.getRelation().getEndNode().getCode());

                } else if (entityToPersist instanceof Node) {

                    /* Node is target or leaf node */
                    final Node node = (Node) entityToPersist;
                    neo4jService.addCetNode(node.getCode(), node.getValues());

                } else {

                    /* Item is a relation */
                    final Relation relation = (Relation) entityToPersist;
                    neo4jService.addCRT(relation.getCode(),
                            relation.getValues(),
                            relation.getStartNode().getValues(),
                            relation.getEndNode().getValues(),
                            relation.getStartNode().getCode(), relation.getEndNode().getCode());
                }
            }

        }
    }
}

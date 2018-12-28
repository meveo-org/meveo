package org.meveo.service.neo4j.scheduler;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.elresolver.ELException;
import org.meveo.model.neo4j.Neo4JConfiguration;
import org.meveo.service.neo4j.service.Neo4jService;

public class ScheduledPersistenceService {

    @Inject
    private Neo4jService neo4jService;

    /**
     * Iterate over the persistence schedule and persist the provided entities
     *
     * @param neo4JConfiguration Neo4J coordinates
     * @param atomicPersistencePlan The schedule to follow
     * @throws BusinessException If the relation cannot be persisted
     */
    public void persist(String neo4JConfiguration, AtomicPersistencePlan atomicPersistencePlan) throws BusinessException, ELException {

        /* Iterate over persistence schedule and persist the node */

        final Iterator<List<EntityToPersist>> iterator = atomicPersistencePlan.iterator();

        while(iterator.hasNext()){

            for (EntityToPersist entityToPersist : iterator.next()) {
                if (entityToPersist instanceof SourceNode) {

                    /* Node is a source node */
                    final SourceNode sourceNode = (SourceNode) entityToPersist;
                    neo4jService.addSourceNodeUniqueCrt(
                            neo4JConfiguration,
                            sourceNode.getRelation().getCode(),
                            sourceNode.getValues(),
                            sourceNode.getRelation().getEndNode().getValues());

                } else if (entityToPersist instanceof Node) {

                    /* Node is target or leaf node */
                    final Node node = (Node) entityToPersist;
                    neo4jService.addCetNode(neo4JConfiguration, node.getCode(), node.getValues());

                } else {

                    /* Item is a relation */
                    final Relation relation = (Relation) entityToPersist;
                    neo4jService.addCRT(neo4JConfiguration,
                            relation.getCode(),
                            relation.getValues(),
                            relation.getStartNode().getValues(),
                            relation.getEndNode().getValues());
                }
            }

        }
    }
}

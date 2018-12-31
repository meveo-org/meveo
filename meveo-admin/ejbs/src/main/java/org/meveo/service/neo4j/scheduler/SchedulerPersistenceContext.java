package org.meveo.service.neo4j.scheduler;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableSet;

public class SchedulerPersistenceContext {
    private final Map<String, Set<NodeReference>> nodeReferencesByNodeName = new ConcurrentHashMap<>();

    public Set<NodeReference> getNodeReferences(String nodeName) {
        return nodeReferencesByNodeName.computeIfAbsent(nodeName, key -> Collections.emptySet());
    }

    public void putNodeReferences(String nodeName, Set<NodeReference> nodeReference) {
        nodeReferencesByNodeName.put(nodeName, ImmutableSet.copyOf(nodeReference));
    }
}

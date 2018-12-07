package org.meveo.service.neo4j.scheduler;

import java.util.Map;
import java.util.Objects;

public class Relation extends EntityToPersist {

    private Node startNode;
    private Node endNode;

    public Relation(String code, String name, Map<String, Object> values, Node startNode, Node endNode) {
        super(code, name, values);
        this.startNode = startNode;
        this.endNode = endNode;
    }

    public Node getStartNode() {
        return startNode;
    }

    public void setStartNode(Node startNode) {
        this.startNode = startNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    public void setEndNode(Node endNode) {
        this.endNode = endNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Relation relation = (Relation) o;
        return Objects.equals(startNode, relation.startNode) &&
                Objects.equals(endNode, relation.endNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startNode, endNode);
    }

}

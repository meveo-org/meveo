package org.meveo.service.neo4j.scheduler;

import java.util.Map;
import java.util.Objects;

public class SourceNode extends Node {

    private Relation relation;

    public SourceNode(String code, String name, Map<String, Object> values, Relation relation) {
        super(code, name, values);
        this.relation = relation;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relation);
    }
}

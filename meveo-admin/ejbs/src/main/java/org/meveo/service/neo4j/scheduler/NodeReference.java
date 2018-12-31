package org.meveo.service.neo4j.scheduler;

import com.google.common.base.Objects;

public class NodeReference {
    private Long id;
    private Integer trustScore;
    private String constraintCode;

    public NodeReference() {
    }

    public NodeReference(Long id) {
        this.id = id;
    }

    public NodeReference(Long id, Integer trustScore, String constraintCode) {
        this.id = id;
        this.trustScore = trustScore;
        this.constraintCode = constraintCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isTrusted() {
        return trustScore == null || new Integer(100).equals(trustScore);
    }

    public Integer getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(Integer trustScore) {
        this.trustScore = trustScore;
    }

    public String getConstraintCode() {
        return constraintCode;
    }

    public void setConstraintCode(String constraintCode) {
        this.constraintCode = constraintCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NodeReference that = (NodeReference) o;
        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

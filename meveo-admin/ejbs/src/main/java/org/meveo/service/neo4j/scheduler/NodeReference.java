package org.meveo.service.neo4j.scheduler;

import com.google.common.base.Objects;

public class NodeReference {
    private String uuid;
    private Integer trustScore;
    private String constraintCode;

    public NodeReference() {
    }

    public NodeReference(String uuid) {
        this.uuid = uuid;
    }

    public NodeReference(String uuid, Integer trustScore, String constraintCode) {
        this.uuid = uuid;
        this.trustScore = trustScore;
        this.constraintCode = constraintCode;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
        return Objects.equal(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}

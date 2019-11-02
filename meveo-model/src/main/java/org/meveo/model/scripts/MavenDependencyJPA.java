package org.meveo.model.scripts;

import org.meveo.validation.constraint.subtypeof.SubTypeOf;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "maven_dependency_jpa")
@IdClass(MavenDependencyPk.class)
public class MavenDependencyJPA implements Serializable {
    @Id
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Function.class)
    @JoinColumn(name = "script_id")
    @SubTypeOf(ScriptInstance.class)
    @NotNull
    private Function script;

    @Id
    @Column(name = "coordinates")
    private String coordinates;

    @Column(name = "group_id", nullable =false)
    @NotNull
    private String groupId;

    @Column(name = "artifact_id", nullable =false)
    @NotNull
    private String artifactId;

    @Column(name = "version", nullable =false)
    @NotNull
    private String version;

    @Column(name = "classifier")
    private String classifier;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getCoordinates() {
        StringBuilder coordinatesBuilder = new StringBuilder();
        coordinatesBuilder.append(groupId != null ? groupId : "").append(":");
        coordinatesBuilder.append(artifactId != null ? artifactId : "").append(":");
        coordinatesBuilder.append(version != null ? version : "").append(":");
        coordinatesBuilder.append(classifier != null ? classifier : "");
        coordinates = coordinatesBuilder.toString();
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public Function getScript() {
        return script;
    }

    public void setScript(Function script) {
        this.script = script;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MavenDependencyJPA mavenDependencyJPA = (MavenDependencyJPA) o;
        return Objects.equals(getCoordinates(), mavenDependencyJPA.getCoordinates()) &&
                Objects.equals(getScript(), mavenDependencyJPA.getScript());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCoordinates(), getScript());
    }
}

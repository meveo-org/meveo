package org.meveo.model.scripts;

import org.meveo.validation.constraint.subtypeof.SubTypeOf;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

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
        return groupId + ":" + artifactId + ":" + version + ":" + classifier;
    }

    public Function getScript() {
        return script;
    }

    public void setScript(Function script) {
        this.script = script;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = groupId + ":" + artifactId + ":" + version + ":" + classifier;
    }
}

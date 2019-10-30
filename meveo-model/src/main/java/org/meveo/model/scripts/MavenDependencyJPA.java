package org.meveo.model.scripts;

import org.meveo.validation.constraint.subtypeof.SubTypeOf;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "maven_dependency_jpa")
public class MavenDependencyJPA {

    @EmbeddedId
    private MavenDependencyId mavenDependencyId;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Function.class)
    @JoinColumn(name = "script_id")
    @SubTypeOf(ScriptInstance.class)
    @NotNull
    private Function script;

    @JoinColumn(name = "group_id", nullable =false)
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

    @Column(name = "coordinates")
    private String coordinates;

    public MavenDependencyId getMavenDependencyId() {
        return mavenDependencyId;
    }

    public void setMavenDependencyId(MavenDependencyId mavenDependencyId) {
        this.mavenDependencyId = mavenDependencyId;
    }

    public Function getScript() {
        return script;
    }

    public void setScript(Function script) {
        this.script = script;
    }

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
        return coordinates= (groupId+artifactId+version+classifier);
    }
}

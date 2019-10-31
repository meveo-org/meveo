package org.meveo.model.scripts;

import org.meveo.validation.constraint.subtypeof.SubTypeOf;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "maven_dependency_jpa")
public class MavenDependencyJPA {

    @EmbeddedId
    private MavenDependencyId mavenDependencyId;

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

    public MavenDependencyId getMavenDependencyId() {
        return mavenDependencyId;
    }

    public void setMavenDependencyId(MavenDependencyId mavenDependencyId) {
        this.mavenDependencyId = mavenDependencyId;
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
        return (groupId+artifactId+version+classifier);
    }
}

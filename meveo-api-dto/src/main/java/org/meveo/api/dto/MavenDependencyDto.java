package org.meveo.api.dto;

import org.meveo.model.scripts.MavenDependencyJPA;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MavenDependencyJPA")
@XmlAccessorType(XmlAccessType.FIELD)
public class MavenDependencyDto extends BaseEntityDto {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The groupId. */
    @XmlAttribute()
    private String groupId;

    /** The artifactId. */
    @XmlAttribute()
    private String artifactId;

    /** The version. */
    @XmlAttribute()
    private String version;

    /** The classifier. */
    @XmlAttribute()
    private String classifier;

    /** The coordinates. */
    @XmlAttribute()
    private String coordinates;

    public String getGroupId() {
        return groupId;
    }

    /**
     * Instantiates a new maven dependency dto.
     */
    public MavenDependencyDto() {

    }

    public MavenDependencyDto(MavenDependencyJPA mavenDependencyJPA) {
        this.setGroupId(mavenDependencyJPA.getGroupId());
        this.setArtifactId(mavenDependencyJPA.getArtifactId());
        this.setVersion(mavenDependencyJPA.getVersion());
        this.setCoordinates(mavenDependencyJPA.getCoordinates());
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
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }
}

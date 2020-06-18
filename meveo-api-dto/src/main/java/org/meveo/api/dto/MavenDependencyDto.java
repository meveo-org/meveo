package org.meveo.api.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.model.scripts.MavenDependency;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Dto for {@link MavenDependency}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "MavenDependency")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class MavenDependencyDto extends BaseEntityDto {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The groupId. */
	@XmlAttribute()
	@ApiModelProperty("The groupId")
	private String groupId;

	/** The artifactId. */
	@XmlAttribute()
	@ApiModelProperty("The artifactId")
	private String artifactId;

	/** The version. */
	@XmlAttribute()
	@ApiModelProperty("The version")
	private String version;

	/** The classifier. */
	@XmlAttribute()
	@ApiModelProperty("The classifier")
	private String classifier;

	/** The coordinates. */
	@ApiModelProperty("The coordinates")
	private String coordinates;

	public String getGroupId() {
		return groupId;
	}

	/**
	 * Instantiates a new maven dependency dto.
	 */
	public MavenDependencyDto() {

	}

	public MavenDependencyDto(MavenDependency mavenDependency) {
		this.setGroupId(mavenDependency.getGroupId());
		this.setArtifactId(mavenDependency.getArtifactId());
		this.setVersion(mavenDependency.getVersion());
		this.setCoordinates(mavenDependency.getBuiltCoordinates());
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

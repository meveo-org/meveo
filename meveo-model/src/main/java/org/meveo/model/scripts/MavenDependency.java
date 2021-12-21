package org.meveo.model.scripts;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.meveo.commons.utils.StringUtils;

/**
 * @author clement.bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@Entity
@Table(name = "maven_dependency", uniqueConstraints = { @UniqueConstraint(columnNames = { "group_id", "artifact_id" }) })
@EntityListeners(JPAtoCDIListener.class)
public class MavenDependency implements Serializable {

	private static final long serialVersionUID = 4010437441199195133L;

	@Id
	@Column(name = "coordinates")
	private String coordinates;

	@Column(name = "group_id", nullable = false, updatable = false)
	@NotNull
	private String groupId;

	@Column(name = "artifact_id", nullable = false, updatable = false)
	@NotNull
	private String artifactId;

	@Column(name = "version", nullable = false, updatable = false)
	@NotNull
	private String version;

	@Column(name = "classifier", updatable = false)
	private String classifier;
	
	@PrePersist
	public void prePersist() {
		this.coordinates = this.getBuiltCoordinates();
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
		return coordinates;
	}
	
	public String getBuiltCoordinates() {
		StringBuilder coordinatesBuilder = new StringBuilder();
		coordinatesBuilder.append(groupId != null ? groupId : "").append(":");
		coordinatesBuilder.append(artifactId != null ? artifactId : "").append(":");
		coordinatesBuilder.append(version != null ? version : "");
		coordinatesBuilder.append(classifier != null ? ":" + classifier : "");
		coordinates = coordinatesBuilder.toString();
		return coordinates;
	}

	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((coordinates == null) ? 0 : coordinates.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MavenDependency other = (MavenDependency) obj;
		if (coordinates == null) {
			if (other.coordinates != null)
				return false;
		} else if (!coordinates.equals(other.coordinates))
			return false;
		return true;
	}

	public String toLocalM2Path(String m2Path) {

		String convertedGroupId = groupId.replace(".", File.separator);

		String clazzifier = StringUtils.isBlank(classifier) ? "" : "-" + classifier;

		StringBuilder sb = new StringBuilder(m2Path);
		sb = sb.append(File.separator);
		sb = sb.append(convertedGroupId);
		sb = sb.append(File.separator);
		sb = sb.append(artifactId);
		sb = sb.append(File.separator);
		sb = sb.append(version);
		sb = sb.append(File.separator);
		sb = sb.append(artifactId + "-" + version + clazzifier);
		sb = sb.append(".jar");

		return sb.toString();
	}

	@Override
	public String toString() {
		return "MavenDependency [coordinates=" + coordinates + "]";
	}
	
}

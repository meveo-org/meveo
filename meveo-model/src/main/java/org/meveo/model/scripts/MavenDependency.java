package org.meveo.model.scripts;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.meveo.commons.utils.StringUtils;
import org.meveo.validation.constraint.subtypeof.SubTypeOf;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @lastModifiedVersion 6.5.0
 */
@Entity
@Table(name = "maven_dependency")
@IdClass(MavenDependencyPk.class)
public class MavenDependency implements Serializable {

	private static final long serialVersionUID = 4010437441199195133L;

	@Id
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Function.class)
	@JoinColumn(name = "script_id")
	@SubTypeOf(ScriptInstance.class)
	@NotNull
	private Function script;

	@Id
	@Column(name = "coordinates")
	private String coordinates;

	@Column(name = "group_id", nullable = false)
	@NotNull
	private String groupId;

	@Column(name = "artifact_id", nullable = false)
	@NotNull
	private String artifactId;

	@Column(name = "version", nullable = false)
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
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		MavenDependency mavenDependency = (MavenDependency) o;
		return Objects.equals(getCoordinates(), mavenDependency.getCoordinates()) && Objects.equals(getScript(), mavenDependency.getScript());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getCoordinates(), getScript());
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
}

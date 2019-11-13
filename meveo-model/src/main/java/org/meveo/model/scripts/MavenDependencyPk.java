package org.meveo.model.scripts;

import java.io.Serializable;
import java.util.Objects;


/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @lastModifiedVersion 6.5.0
 */
public class MavenDependencyPk implements Serializable {

	private static final long serialVersionUID = -9216857242601339722L;

	private Function script;

	private String coordinates;

	public Function getScript() {
		return script;
	}

	public void setScript(Function script) {
		this.script = script;
	}

	public String getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		MavenDependencyPk mavenDependencyPk = (MavenDependencyPk) o;
		return Objects.equals(getCoordinates(), mavenDependencyPk.getCoordinates()) && Objects.equals(getScript(), mavenDependencyPk.getScript());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getCoordinates(), getScript());
	}
}

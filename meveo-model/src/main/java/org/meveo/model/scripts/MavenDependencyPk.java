package org.meveo.model.scripts;

import java.io.Serializable;
import java.util.Objects;

public class MavenDependencyPk implements Serializable {

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileDependencyPk fileDependencyPk = (FileDependencyPk) o;
        return Objects.equals(getCoordinates(), fileDependencyPk.getPath()) &&
                Objects.equals(getScript(), fileDependencyPk.getScript());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCoordinates(), getScript());
    }
}

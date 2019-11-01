package org.meveo.model.scripts;

import java.io.Serializable;
import java.util.Objects;

public class FileDependencyPk implements Serializable {

    private Function script;

    private String path;

    public Function getScript() {
        return script;
    }

    public void setScript(Function script) {
        this.script = script;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileDependencyPk fileDependencyPk = (FileDependencyPk) o;
        return Objects.equals(getPath(), fileDependencyPk.getPath()) &&
                Objects.equals(getScript(), fileDependencyPk.getScript());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath(), getScript());
    }
}

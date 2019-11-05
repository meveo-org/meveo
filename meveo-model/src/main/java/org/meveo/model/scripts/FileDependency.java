package org.meveo.model.scripts;

import org.meveo.validation.constraint.subtypeof.SubTypeOf;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "file_dependency")
@IdClass(FileDependencyPk.class)
public class FileDependency implements Serializable {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Function.class)
    @JoinColumn(name = "script_id")
    @SubTypeOf(ScriptInstance.class)
    private Function script;

    @Id
    @Column(name = "path")
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
        FileDependency fileDependency = (FileDependency) o;
        return Objects.equals(getPath(), fileDependency.getPath()) &&
                Objects.equals(getScript(), fileDependency.getScript());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath(), getScript());
    }
}

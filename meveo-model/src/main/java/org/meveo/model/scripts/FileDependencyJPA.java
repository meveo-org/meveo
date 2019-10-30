package org.meveo.model.scripts;

import org.meveo.validation.constraint.subtypeof.SubTypeOf;

import javax.persistence.*;

@Entity
@Table(name = "file_dependency_jpa")
public class FileDependencyJPA {

    @EmbeddedId
    private FileDependencyId fileDependencyId;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Function.class)
    @JoinColumn(name = "script_id")
    @SubTypeOf(ScriptInstance.class)
    private Function script;

    @Column(name = "path")
    private String path;

    public FileDependencyId getFileDependencyId() {
        return fileDependencyId;
    }

    public void setFileDependencyId(FileDependencyId fileDependencyId) {
        this.fileDependencyId = fileDependencyId;
    }

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
}

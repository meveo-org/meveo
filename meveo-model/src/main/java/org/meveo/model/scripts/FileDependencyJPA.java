package org.meveo.model.scripts;

import org.meveo.validation.constraint.subtypeof.SubTypeOf;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "file_dependency_jpa")
@IdClass(FileDependencyPk.class)
public class FileDependencyJPA implements Serializable {

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
}

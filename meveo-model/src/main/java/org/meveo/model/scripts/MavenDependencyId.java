package org.meveo.model.scripts;

import org.meveo.validation.constraint.subtypeof.SubTypeOf;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Embeddable
public class MavenDependencyId implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Function.class)
    @JoinColumn(name = "script_id")
    @SubTypeOf(ScriptInstance.class)
    @NotNull
    private Function script;

    @Column(name = "coordinates")
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
}

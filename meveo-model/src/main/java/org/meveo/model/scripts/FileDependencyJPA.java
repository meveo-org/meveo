package org.meveo.model.scripts;

import org.meveo.validation.constraint.subtypeof.SubTypeOf;

import javax.persistence.*;

@Entity
@Table(name = "file_dependency_jpa")
public class FileDependencyJPA {

    @EmbeddedId
    private FileDependencyId fileDependencyId;

    public FileDependencyId getFileDependencyId() {
        return fileDependencyId;
    }

    public void setFileDependencyId(FileDependencyId fileDependencyId) {
        this.fileDependencyId = fileDependencyId;
    }

}

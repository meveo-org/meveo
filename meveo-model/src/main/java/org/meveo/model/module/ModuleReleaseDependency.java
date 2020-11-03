package org.meveo.model.module;

import org.hibernate.annotations.GenericGenerator;
import org.meveo.model.BaseEntity;
import org.meveo.model.BusinessEntity;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name = "module_release_dependency")
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "module_release_dependency_seq")})
public class ModuleReleaseDependency extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_release_id")
    private ModuleRelease moduleRelease;

    @Column(name = "current_version")
    @Size(max = 50)
    private String currentVersion;

    @Column(name = "code")
    @Size(max = 255)
    private String code;

    @Column(name = "description")
    @Size(max = 255)
    private String description;

    @Transient
    private BusinessEntity dependencyEntity;

    public ModuleReleaseDependency(String code,String description,String currentVersion) {
        this.currentVersion = currentVersion;
        this.code = code;
        this.description = description;
    }
    public ModuleReleaseDependency( BusinessEntity dependency) {
        this.dependencyEntity=dependency;
    }
    public ModuleReleaseDependency() {
        super();
    }

    public ModuleRelease getModuleRelease() {
        return moduleRelease;
    }

    public void setModuleRelease(ModuleRelease moduleRelease) {
        this.moduleRelease = moduleRelease;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        ModuleReleaseDependency other = (ModuleReleaseDependency) obj;
        if (currentVersion == null) {
            if (other.currentVersion != null)
                return false;
        } else if (!currentVersion.equals(other.currentVersion))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        return true;
    }
}

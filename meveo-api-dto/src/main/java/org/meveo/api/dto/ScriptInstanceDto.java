package org.meveo.api.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.script.CustomScriptDto;
import org.meveo.model.scripts.FileDependency;
import org.meveo.model.scripts.MavenDependency;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.security.Role;

/**
 * The Class ScriptInstanceDto.
 *
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.5.0
 */
@XmlRootElement(name = "ScriptInstance")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScriptInstanceDto extends CustomScriptDto {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4555037251902559699L;

    /** The execution roles. */
    private List<RoleDto> executionRoles = new ArrayList<RoleDto>();
    
    /** The sourcing roles. */
    private List<RoleDto> sourcingRoles = new ArrayList<RoleDto>();

    /** The file dependencies. */
    private List<FileDependencyDto> fileDependencies = new ArrayList<>();

    /** The maven dependencies. */
    private List<MavenDependencyDto> mavenDependencies = new ArrayList<>();

    /**
     * Instantiates a new script instance dto.
     */
    public ScriptInstanceDto() {
        super();
    }

    /**
     * Instantiates a new script instance dto.
     *
     * @param scriptInstance the ScriptInstance entity
     */
    public ScriptInstanceDto(ScriptInstance scriptInstance, String source) {
        super(scriptInstance, source);

        if (scriptInstance.getExecutionRoles() != null) {
            for (Role role : scriptInstance.getExecutionRoles()) {
                executionRoles.add(new RoleDto(role, true, true));
            }
        }
        if (scriptInstance.getSourcingRoles() != null) {
            for (Role role : scriptInstance.getSourcingRoles()) {
                sourcingRoles.add(new RoleDto(role, true, true));
            }
        }

        if (scriptInstance.getFileDependencies() != null) {
            for (FileDependency file : scriptInstance.getFileDependencies() ) {
                fileDependencies.add(new FileDependencyDto(file));
            }
        }

        if (scriptInstance.getMavenDependencies() != null) {
            for (MavenDependency maven : scriptInstance.getMavenDependencies() ) {
                mavenDependencies.add(new MavenDependencyDto(maven));
            }
        }
    }


    @Override
    public String toString() {
        return "ScriptInstanceDto [code=" + getCode() + ", description=" + getDescription() + ", type=" + getType() + ", script=" + getScript() + ", executionRoles="
                + executionRoles + ", sourcingRoles=" + sourcingRoles + "]";
    }

    /**
     * Gets the execution roles.
     *
     * @return the executionRoles
     */
    public List<RoleDto> getExecutionRoles() {
        return executionRoles;
    }

    /**
     * Sets the execution roles.
     *
     * @param executionRoles the executionRoles to set
     */
    public void setExecutionRoles(List<RoleDto> executionRoles) {
        this.executionRoles = executionRoles;
    }

    /**
     * Gets the sourcing roles.
     *
     * @return the sourcingRoles
     */
    public List<RoleDto> getSourcingRoles() {
        return sourcingRoles;
    }

    /**
     * Sets the sourcing roles.
     *
     * @param sourcingRoles the sourcingRoles to set
     */
    public void setSourcingRoles(List<RoleDto> sourcingRoles) {
        this.sourcingRoles = sourcingRoles;
    }

    public List<FileDependencyDto> getFileDependencies() {
        return fileDependencies;
    }

    public void setFileDependencies(List<FileDependencyDto> fileDependencies) {
        this.fileDependencies = fileDependencies;
    }

    public List<MavenDependencyDto> getMavenDependencies() {
        return mavenDependencies;
    }

    public void setMavenDependencies(List<MavenDependencyDto> mavenDependencies) {
        this.mavenDependencies = mavenDependencies;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        } else if (!(obj instanceof ScriptInstanceDto)) { // Fails with proxed objects: getClass() != obj.getClass()){
            return false;
        }

        ScriptInstanceDto other = (ScriptInstanceDto) obj;

        if (getCode() == null) {
            if (other.getCode() != null) {
                return false;
            }
        } else if (!getCode().equals(other.getCode())) {
            return false;
        }
        return true;
    }
}
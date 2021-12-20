package org.meveo.model.module;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.model.BaseEntity;
import org.meveo.model.BusinessEntity;
import org.meveo.model.scripts.JPAtoCDIListener;

/**
 * @author Mbarek
 * @lastModifiedVersion 6.9.0
 */
@Entity 
@Table(name = "meveo_module_dependency")
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "meveo_module_dependency_seq")})
@NamedQueries({ 
	@NamedQuery(name = "MeveoModuleDependency.findByCodeAndVersion", query = "SELECT m from MeveoModuleDependency m WHERE m.code=:moduleCode AND m.currentVersion=:currentVersion") })
public class MeveoModuleDependency extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id")
    private MeveoModule meveoModule;
    
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
    
	public MeveoModuleDependency(String code,String description,String currentVersion) { 
		this.currentVersion = currentVersion;
		this.code = code;
		this.description = description;
	}
	public MeveoModuleDependency( BusinessEntity dependency) { 
		this.dependencyEntity=dependency;
	}
	public MeveoModuleDependency() {
		super();
	} 
	public MeveoModule getMeveoModule() {
		return meveoModule;
	}

	public void setMeveoModule(MeveoModule meveoModule) {
		this.meveoModule = meveoModule;
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
		MeveoModuleDependency other = (MeveoModuleDependency) obj;
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
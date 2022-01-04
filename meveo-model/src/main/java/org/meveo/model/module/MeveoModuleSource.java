package org.meveo.model.module;

import java.io.Serializable;

import javax.persistence.Table;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;


/**
 * Json of a meveo module storage
 * @author ArthurGrenier
 * @version 7.0.0
 *
 */
@Entity
@Table (name = "meveo_module_source")
public class MeveoModuleSource implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @Id
    private MeveoModule meveoModule;
    
    @Column(name = "module_source", columnDefinition = "TEXT")
    private String moduleSource;
	
	public void setModuleSource(String moduleSource) {
		this.moduleSource = moduleSource;
	}
	
	public String getModuleSource() {
		return this.moduleSource;
	}
	
	public void setMeveoModule (MeveoModule meveoModule) {
		this.meveoModule = meveoModule;
	}
	
	public MeveoModule getMeveoModule() {
		return this.meveoModule;
	}
}

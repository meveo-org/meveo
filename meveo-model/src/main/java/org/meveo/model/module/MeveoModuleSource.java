package org.meveo.model.module;

import java.io.Serializable;

import javax.persistence.Table;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ObservableEntity;

/**
 * Json of a meveo module storage
 * @author ArthurGrenier
 * @version 7.0.0
 *
 */
@Entity
@ObservableEntity
@Table (name = "meveo_module_source")
public class MeveoModuleSource extends BusinessEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @OneToOne(mappedBy = "meveoModuleSource", fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private MeveoModule meveoModule;
    
	@OneToOne(mappedBy = "meveoModuleSource", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(name = "module_source", table = "module_source", columnDefinition = "TEXT")
    private String moduleSource;
	
	public void setModuleSource(String moduleSource) {
		this.moduleSource = moduleSource;
	}
	
	public String getModuleSource() {
		return this.moduleSource;
	}
}

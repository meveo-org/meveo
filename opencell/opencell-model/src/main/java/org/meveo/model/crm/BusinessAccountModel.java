package org.meveo.model.crm;

import org.meveo.model.module.MeveoModule;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Edward P. Legaspi
 **/
@Entity
@Table(name = "crm_business_account_model")
public class BusinessAccountModel extends MeveoModule {

    private static final long serialVersionUID = 8664266331861722097L;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "hierarchy_type", length = 20)
    private AccountHierarchyTypeEnum hierarchyType;

	public AccountHierarchyTypeEnum getHierarchyType() {
		return hierarchyType;
	}

	public void setHierarchyType(AccountHierarchyTypeEnum hierarchyType) {
		this.hierarchyType = hierarchyType;
	}

}
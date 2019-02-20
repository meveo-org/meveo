package org.meveo.model.customEntities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.ModuleItem;

import java.util.List;

@Entity
@ModuleItem
@Cacheable
@ExportIdentifier({ "code" })
@Table(name = "cust_cec", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "increment")
@NamedQueries({@NamedQuery(name = "CustomEntityCategory.getCustomEntityCategories", query = "SELECT DISTINCT cec from CustomEntityCategory cec join fetch cec.customEntityTemplates where cec.disabled=false order by cec.name")})

public class CustomEntityCategory extends BusinessEntity {

	private static final long serialVersionUID = -4264545157890676607L;
	
	@Column(name = "name", length = 100, nullable = false)
	@Size(max = 100)
	@NotNull
	private String name;

    @OneToMany(mappedBy = "customEntityCategory", fetch = FetchType.LAZY)
    private List<CustomEntityTemplate> customEntityTemplates;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public List<CustomEntityTemplate> getCustomEntityTemplates() {
        return customEntityTemplates;
    }

    public void setCustomEntityTemplates(List<CustomEntityTemplate> customEntityTemplates) {
        this.customEntityTemplates = customEntityTemplates;
    }
}

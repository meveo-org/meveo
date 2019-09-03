package org.meveo.model.customEntities;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.*;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.ModuleItem;
import org.meveo.model.annotation.ImportOrder;

import java.util.List;

@Entity
@ModuleItem
@Cacheable
@ImportOrder(1)
@ExportIdentifier({ "code" })
@Table(name = "cust_cec", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @org.hibernate.annotations.Parameter(name = "sequence_name", value = "cust_cec_seq"), })
public class CustomEntityCategory extends BusinessEntity {

	private static final long serialVersionUID = -4264545157890676607L;
	
	@Column(name = "name", length = 100, nullable = false)
	@Size(max = 100)
	@NotNull
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

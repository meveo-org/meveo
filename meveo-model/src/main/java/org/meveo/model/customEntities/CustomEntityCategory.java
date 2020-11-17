/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.model.customEntities;

import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.ModuleItem;
import org.meveo.model.ModuleItemOrder;
import org.meveo.model.annotation.ImportOrder;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @author Clément Bareth
 * @version 6.9.0
 */
@Entity
@ModuleItem(value = "CustomEntityCategory", path = "customEntityCategories")
@ModuleItemOrder(0)
@Cacheable
@ImportOrder(1)
@ExportIdentifier({ "code" })
@Table(name = "cust_cec", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence_name", value = "cust_cec_seq"), })
public class CustomEntityCategory extends BusinessEntity {

	private static final long serialVersionUID = -4264545157890676607L;

	@Column(name = "name", length = 100, nullable = false)
	@Size(max = 100)
	@NotNull
	private String name;

	@OneToMany(mappedBy = "customEntityCategory", cascade = { CascadeType.MERGE })
	private List<CustomEntityTemplate> customEntityTemplates;

//	public void addCustomEntityTemplate(CustomEntityTemplate cet) {
//		addCustomEntityTemplate(cet, true);
//	}
//
//	public void addCustomEntityTemplate(CustomEntityTemplate cet, boolean isSet) {
//
//		if (getCustomEntityTemplates() != null) {
//			if (getCustomEntityTemplates().contains(cet)) {
//				getCustomEntityTemplates().set(getCustomEntityTemplates().indexOf(cet), cet);
//
//			} else {
//				getCustomEntityTemplates().add(cet);
//			}
//
//			if (isSet) {
//				cet.setCustomEntityCategory(this, false);
//			}
//		}
//	}
//
//	public void removeCustomEntityTemplate(CustomEntityTemplate cet) {
//		getCustomEntityTemplates().remove(cet);
//		cet.setCustomEntityCategory(null);
//	}

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

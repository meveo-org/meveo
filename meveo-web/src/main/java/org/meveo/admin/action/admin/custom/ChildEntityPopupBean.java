/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.admin.action.admin.custom;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.jaxb.customer.CustomField;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.typereferences.GenericTypeReferences;
import org.meveo.service.crm.impl.CustomFieldTemplateService;

@Named
@ViewScoped
public class ChildEntityPopupBean implements Serializable {

	private static final long serialVersionUID = 2039863489603451126L;

	@Inject 
    private CustomFieldTemplateService customFieldTemplateService;
    
    private CustomFieldValues values;
    
    private Collection<CustomFieldTemplate> fields;
    
    private String cetCode;
    
    public void initEntity(String cetCode, String serializedValues) {
    	values = new CustomFieldValues();
    	fields = customFieldTemplateService.findByAppliesTo("CE_"+cetCode).values();
    	this.cetCode = cetCode;

    	if(!StringUtils.isBlank(serializedValues)) {
    		Map<String, Object> mapOfValues = JacksonUtil.fromString(serializedValues, GenericTypeReferences.MAP_STRING_OBJECT);
        	mapOfValues.forEach((k,v) -> values.setValue(k, v));
    	}else {
    		fields.forEach(t -> values.setValue(t.getDbFieldname(), t.getDefaultValueConverted()));
    	}
    	
    }
    
	public CustomFieldValues getValues() {
		return values;
	}

	public void setValues(CustomFieldValues values) {
		this.values = values;
	}

	public Collection<CustomFieldTemplate> getFields() {
		return fields;
	}

	public void setFields(Collection<CustomFieldTemplate> fields) {
		this.fields = fields;
	}

	public String getCetCode() {
		return cetCode;
	}
    
}
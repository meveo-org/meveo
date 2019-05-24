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

import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldValue;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.persistence.JacksonUtil;
import org.primefaces.event.SelectEvent;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

@Named
@ViewScoped
public class CustomTableRowDetailBean extends CustomTableBean implements Serializable{

    private static final long serialVersionUID = -2748591950645172132L;
    
    private CustomFieldValues values;
    
    private Collection<CustomFieldTemplate> fields;
    
    private CustomFieldTemplate selectedCft;
    
    private String cetCode;
    
    public void initEntity(String cetCode, Map<String, Object> valuesMap, Collection<CustomFieldTemplate> fields) {
    	values = new CustomFieldValues();
    	this.cetCode = cetCode;
    	this.fields = fields;
    	valuesMap.forEach((k,v) -> values.setValue(k, v));
    }
    
	public CustomFieldValues getValues() {
		return values;
	}
	
	public Map<String, Object> getValuesMap(){
		return values.getValues();
	}

	public void setValues(CustomFieldValues values) {
		this.values = values;
	}

	@Override
    public Collection<CustomFieldTemplate> getFields() {
		return fields;
	}

	public void setFields(Collection<CustomFieldTemplate> fields) {
		this.fields = fields;
	}

	public String getCetCode() {
		return cetCode;
	}
	
	@SuppressWarnings("unchecked")
    @ActionMethod
    @Override
    public void onEntityReferenceSelected(SelectEvent event) {
		Map<String, Object> selectedEntityInPopup = (Map<String,Object>) event.getObject();
		Number newId = (Number) selectedEntityInPopup.get("id");
    	CustomFieldValue cfValue = values.getCfValue(selectedCft.getDbFieldname());
		cfValue.setLongValue(newId.longValue());
    }
	
	@Override
	@ActionMethod
	public void onChildEntityUpdated(CustomFieldValues cfValues) {
		String serializedValues = JacksonUtil.toString(cfValues.getValues());
    	CustomFieldValue cfValue = values.getCfValue(selectedCft.getDbFieldname());
		cfValue.setStringValue(serializedValues);
	}

	public CustomFieldTemplate getSelectedCft() {
		return selectedCft;
	}

	public void setSelectedCft(CustomFieldTemplate selectedCft) {
		this.selectedCft = selectedCft;
	}

	public void setCetCode(String cetCode) {
		this.cetCode = cetCode;
	}
	
	
}
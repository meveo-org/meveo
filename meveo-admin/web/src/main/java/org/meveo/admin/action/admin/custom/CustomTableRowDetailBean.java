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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValue;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.persistence.JacksonUtil;
import org.primefaces.event.SelectEvent;

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
    	
    	valuesMap.forEach((k,v) -> {
    		values.setValue(k, v);
    	});
    	
    	fields.stream().filter(f -> CustomFieldStorageTypeEnum.LIST.equals(f.getStorageType()))
    		.filter(f -> valuesMap.get(f.getDbFieldname()) == null)
			.forEach(f -> values.setValue(f.getDbFieldname(), f.getNewListValue(), f.getFieldType().getDataClass()));
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
		String newId = (String) selectedEntityInPopup.get("uuid");
    	CustomFieldValue cfValue = values.getCfValue(selectedCft.getDbFieldname());
    	if (selectedCft.getStorageType().equals(CustomFieldStorageTypeEnum.LIST)) {
    		List<String> listValue = cfValue.getListValue();
    		if(listValue == null) {
    			listValue = new ArrayList<String>();
        		listValue.add(newId);
    		} else {
        		listValue.add(newId);
        		listValue = listValue.stream().distinct().collect(Collectors.toList());
    		}
    		cfValue.setListValue(listValue);
    	} else {
    		cfValue.setStringValue(newId);
    	}
	}
	
	@Override
	@ActionMethod
	public void onChildEntityUpdated(CustomFieldValues cfValues) {
		String serializedValues = JacksonUtil.toString(cfValues.getValues());
    	CustomFieldValue cfValue = values.getCfValue(selectedCft.getDbFieldname());
		cfValue.setStringValue(serializedValues);
	}
	
	@ActionMethod
	public void onListElementUpdated() {
		
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
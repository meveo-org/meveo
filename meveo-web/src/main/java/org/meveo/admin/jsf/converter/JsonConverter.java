package org.meveo.admin.jsf.converter;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.meveo.model.persistence.JacksonUtil;

@FacesConverter("jsonConverter")
public class JsonConverter implements Converter<Object>{

	@Override
	public String getAsObject(FacesContext context, UIComponent component, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if(!(value instanceof String)) {
			return value.toString();
		}
		
		return JacksonUtil.fromString((String) value, Map.class).toString();
	}
	
	

}

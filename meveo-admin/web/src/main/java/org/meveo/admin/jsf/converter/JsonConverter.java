package org.meveo.admin.jsf.converter;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.meveo.model.persistence.JacksonUtil;

@FacesConverter("jsonConverter")
public class JsonConverter implements Converter<String>{

	@Override
	public String getAsObject(FacesContext context, UIComponent component, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, String value) {
		return JacksonUtil.fromString(value, Map.class).toString();
	}
	
	

}

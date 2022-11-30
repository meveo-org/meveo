package org.meveo.admin.jsf.converter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.meveo.model.IEntity;
import org.primefaces.component.picklist.PickList;
import org.primefaces.model.DualListModel;

@FacesConverter("iEntityConverter")
public class IEntityConverter implements Converter<IEntity<?>>{
	
	@SuppressWarnings("unchecked")
	@Override
	public IEntity<?> getAsObject(FacesContext context, UIComponent component, String value) {
		if (component instanceof PickList) {
	        Object dualList = ((PickList) component).getValue();
	        DualListModel<IEntity<?>> dl = (DualListModel<IEntity<?>>) dualList;
			for(IEntity<?> e : dl.getSource()) {
				if(getAsString(context, component, e).equals(value)) {
					return e;
				}
			}
			
			for(IEntity<?> e : dl.getTarget()) {
				if(getAsString(context, component, e).equals(value)) {
					return e;
				}
			}
	    }
		
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, IEntity<?> value) {
		return "" + value.getId();
	}

}

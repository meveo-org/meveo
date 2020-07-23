package org.meveo.admin.jsf.converter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;

import org.meveo.cache.CacheKeyStr;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.BaseEntity;
import org.meveo.model.BusinessEntity;
import org.meveo.service.crm.impl.CustomFieldInstanceService;

/**
 * @author Phu Bach | pbach1982@gmail.com
 * @author clement.bareth
 * @author Edward Legaspi | edward.legaspi@manaty.net
 * @version 6.10.0
 **/
@FacesConverter("entityReferenceValueConverter")
@ViewScoped
public class EntityReferenceValueConverter implements Converter<Object>, Serializable {

	private static final long serialVersionUID = 2297474050618191644L;

	@Inject
	private CustomFieldInstanceService customFieldInstanceService;

	private Map<CacheKeyStr, Object> entityReferencesCache = new HashMap<>();

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		String classname = (String) component.getAttributes().get("classname");
		if (customFieldInstanceService == null) {
			customFieldInstanceService = CDI.current().select(CustomFieldInstanceService.class).get();
		}

		if (!StringUtils.isBlank(classname)) {
			BusinessEntity convertedEntity = customFieldInstanceService.findBusinessEntityCFVByCode(classname, value);
			if (convertedEntity != null) {
				return convertedEntity;

			} else {
				CacheKeyStr key = new CacheKeyStr(classname, value);
				return entityReferencesCache.get(key);
			}

		} else {
			return null;
		}
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String str = "";
		if (value instanceof BusinessEntity) {
			str = "" + ((BusinessEntity) value).getCode();

		} else if (value instanceof BaseEntity) {
			str = "" + ((BaseEntity) value).getId();
		}

		CacheKeyStr key = new CacheKeyStr(value.getClass().getName(), str);
		entityReferencesCache.put(key, value);

		return str;
	}
}

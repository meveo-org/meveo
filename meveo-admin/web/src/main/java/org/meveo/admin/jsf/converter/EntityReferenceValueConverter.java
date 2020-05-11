package org.meveo.admin.jsf.converter;

import org.meveo.commons.utils.StringUtils;
import org.meveo.model.BusinessEntity;
import org.meveo.service.crm.impl.CustomFieldInstanceService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author Phu Bach | pbach1982@gmail.com
 * @version 6.9.0
 **/
@FacesConverter("entityReferenceValueConverter")
@ApplicationScoped
public class EntityReferenceValueConverter implements Converter<Object>, Serializable {

    private static final long serialVersionUID = 2297474050618191644L;

    @Inject
    private CustomFieldInstanceService customFieldInstanceService;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        String classname = (String) component.getAttributes().get("classname");
        if(customFieldInstanceService == null) {
            customFieldInstanceService = CDI.current().select(CustomFieldInstanceService.class).get();
        }
        if (!StringUtils.isBlank(classname)) {
            BusinessEntity convertedEntity = customFieldInstanceService.findBusinessEntityCFVByCode(classname, value);
            if (convertedEntity != null) {
                return convertedEntity;
            } else {
                return null;
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
        }
        return str;
    }
}

package org.meveo.admin.jsf.converter;

import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomizedEntity;
import org.meveo.util.EntityCustomizationUtils;

import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FacesConverter("customEntityCodeConverter")
@ViewScoped
public class CustomEntityCodeConverter implements Converter, Serializable {

    private static final long serialVersionUID = -7175173363564310863L;

    @Inject
    private CustomEntityInstanceService customEntityInstanceService;

    private Map<String, String> cetCodeMap = new HashMap<String, String>();

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        if (!cetCodeMap.containsValue(value)){
            loadCetCodeDefinitions();
        }

        for (Map.Entry<String, String> entry : cetCodeMap.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }

        return value;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object obj) {

        if (obj == null || obj.toString().length() == 0){
            return "";
        }

        if (cetCodeMap.containsValue(obj.toString())) {
            return obj.toString();
        }

        if (!cetCodeMap.containsKey(obj.toString())) {
            loadCetCodeDefinitions();
        }

        String stringValue = cetCodeMap.get(obj.toString());

        return stringValue;
    }

    private void loadCetCodeDefinitions() {
        cetCodeMap = new HashMap<String, String>();

        if(customEntityInstanceService == null) {
            customEntityInstanceService = CDI.current().select(CustomEntityInstanceService.class).get();
        }
        List<CustomEntityInstance> entities = customEntityInstanceService.list();

        for (CustomEntityInstance customEntityInstance : entities) {
            cetCodeMap.put(customEntityInstance.cetCode, customEntityInstance.cetCode);
        }
    }

}

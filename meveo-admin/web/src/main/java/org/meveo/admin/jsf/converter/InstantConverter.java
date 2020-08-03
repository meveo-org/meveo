package org.meveo.admin.jsf.converter;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Named;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;

/**
 * @author Phu Bach | pbach1982@gmail.com
 * @version 6.10.0
 *
 */

@Named("instantConverter")
@ApplicationScoped
public class InstantConverter implements Converter<Object> {

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String str) {
        String format = (String) uiComponent.getAttributes().get("format");
        DateFormat df = new SimpleDateFormat(format, FacesContext.getCurrentInstance().getViewRoot().getLocale());

        try {
            return df.parse(str).toInstant();
        } catch (ParseException e) {
            return "";
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object obj) {
        String format = (String) uiComponent.getAttributes().get("format");
        if (obj instanceof Instant) {
            obj = new Timestamp(((Instant) obj).toEpochMilli());
        }
        DateFormat df = new SimpleDateFormat(format, FacesContext.getCurrentInstance().getViewRoot().getLocale());

        return df.format(obj);
    }
}

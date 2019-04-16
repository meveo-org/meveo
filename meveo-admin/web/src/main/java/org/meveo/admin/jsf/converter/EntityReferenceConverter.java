package org.meveo.admin.jsf.converter;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomTableService;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;

@Named("entityReferenceConverter")
@ApplicationScoped
public class EntityReferenceConverter implements Converter, Serializable {

    @Inject
    private CustomTableService customTableService;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    private volatile Map<String, LoadingCache<BigInteger, String>> cacheMap = new HashMap<>();

    private volatile Map<String, Map<BigInteger, Object>> valuesMap = new HashMap<>();

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
    	System.out.println(value);
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {

    	if(!(value instanceof BigInteger)) {
    		return null;
    	}
    	
        CustomFieldTemplate field = (CustomFieldTemplate) component.getAttributes().get("field");
        LoadingCache<BigInteger, String> cetCache = cacheMap.computeIfAbsent(field.getEntityClazzCetCode(), cetCode -> {
        	return CacheBuilder.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .removalListener((RemovalListener<BigInteger, String>) notification -> valuesMap.get(cetCode).remove(notification.getKey()))
                    .build(new FieldRepresentationLoader(cetCode));
        });

        return cetCache.getUnchecked((BigInteger) value);
    }

    private class FieldRepresentationLoader extends CacheLoader<BigInteger, String>{

        String cetCode;

        public FieldRepresentationLoader(String cetCode) {
            this.cetCode = cetCode;
        }

        @Override
        public String load(BigInteger value) {
            final Map<String, CustomFieldTemplate> referencedEntityFields = customFieldTemplateService.findByAppliesTo("CE_" + cetCode);
            final List<String> summaryFields = referencedEntityFields.values()
                    .stream()
                    .filter(f -> f.isSummary())
                    .map(CustomFieldTemplate::getDbFieldname)
                    .collect(Collectors.toList());

            final Map<String, Object> values = customTableService.findById(cetCode, value.longValue());

            valuesMap.computeIfAbsent(cetCode, (k) -> new HashMap<>())
                    .put(value, values);

            Map<String, Object> summaryValues = values.entrySet()
            	.stream()
            	.filter(e -> summaryFields.contains(e.getKey()))
                .collect(Collectors.toMap(e->e.getKey(),e->e.getValue()));

            return summaryValues.toString();
        }
    }

}

package org.meveo.admin.jsf.converter;

import java.io.Serializable;
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
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.storage.Repository;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomTableService;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.6.0
 **/
@Named("entityReferenceConverter")
@ApplicationScoped
public class EntityReferenceConverter implements Converter<Object>, Serializable {

	private static final long serialVersionUID = 2297474050618191644L;

	@Inject
	private CustomTableService customTableService;

	@Inject
	private CustomFieldTemplateService customFieldTemplateService;

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

	private volatile Map<String, LoadingCache<String, String>> cacheMap = new HashMap<>();

	private volatile Map<String, Map<String, Object>> valuesMap = new HashMap<>();

	private Repository repository;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		System.out.println(value);
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object uuid) {
		CustomFieldTemplate field = (CustomFieldTemplate) component.getAttributes().get("field");
		repository = (Repository) component.getAttributes().get("repository");

		// This converter only applies on entity references
		if (uuid == null || field.getFieldType() != CustomFieldTypeEnum.ENTITY) {
			return null;
		}

		String stringUuid = (String) uuid;

		LoadingCache<String, String> cetCache = cacheMap.computeIfAbsent(field.getEntityClazzCetCode(), cetCode -> {
			return CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES)
					.removalListener((RemovalListener<String, String>) notification -> valuesMap.get(cetCode).remove(notification.getKey()))
					.build(new FieldRepresentationLoader(cetCode));
		});

		return cetCache.getUnchecked(stringUuid);
	}

	private class FieldRepresentationLoader extends CacheLoader<String, String> {

		String cetCode;

		public FieldRepresentationLoader(String cetCode) {
			this.cetCode = cetCode;
		}

		@Override
		public String load(String uuid) {
			final Map<String, CustomFieldTemplate> referencedEntityFields = customFieldTemplateService.findByAppliesTo("CE_" + cetCode);
			final List<String> summaryFields = referencedEntityFields.values().stream().filter(f -> f.isSummary()).map(CustomFieldTemplate::getDbFieldname)
					.collect(Collectors.toList());

			String sqlConfigurationCode = repository != null ? repository.getSqlConfigurationCode() : null;
			CustomEntityTemplate cet = customEntityTemplateService.findByCode(cetCode);
			final Map<String, Object> values = customTableService.findById(sqlConfigurationCode, cetCode, uuid);

			valuesMap.computeIfAbsent(cetCode, (k) -> new HashMap<>()).put(uuid, values);

			Map<String, Object> summaryValues = values.entrySet().stream().filter(e -> e.getValue() != null).filter(e -> summaryFields.contains(e.getKey()))
					.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

			return summaryValues.toString();
		}
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}

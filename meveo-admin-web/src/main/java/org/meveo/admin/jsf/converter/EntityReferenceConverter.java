package org.meveo.admin.jsf.converter;

import java.io.Serializable;
import java.util.Collection;
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

import org.meveo.model.admin.User;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.EntityReferenceWrapper;
import org.meveo.model.crm.Provider;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.storage.Repository;
import org.meveo.service.admin.impl.UserService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.crm.impl.ProviderService;
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
	private UserService userService;
	
	@Inject
	private ProviderService providerService;

	private volatile Map<String, LoadingCache<String, String>> cacheMap = new HashMap<>();

	private volatile Map<String, Map<String, Object>> valuesMap = new HashMap<>();

	@Inject
	private Repository repository;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		System.out.println(value);
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object uuid) {
		CustomFieldTemplate field = (CustomFieldTemplate) component.getAttributes().get("field");

		// This converter only applies on entity references
		if (uuid == null || field.getFieldType() != CustomFieldTypeEnum.ENTITY) {
			return null;
		}
		
		if (field.getEntityClazz().equals(User.class.getName())) {
            User user = userService.findById(Long.parseLong(String.valueOf(uuid)));
            return user.getNameOrUsername();
       }else if (field.getEntityClazz().equals(Provider.class.getName())) {
           Provider provider = providerService.findById(Long.parseLong(String.valueOf(uuid)));
           return provider.getCode();
       }
		
		String stringUuid = null;
		if (uuid instanceof EntityReferenceWrapper) {
			EntityReferenceWrapper entityReferenceWrapper = (EntityReferenceWrapper) uuid;
			return entityReferenceWrapper.getCode();
		} else if (uuid instanceof String) {
			stringUuid = (String) uuid;
		}

		LoadingCache<String, String> cetCache = cacheMap.computeIfAbsent(field.getEntityClazzCetCode(), cetCode -> {
			return CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES)
					.removalListener((RemovalListener<String, String>) notification -> valuesMap.get(cetCode).remove(notification.getKey()))
					.build(new FieldRepresentationLoader(cetCode));
		});

		if (stringUuid != null) {
			return cetCache.getUnchecked(stringUuid);
		} else if (uuid instanceof Collection){
			Collection<?> uuids = (Collection<?>) uuid;
			if (uuids.isEmpty()) {
				return "[]";
			}
			
			Object firstItem = uuids.iterator().next();
			if (firstItem instanceof String) {
				return ((Collection<String>) uuid).stream()
						.map(cetCache::getUnchecked)
						.collect(Collectors.joining(",", "[", "]"));
			} else if (firstItem instanceof EntityReferenceWrapper) {
				return ((Collection<EntityReferenceWrapper>) uuid).stream()
						.map(EntityReferenceWrapper::getUuid)
						.map(cetCache::getUnchecked)
						.collect(Collectors.joining(",", "[", "]"));
			}
		}
		
		return String.valueOf(uuid);
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

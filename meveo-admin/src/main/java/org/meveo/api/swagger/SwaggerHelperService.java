package org.meveo.api.swagger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.BinaryProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.PropertyBuilder;
import io.swagger.models.properties.PropertyBuilder.PropertyId;
import io.swagger.models.properties.StringProperty;

/**
 * Utility class for Swagger documentation.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.8.0
 * @version 6.9.0
 */
@Stateless
public class SwaggerHelperService {

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

	@Inject
	private CustomFieldTemplateService customFieldTemplateService;

	public Map<String, Property> convertCftsToProperties(Map<String, CustomFieldTemplate> cfts) {

		Map<String, Property> result = new HashMap<>();

		if (!cfts.isEmpty()) {
			for (Entry<String, CustomFieldTemplate> entry : cfts.entrySet()) {
				Property property = convertToProperty(entry.getValue());
				result.put(entry.getKey(), property);
			}
		}

		return result;
	}

	public Property convertToProperty(CustomFieldTemplate cft) {

		Property result = new ObjectProperty();

		switch (cft.getFieldType()) {
		case SECRET:
		case STRING:
			result = new StringProperty();
			break;
		case DATE:
			result = new DateProperty();
			break;
		case LONG:
			result = new LongProperty();
			break;
		case DOUBLE:
			result = new DoubleProperty();
			break;
		case LIST:
			result.setName(CustomFieldTypeEnum.LIST.name());
			break;
		case ENTITY:
			result = buildTypeOfEntity(cft);
			result.setName(CustomFieldTypeEnum.ENTITY.name());
			break;
		case TEXT_AREA:
			result.setName(CustomFieldTypeEnum.TEXT_AREA.name());
			break;
		case LONG_TEXT:
			result.setName(CustomFieldTypeEnum.LONG_TEXT.name());
			break;
		case CHILD_ENTITY:
			result.setName(CustomFieldTypeEnum.CHILD_ENTITY.name());
			break;
		case MULTI_VALUE:
			result.setName(CustomFieldTypeEnum.MULTI_VALUE.name());
			break;
		case EXPRESSION:
			result.setName(CustomFieldTypeEnum.EXPRESSION.name());
			break;
		case BOOLEAN:
			result = new BooleanProperty();
			break;
		case EMBEDDED_ENTITY:
			result = buildTypeOfEntity(cft);
			result.setName(CustomFieldTypeEnum.EMBEDDED_ENTITY.name());
			break;
		case BINARY:
			result = new BinaryProperty();
			break;
		}

		result.setName(cft.getCode());
		result.setTitle(cft.getDescription());

		return result;
	}

	public ModelImpl cetToModel(CustomEntityTemplate cet) {

		Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());

		ModelImpl result = new ModelImpl();
		result.setType("object");
		result.setDescription(cet.getDescription());

		MapProperty mapProperty = new MapProperty();
		mapProperty.setType("cet");
		mapProperty.setFormat(cet.getCode());
		mapProperty.setDescription(cet.getDescription());
		Map<String, Property> properties = convertCftsToProperties(cfts);
		properties.put("_additionalProperties", mapProperty);

		result.setProperties(properties);
		result.setRequired(getRequiredFields(cfts));

		return result;
	}

	public Property buildTypeOfEntity(CustomFieldTemplate cft) {

		CustomEntityTemplate cet = customEntityTemplateService.findByCode(cft.getEntityClazzCetCode());
		if(cet == null) {
			return null;
		}
		
		Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());

		ObjectProperty result = new ObjectProperty();
	
		MapProperty mapProperty = new MapProperty();
		mapProperty.setType("cet");
		mapProperty.setFormat(cet.getCode());
		mapProperty.setDescription(cet.getDescription());
		Map<String, Property> properties = convertCftsToProperties(cfts);
		properties.put("_additionalProperties", mapProperty);

		result.setProperties(properties);
		result.setRequiredProperties(getRequiredFields(cfts));

		return result;
	}

	/**
	 * Retrieves a list of required {@link CustomFieldTemplate}.
	 * 
	 * @param cfts list of custom fields
	 * @return required custom fields
	 * @see CustomFieldTemplate
	 */
	public List<String> getRequiredFields(Map<String, CustomFieldTemplate> cfts) {

		List<String> result = new ArrayList<>();
		if (!cfts.isEmpty()) {
			result = cfts.entrySet().stream().filter(e -> e.getValue().isValueRequired()).map(e -> e.getValue().getCode()).collect(Collectors.toList());
		}

		return result;
	}

	public Model buildPrimitiveResponse(String variableName, String variableType) {

		variableType = variableType.toLowerCase();
		Map<PropertyId, Object> props = new HashMap<>();
		props.put(PropertyId.TITLE, variableName);
		Property prop = PropertyBuilder.build(variableType, "", props);
		prop.setDescription(variableName);

		return PropertyBuilder.toModel(prop);
	}

	public Model buildObjectResponse(String variableName) {

		Map<PropertyId, Object> props = new HashMap<>();
		props.put(PropertyId.TITLE, variableName);
		Property prop = PropertyBuilder.build("object", "", props);
		prop.setDescription(variableName);
		return PropertyBuilder.toModel(prop);
	}

	public List<Parameter> getGetPathParamaters(Map<String, Path> map) {

		Optional<Entry<String, Path>> getPath = map.entrySet().stream().filter(e -> e.getValue().getGet() != null).findAny();
		if (getPath.isPresent()) {
			Operation getOperation = getPath.get().getValue().getGet();
			return getOperation.getParameters();
		}

		return new ArrayList<>();
	}

	public List<Parameter> getPostPathParamaters(Map<String, Path> map) {

		Optional<Entry<String, Path>> postPath = map.entrySet().stream().filter(e -> e.getValue().getPost() != null).findAny();
		if (postPath.isPresent()) {
			Operation postOperation = postPath.get().getValue().getPost();
			return postOperation.getParameters();
		}

		return new ArrayList<>();
	}
	
	
}

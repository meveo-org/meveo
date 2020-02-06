package org.meveo.api.swagger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;

import io.swagger.models.Model;
import io.swagger.models.properties.BinaryProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.LongProperty;
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
 * @version 6.8.0
 */
public final class SwaggerHelper {

	private SwaggerHelper() {

	}

	public static Map<String, Property> convertCftsToProperties(Map<String, CustomFieldTemplate> cfts) {

		Map<String, Property> result = new HashMap<>();

		if (!cfts.isEmpty()) {
			for (Entry<String, CustomFieldTemplate> entry : cfts.entrySet()) {
				Property property = convertToProperty(entry.getValue());
				result.put(entry.getKey(), property);
			}
		}

		return result;
	}

	public static Property convertToProperty(CustomFieldTemplate cft) {

		Property result = new ObjectProperty();

		switch (cft.getFieldType()) {
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
			result.setName(CustomFieldTypeEnum.ENTITY.name());
			break;
		case TEXT_AREA:
			result.setName(CustomFieldTypeEnum.TEXT_AREA.name());
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
			result.setName(CustomFieldTypeEnum.EMBEDDED_ENTITY.name());
			break;
		case BINARY:
			result = new BinaryProperty();
			break;
		}

		result.setName(cft.getCode());

		return result;
	}

	public static Property convertTypeToProperty(CustomFieldTemplate cft) {

		Property result = new ObjectProperty();
		return result;
	}

	/**
	 * Retrieves a list of required {@link CustomFieldTemplate}.
	 * 
	 * @param cfts list of custom fields
	 * @return required custom fields
	 * @see CustomFieldTemplate
	 */
	public static List<String> getRequiredFields(Map<String, CustomFieldTemplate> cfts) {

		List<String> result = new ArrayList<>();
		if (!cfts.isEmpty()) {
			result = cfts.entrySet().stream().filter(e -> e.getValue().isValueRequired()).map(e -> e.getValue().getCode()).collect(Collectors.toList());
		}

		return result;
	}

	public static Model buildPrimitiveResponse(String variableName, String variableType) {

		variableType = variableType.toLowerCase();
		Map<PropertyId, Object> props = new HashMap<>();
		props.put(PropertyId.TITLE, variableName);
		Property prop = PropertyBuilder.build(variableType, "", props);
		prop.setDescription(variableName);
		return PropertyBuilder.toModel(prop);
	}

	public static Model buildObjectResponse(String variableName) {

		Map<PropertyId, Object> props = new HashMap<>();
		props.put(PropertyId.TITLE, variableName);
		Property prop = PropertyBuilder.build("object", "", props);
		prop.setDescription(variableName);
		return PropertyBuilder.toModel(prop);
	}
}

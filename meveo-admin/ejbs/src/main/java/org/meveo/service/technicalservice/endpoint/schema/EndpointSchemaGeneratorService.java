package org.meveo.service.technicalservice.endpoint.schema;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.internal.JSONPrinter;
import org.meveo.commons.utils.JsonUtils;
import org.meveo.json.schema.RootObjectSchema;
import org.meveo.service.crm.impl.JSONSchemaGenerator;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.9.0
 * @version 6.9.0
 */
@Stateless
public class EndpointSchemaGeneratorService {
	
	@Inject
	private JSONSchemaGenerator jsonSchemaGenerator;

	abstract static class CustomEndpointParameterProcessor {
		abstract String code();

		abstract CustomEndpointParameterProcessor parentTemplate();

		abstract Map<String, EndpointParameter> fields();

		abstract ObjectSchema.Builder createJsonSchemaBuilder(String schemaLocation, Set<String> allRefs);

		abstract ObjectSchema.Builder toRootJsonSchema(ObjectSchema original, Map<String, Schema> dependencies);
	}

	public CustomEndpointParameterProcessor processorOf(EndpointSchema endpointSchema) {

		return new CustomEndpointParameterProcessor() {
			@Override
			String code() {
				return endpointSchema.getName();
			}

			@Override
			Map<String, EndpointParameter> fields() {
				return endpointSchema.getEndpointParameters();
			}

			@Override
			CustomEndpointParameterProcessor parentTemplate() {
				return null;
			}

			@Override
			ObjectSchema.Builder createJsonSchemaBuilder(String schemaLocation, Set<String> allRefs) {
				return (ObjectSchema.Builder) ObjectSchema.builder() //
						.id(endpointSchema.getName()) //
						.title(endpointSchema.getName()) //
						.description(endpointSchema.getDescription()) //
						.schemaLocation(schemaLocation);
			}

			@Override
			ObjectSchema.Builder toRootJsonSchema(ObjectSchema original, Map<String, Schema> dependencies) {
				RootObjectSchema.Builder builder = RootObjectSchema.builder() //
						.copyOf(original) //
						.specificationVersion(JSONSchemaGenerator.JSON_SCHEMA_VERSION);

				dependencies.forEach(builder::addDefinition);
				return builder;
			}

		};
	}

	public String generateSchema(String schemaLocation, EndpointSchema endpointSchema) {

		return generateSchema(schemaLocation, processorOf(endpointSchema));
	}

	public String generateSchema(String schemaLocation, CustomEndpointParameterProcessor template) {

		Map<String, Schema> processed = new HashMap<>();
		ObjectSchema root = createSchema(schemaLocation, template, processed);
		processed.remove(template.code());

		Schema.Builder<?> builder = template.toRootJsonSchema(root, processed);

		StringWriter out = new StringWriter();
		JSONPrinter json = new JSONPrinter(out);

		builder.build().describeTo(json);
		return JsonUtils.beautifyString(out.toString());
	}

	private ObjectSchema createSchema(String schemaLocation, CustomEndpointParameterProcessor template, Map<String, Schema> processed) {

		Set<String> ownRefs = new HashSet<>();
		ObjectSchema result = buildSchema(schemaLocation, template, ownRefs);
		processed.put(template.code(), result);
		ownRefs.removeAll(processed.keySet());
		return result;
	}

	public ObjectSchema buildSchema(String schemaLocation, CustomEndpointParameterProcessor template, Set<String> allRefs) {

		ObjectSchema.Builder result = template.createJsonSchemaBuilder(schemaLocation, allRefs);
		Map<String, EndpointParameter> fields = template.fields();
		if (fields != null) {
			fields.forEach((key, field) -> {
				if (field.getCet() != null) {
					result.addPropertySchema(key, jsonSchemaGenerator.createSchemaOfCet(schemaLocation, field.getCet()));
					if (field.isRequired()) {
						result.addRequiredProperty(field.getName());
					}
				} else {
					result.addPropertySchema(key, createFieldSchema(schemaLocation, template, field, allRefs).build());
					if (field.isRequired()) {
						result.addRequiredProperty(field.getName());
					}
				}
			});
		}

		return result.build();
	}

	private Schema.Builder<?> createFieldSchema(String schemaLocation, CustomEndpointParameterProcessor template, EndpointParameter field, Set<String> allRefs) {

		Schema.Builder<?> result = null;

		switch (field.getType()) {
		case "string":
			result = createStringSchema(field);
			break;
		case "date":
			result = createDateSchema(field);
			break;
		case "integer":
			result = createNumberSchema(field);
			break;
		case "long":
			result = createLongSchema(field);
			break;
		case "boolean":
			result = createBooleanSchema(field);
			break;
		case "double":
			result = createNumberSchema(field);
			break;
		default:
			result = createStringSchema(field);
			break;
		}

		result = result.id(field.getId()) //
				.title(field.getName()) //
				.description(field.getDescription()) //
				.schemaLocation(schemaLocation);

		return result;
	}

	private Schema.Builder<BooleanSchema> createBooleanSchema(EndpointParameter field) {

		BooleanSchema.Builder result = BooleanSchema.builder();

		Object defaultValue = field.getDefaultValue();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}

		return result;
	}

	private Schema.Builder<NumberSchema> createNumberSchema(EndpointParameter field) {

		NumberSchema.Builder result = NumberSchema.builder();

		Object defaultValue = field.getDefaultValue();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}

		return result.requiresNumber(true);
	}

	private Schema.Builder<NumberSchema> createLongSchema(EndpointParameter field) {

		NumberSchema.Builder result = NumberSchema.builder();
		Object defaultValue = field.getDefaultValue();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}

		return result.requiresInteger(true);
	}

	private Schema.Builder<StringSchema> createStringSchema(EndpointParameter field) {

		StringSchema.Builder result = StringSchema.builder();
		result.minLength(1);
		Object defaultValue = field.getDefaultValue();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}

		return result.requiresString(true);
	}

	private Schema.Builder<StringSchema> createDateSchema(EndpointParameter field) {

		StringSchema.Builder result = StringSchema.builder();
		Object defaultValue = field.getDefaultValue();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}

		return result.requiresString(true).formatValidator(JSONSchemaGenerator.DATE_TIME_FORMAT);
	}
}

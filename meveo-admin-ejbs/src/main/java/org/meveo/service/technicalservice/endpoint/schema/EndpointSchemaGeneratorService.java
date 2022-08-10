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
import org.meveo.json.schema.RootObjectSchema;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.service.crm.impl.JSONSchemaGenerator;

/**
 * This is a helper service that generates the endpoint schema.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.9.0
 * @version 6.9.0
 * @see Endpoint
 */
@Stateless
public class EndpointSchemaGeneratorService {

	@Inject
	private JSONSchemaGenerator jsonSchemaGenerator;

	/**
	 * Base class that is use when processing an endpoint schema.
	 * 
	 * @see EndpointSchema
	 */
	abstract static class CustomEndpointParameterProcessor {
		abstract String code();

		abstract CustomEndpointParameterProcessor parentTemplate();

		abstract Map<String, EndpointParameter> fields();

		abstract ObjectSchema.Builder createJsonSchemaBuilder(String schemaLocation, Set<String> allRefs);

		abstract ObjectSchema.Builder toRootJsonSchema(ObjectSchema original, Map<String, Schema> dependencies);
	}

	/**
	 * Initialize the endpoint processor of a given endpoint schema.
	 * 
	 * @param endpointSchema endpoint definition with all its parameters
	 * @return initialized {@link CustomEndpointParameterProcessor}
	 */
	public CustomEndpointParameterProcessor processorOf(EndpointSchema endpointSchema) {

		return new CustomEndpointParameterProcessor() {
			@Override
			String code() {
				return endpointSchema.getName();
			}

			/**
			 * Returns all the parameters of a given endpoint.
			 */
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

	/**
	 * Generate the string schema representation of a given endpoint schema.
	 * 
	 * @param schemaLocation location of schema, defaults to "endpoint"
	 * @param endpointSchema the computed endpoint schema
	 * @return string schema representation of the endpoint
	 */
	public String generateSchema(String schemaLocation, EndpointSchema endpointSchema) {

		CustomEndpointParameterProcessor endpointProcessor = processorOf(endpointSchema);
		return generateSchema(schemaLocation, endpointProcessor);
	}

	/**
	 * Generate the string schema representation of a given endpoint schema.
	 * 
	 * @param schemaLocation location of schema, defaults to "endpoint"
	 * @param template       template class that will be use to initialize and
	 *                       process the schema.
	 * @return string schema representation of the endpoint
	 */
	public String generateSchema(String schemaLocation, CustomEndpointParameterProcessor template) {

		Map<String, Schema> processed = new HashMap<>();
		ObjectSchema root = createSchema(schemaLocation, template, processed);
		processed.remove(template.code());

		Schema.Builder<?> builder = template.toRootJsonSchema(root, processed);

		StringWriter out = new StringWriter();
		JSONPrinter json = new JSONPrinter(out);

		builder.build().describeTo(json);
		return JacksonUtil.beautifyString(out.toString());
	}

	/**
	 * Creates an object schema of a given endpoint parameter processor.
	 * 
	 * @param schemaLocation location of schema
	 * @param template       endpoint parameter processor
	 * @param processed      dependencies that will be add to the builder.
	 * @return object schema representation of the given endpoint parameter
	 *         processor
	 */
	private ObjectSchema createSchema(String schemaLocation, CustomEndpointParameterProcessor template, Map<String, Schema> processed) {

		Set<String> ownRefs = new HashSet<>();
		ObjectSchema result = buildSchema(schemaLocation, template, ownRefs);
		processed.put(template.code(), result);
		ownRefs.removeAll(processed.keySet());
		return result;
	}

	/**
	 * Builds an object schema of a given endpoint parameter processor.
	 * 
	 * @param schemaLocation location of schema
	 * @param template       endpoint parameter processor
	 * @param allRefs        use when type is entity
	 * @return object schema representation of the given endpoint parameter
	 *         processor
	 */
	public ObjectSchema buildSchema(String schemaLocation, CustomEndpointParameterProcessor template, Set<String> allRefs) {

		ObjectSchema.Builder result = template.createJsonSchemaBuilder(schemaLocation, allRefs);
		Map<String, EndpointParameter> fields = template.fields();
		if (fields != null) {
			fields.forEach((key, field) -> {
				if (field != null) {
					if (field.getCet() != null) {
						result.addPropertySchema(key,
								jsonSchemaGenerator.createSchemaOfCet(schemaLocation, field.getCet()));
						if (field.isRequired()) {
							result.addRequiredProperty(field.getName());
						}
					} else {
						result.addPropertySchema(key,
								createFieldSchema(schemaLocation, template, field, allRefs).build());
						if (field.isRequired()) {
							result.addRequiredProperty(field.getName());
						}
					}
				}
			});
		}

		return result.build();
	}

	/**
	 * Create a field schema of a given type.
	 * 
	 * @param schemaLocation location of schema
	 * @param template       endpoint parameter processor
	 * @param field          the endpoint parameter
	 * @param allRefs        dependencies
	 * @return Schema builder that represents the given field
	 */
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

	/**
	 * Creates a schema builder for boolean data type.
	 * 
	 * @param field endpoint parameter
	 * @return schema builder representation
	 */
	private Schema.Builder<BooleanSchema> createBooleanSchema(EndpointParameter field) {

		BooleanSchema.Builder result = BooleanSchema.builder();

		Object defaultValue = field.getDefaultValue();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}

		return result;
	}

	/**
	 * Creates a schema builder for number data type.
	 * 
	 * @param field endpoint parameter
	 * @return schema builder representation
	 */
	private Schema.Builder<NumberSchema> createNumberSchema(EndpointParameter field) {

		NumberSchema.Builder result = NumberSchema.builder();

		Object defaultValue = field.getDefaultValue();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}

		return result.requiresNumber(true);
	}

	/**
	 * Creates a schema builder for long data type.
	 * 
	 * @param field endpoint parameter
	 * @return schema builder representation
	 */
	private Schema.Builder<NumberSchema> createLongSchema(EndpointParameter field) {

		NumberSchema.Builder result = NumberSchema.builder();
		Object defaultValue = field.getDefaultValue();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}

		return result.requiresInteger(true);
	}

	/**
	 * Creates a schema builder for string data type.
	 * 
	 * @param field endpoint parameter
	 * @return schema builder representation
	 */
	private Schema.Builder<StringSchema> createStringSchema(EndpointParameter field) {

		StringSchema.Builder result = StringSchema.builder();
		result.minLength(1);
		Object defaultValue = field.getDefaultValue();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}

		return result.requiresString(true);
	}

	/**
	 * Creates a schema builder for date data type.
	 * 
	 * @param field endpoint parameter
	 * @return schema builder representation
	 */
	private Schema.Builder<StringSchema> createDateSchema(EndpointParameter field) {

		StringSchema.Builder result = StringSchema.builder();
		Object defaultValue = field.getDefaultValue();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}

		return result.requiresString(true).formatValidator(JSONSchemaGenerator.DATE_TIME_FORMAT);
	}
}

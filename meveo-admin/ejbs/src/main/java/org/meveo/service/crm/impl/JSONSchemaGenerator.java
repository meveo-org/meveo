package org.meveo.service.crm.impl;

import java.io.StringWriter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.everit.json.schema.*;
import org.everit.json.schema.internal.JSONPrinter;
import org.meveo.admin.exception.BusinessException;
import org.meveo.json.schema.RootCombinedSchema;
import org.meveo.json.schema.RootObjectSchema;
import org.meveo.json.schema.RootRelationSchema;
import org.meveo.model.BusinessEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldMapKeyEnum;
import org.meveo.model.crm.custom.CustomFieldMatrixColumn;
import org.meveo.model.crm.custom.CustomFieldMatrixColumn.CustomFieldColumnUseEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;

@Stateless
public class JSONSchemaGenerator {
	@Inject
	CustomEntityTemplateService entityTemplateService;
	
	@Inject
	CustomRelationshipTemplateService relationshipTemplateService;
	
	@Inject
	CustomFieldTemplateService fieldService;
	
	public String generateSchema(String schemaLocation, boolean activeTemplatesOnly) {
		Map<String, Schema> processed = new HashMap<>();
		Set<String> primary = new HashSet<>();
		
		// Entity templates
		processCustomEntityTemplates(schemaLocation, activeTemplatesOnly, primary, processed);

		// Relationship templates
		processCustomRelationshipTemplates(schemaLocation, activeTemplatesOnly, primary, processed);

		RootCombinedSchema.Builder builder = RootCombinedSchema
				.builder()
				.specificationVersion(JSON_SCHEMA_VERSION)
				;

		processed.forEach((k, v) -> builder.addDefinition(k, v, primary.contains(k)));
		
		StringWriter out = new StringWriter();
		JSONPrinter json = new JSONPrinter(out);
		
		builder.build().describeTo(json);
		return out.toString();
	}
	
	public String generateEntitiesSchema(String schemaLocation, boolean activeTemplatesOnly) {
		Map<String, Schema> processed = new HashMap<>();
		Set<String> primary = new HashSet<>();
		
		// Entity templates
		processCustomEntityTemplates(schemaLocation, activeTemplatesOnly, primary, processed);

		RootCombinedSchema.Builder builder = RootCombinedSchema
				.builder()
				.specificationVersion(JSON_SCHEMA_VERSION)
				;

		processed.forEach((k, v) -> builder.addDefinition(k, v, primary.contains(k)));
		
		StringWriter out = new StringWriter();
		JSONPrinter json = new JSONPrinter(out);
		
		builder.build().describeTo(json);
		return out.toString();
	}
	
	public String generateRelationshipsSchema(String schemaLocation, boolean activeTemplatesOnly) {
		Map<String, Schema> processed = new HashMap<>();
		Set<String> primary = new HashSet<>();
		
		// Relationship templates
		processCustomRelationshipTemplates(schemaLocation, activeTemplatesOnly, primary, processed);

		RootCombinedSchema.Builder builder = RootCombinedSchema
				.builder()
				.specificationVersion(JSON_SCHEMA_VERSION)
				;

		processed.forEach((k, v) -> builder.addDefinition(k, v, primary.contains(k)));
		
		StringWriter out = new StringWriter();
		JSONPrinter json = new JSONPrinter(out);
		
		builder.build().describeTo(json);
		return out.toString();
	}
	
	private void processCustomEntityTemplates(String schemaLocation, boolean activeTemplatesOnly, Set<String> primary, Map<String, Schema> processed) {
		Collection<CustomEntityTemplate> templates = entityTemplateService.list(activeTemplatesOnly ? Boolean.TRUE : null);
		templates.forEach(t -> createSchema(schemaLocation, processorOf(t), processed));
		primary.addAll(
			templates
			.stream()
			.map(CustomEntityTemplate::getCode)
			.collect(Collectors.toSet())					
		);		
	}
	
	private void processCustomRelationshipTemplates(String schemaLocation, boolean activeTemplatesOnly, Set<String> primary, Map<String, Schema> processed) {
		Collection<CustomRelationshipTemplate> templates = relationshipTemplateService.list(activeTemplatesOnly ? Boolean.TRUE : null);
		templates.forEach(t -> createSchema(schemaLocation, processorOf(t), processed));
		primary.addAll(
			templates
			.stream()
			.map(CustomRelationshipTemplate::getCode)
			.collect(Collectors.toSet())					
		);
	}
	
	public String generateEntityTemplateSchema(String schemaLocation, String templateCode) {
		return generateSchema(schemaLocation, entityTemplateService.findByCode(templateCode));
	}
	
	public String generateRelationshipTemplateSchema(String schemaLocation, String templateCode) throws BusinessException{
		return generateSchema(schemaLocation, relationshipTemplateService.findByCode(templateCode));
	}
	
	private String generateSchema(String schemaLocation, CustomEntityTemplate template) {
		return generateSchema(schemaLocation, processorOf(template));
	}
	
	private String generateSchema(String schemaLocation, CustomRelationshipTemplate template) {
		return generateSchema(schemaLocation, processorOf(template));
	}
	
	private String generateSchema(String schemaLocation, CustomTemplateProcessor template) {
		Map<String, Schema> processed = new HashMap<>();	
		ObjectSchema root = createSchema(schemaLocation, template, processed);
		processed.remove(template.code());
		
		Schema.Builder<?> builder = template.toRootJsonSchema(root, processed);
		
		StringWriter out = new StringWriter();
		JSONPrinter json = new JSONPrinter(out);
		
		builder.build().describeTo(json);
		return out.toString();
	}
	
	private ObjectSchema createSchema(String schemaLocation, CustomTemplateProcessor template, Map<String, Schema> processed) {
		Set<String> ownRefs = new HashSet<>();
		ObjectSchema result = buildSchema(schemaLocation, template, ownRefs);
		processed.put(template.code(), result);
		ownRefs.removeAll(processed.keySet());
		return result;
	}
	
	private ObjectSchema buildSchema(String schemaLocation, CustomTemplateProcessor template, Set<String> allRefs) {
		ObjectSchema.Builder result = template.createJsonSchemaBuilder(schemaLocation, allRefs);
		Map<String, CustomFieldTemplate> fields = template.fields();
		fields.forEach((key, field) -> {
			result.addPropertySchema(key, createFieldSchema(schemaLocation, template, field, allRefs).build());
			if (field.isValueRequired()) {
				result.addRequiredProperty(field.getCode());
			}
		});

		// If has super template
		if(template.parentTemplate() != null){
			ReferenceSchema.Builder referenceSchema = ReferenceSchema.builder();
			referenceSchema.refValue(DEFINITIONS_PREFIX + template.parentTemplate().code());
			final CombinedSchema combinedSchema = CombinedSchema.allOf(Collections.singletonList(referenceSchema.build()))
					.build();
			return CombinedObjectSchema.Factory.get(result, combinedSchema);
		}

		return result.build();
	}
	
	private Schema.Builder<?> createFieldSchema(String schemaLocation, CustomTemplateProcessor template, CustomFieldTemplate field, Set<String> allRefs) {
		Schema.Builder<?> result;
		switch (field.getStorageType()) {
			case SINGLE:
				switch (field.getFieldType()) {
					case DATE:   result = createDateSchema(field); break;
					case ENTITY: result = createReferenceSchema(field, allRefs); break;
//					case CHILD_ENTITY:
						//TODO: Handle this case
//						throw new IllegalStateException(
//							"Child entity type of field supports only list of entities" +
//							": field = " + field +
//							", storageType = " + field.getStorageType()
//						);
					case TEXT_AREA:
					case EMBEDDED_ENTITY:
					case STRING:  result = createStringSchema(field); break;
					case LIST:    result = createEnumSchema(field); break;
					case LONG:    result = createLongSchema(field); break;
					case DOUBLE:  result = createNumberSchema(field); break;
		            case MULTI_VALUE:
		                throw new IllegalStateException(
		                	"Multi-value type of field supports only matrix storage" + 
		                	": field = " + field + 
		                	", storageType = " + field.getStorageType()
		                );
				default:
					result = createStringSchema(field); break;
				}
				break;
			case LIST:
				result = createArraySchema(field, createElementSchema(schemaLocation, template, field, allRefs).build());
				break;
			case MAP:
				CustomFieldMapKeyEnum mapKeyType = field.getMapKeyType();
				switch (mapKeyType) {
					case STRING:
					case RON:
						String propertyNamePattern = mapKeyType == CustomFieldMapKeyEnum.RON ?
								"^(\\d+)?\\.\\.(\\d+)?$" : "^.*$";
						result = ObjectSchema
							.builder()
							.requiresObject(true)
							.patternProperty(propertyNamePattern, createElementSchema(schemaLocation, template, field, allRefs).build());
						break;
					default:
						if (!mapKeyType.isKeyUse()) {
							throw new  IllegalStateException(
								"Field has invalid mapKey type (not for key use)" +
								": field = " + field + 
								", storageType = " + field.getStorageType() +
								", mapKeyType = " + mapKeyType
							);
						} else {
							throw new  IllegalStateException(
									"Field has unsupported mapKey type" +
									": field = " + field + 
									", storageType = " + field.getStorageType() +
									", mapKeyType = " + mapKeyType
								);
						}
				}
				break;
			case MATRIX:
				result = createArraySchema(field, createMatrixSchema(schemaLocation, field, template, allRefs).build()); break;
			default:
				throw new IllegalStateException("Unknown storage type: field = " + field + ", storageType = " + field.getStorageType());
		}
		result
			.readOnly(!field.isAllowEdit())
			.nullable(!field.isValueRequired())
		;
		result
			.id(field.getAppliesTo() + '_' + field.getCode())
			.title(template.code() + "." + field.getCode())
			.description(field.getDescription())
			.schemaLocation(schemaLocation);
		return result;
	}
	
	private Schema.Builder<?> createElementSchema(String schemaLocation, CustomTemplateProcessor template, CustomFieldTemplate field, Set<String> allRefs) {
		Schema.Builder<?> result;
		switch (field.getFieldType()) {
			case DATE:   result = createDateSchema(field); break;
			case ENTITY: 
			case CHILD_ENTITY: 
				result = createReferenceSchema(field, allRefs); break; 
				// throw new IllegalStateException("Child entity type of field supports only list of entities: field = " + field + ", storageType = " + field.getStorageType());
			case TEXT_AREA:
			case STRING:  result = createStringSchema(field); break;
			case LIST:    result = createEnumSchema(field); break;
			case LONG:    result = createLongSchema(field); break;
			case DOUBLE:  result = createNumberSchema(field); break;
	        case MULTI_VALUE:
	            throw new IllegalStateException("Multi-value type of field supports only matrix: field = " + field + ", storageType = " + field.getStorageType());
	        default:
				result = createStringSchema(field); break;
		}
		return result
			//.readOnly(!field.isAllowEdit())
			//.nullable(!field.isValueRequired())
			.id(field.getAppliesTo() + '_' + field.getCode() + "_$element")
			.title(template.code() + "." + field.getCode() + " Element")
			//.description(field.getDescription())
			.schemaLocation(schemaLocation)
			;
	}
	
	private Schema.Builder<ObjectSchema> createMatrixSchema(String schemaLocation, CustomFieldTemplate field, CustomTemplateProcessor template, Set<String> allRefs) {
		ObjectSchema.Builder result = ObjectSchema.builder();
		result
			.requiresObject(true)
			//.readOnly(!field.isAllowEdit())
			//.nullable(!field.isValueRequired())
			.id(field.getAppliesTo() + '_' + field.getCode() + "_$element")
			.title(template.code() + "." + field.getCode() + " Element")
			//.description(field.getDescription())
			.schemaLocation(schemaLocation);
		
		ObjectSchema.Builder keyBuilder = ObjectSchema.builder();
		Consumer<CustomFieldMatrixColumn> keyColumnProcessor = c -> {
			StringSchema.Builder b = StringSchema.builder().requiresString(true);
			if (c.getKeyType() == CustomFieldMapKeyEnum.RON) {
				b.pattern("^(\\d+)?\\.\\.(\\d+)?$");
			}
			b
				.id(field.getAppliesTo() + '_' + field.getCode() + "_key_" + c.getCode())
				.title(template.code() + "." + field.getCode() + " Key column " + c.getCode() + " @ " + c.getPosition() + " - " + c.getLabel())
				.schemaLocation(schemaLocation)
				.nullable(false)
			;
			keyBuilder.addPropertySchema(c.getCode(), b.build());
			keyBuilder.addRequiredProperty(c.getCode());
		};
		field.getMatrixColumns()
			.stream()
			.filter(c -> c.getColumnUse() == CustomFieldColumnUseEnum.USE_KEY)
			.sorted(Comparator.comparingInt(CustomFieldMatrixColumn::getPosition))
			.forEach(keyColumnProcessor)
		;
		
		result.addPropertySchema("key", keyBuilder.build());
		
		Schema.Builder<?> valueBuilder;
		switch (field.getFieldType()) {
			case DATE:   valueBuilder = createDateSchema(field); break;
			case ENTITY: valueBuilder = createReferenceSchema(field, allRefs); break;
//			case CHILD_ENTITY:  TODO: handle this case
//				throw new IllegalStateException("Child entity type of field supports only list of entities: field = " + field + ", storageType = " + field.getStorageType());
			case TEXT_AREA:
			case STRING:  valueBuilder = createStringSchema(field); break;
			case LIST:    valueBuilder = createEnumSchema(field); break;
			case LONG:    valueBuilder = createLongSchema(field); break;
			case DOUBLE:  valueBuilder = createNumberSchema(field); break;
	        case MULTI_VALUE:
	        	ObjectSchema.Builder valuesBuilder = ObjectSchema.builder();
	    		Consumer<CustomFieldMatrixColumn> valueColumnProcessor = c -> {
	    			Schema.Builder<?> b;
	    			switch (c.getKeyType()) {
	    				case DOUBLE:
	    					b = NumberSchema.builder().requiresNumber(true); break;
	    				case LONG:
	    					b = NumberSchema.builder().requiresInteger(true); break;
	    				default:
	    					b = StringSchema.builder().requiresString(true);
	    			}
	    			b
	    				.id(field.getAppliesTo() + '_' + field.getCode() + "_value_" + c.getCode())
	    				.title(template.code() + "." + field.getCode() + " Value column " + c.getCode() + " @ " + c.getPosition() + " - " + c.getLabel())
	    				.schemaLocation(schemaLocation)
	    			;
	    			valuesBuilder.addPropertySchema(c.getCode(), b.build());
	    		};
	    		field.getMatrixColumns()
					.stream()
					.filter(c -> c.getColumnUse() == CustomFieldColumnUseEnum.USE_VALUE)
					.sorted(Comparator.comparingInt(CustomFieldMatrixColumn::getPosition))
					.forEach(valueColumnProcessor)
				;
	    		valueBuilder = valuesBuilder;
	    		break;
	        default:
	        	valueBuilder = createStringSchema(field); break;
		}
		valueBuilder
			.id(field.getAppliesTo() + '_' + field.getCode() + "_value")
			.title(template.code() + "." + field.getCode() + " Value")
			.schemaLocation(schemaLocation)
		;
		
		result.addPropertySchema("value", valueBuilder.build());
		result.addRequiredProperty("key");
		return result;
	}
	
	private Schema.Builder<NumberSchema> createNumberSchema(CustomFieldTemplate field) {
		NumberSchema.Builder result = NumberSchema.builder();
		if (null != field.getMaxValue()) {
			result.maximum(field.getMaxValue()).exclusiveMaximum(true);					
		}
		if (null != field.getMinValue()) {
			result.minimum(field.getMinValue()).exclusiveMinimum(true);					
		}
		Object defaultValue = field.getDefaultValueConverted();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}
		return result.requiresNumber(true);		
	}
	
	private Schema.Builder<NumberSchema> createLongSchema(CustomFieldTemplate field) {
		NumberSchema.Builder result = NumberSchema.builder();
		if (null != field.getMaxValue()) {
			result.maximum(field.getMaxValue()).exclusiveMaximum(false);					
		}
		if (null != field.getMinValue()) {
			result.minimum(field.getMinValue()).exclusiveMinimum(false);					
		}
		Object defaultValue = field.getDefaultValueConverted();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}
		return result.requiresInteger(true);		
	}

	private Schema.Builder<StringSchema> createStringSchema(CustomFieldTemplate field) {
		StringSchema.Builder result = StringSchema.builder();
		Object defaultValue = field.getDefaultValue();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}
		if (null != field.getRegExp()) {
			result.pattern(field.getRegExp());
		}
		if (null != field.getMaxValue()) {
			result.maxLength(field.getMaxValue().intValue());					
		}
		if (null != field.getMinValue()) {
			result.minLength(field.getMinValue().intValue());					
		}
		return result.requiresString(true);		
	}
	
	private Schema.Builder<StringSchema> createDateSchema(CustomFieldTemplate field) {
		StringSchema.Builder result = StringSchema.builder();
		Object defaultValue = field.getDefaultValue();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}
		return result.requiresString(true).formatValidator(DATE_TIME_FORMAT);		
	}
	
	private Schema.Builder<EnumSchema> createEnumSchema(CustomFieldTemplate field) {
		EnumSchema.Builder result = EnumSchema.builder();
		Object defaultValue = field.getDefaultValue();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}
		@SuppressWarnings("unchecked")
		final Map<Object, String> enumKeyVal = (Map<Object, String>)(Map<?, String>)field.getListValues();
		if (null != enumKeyVal) {
			result.possibleValues(enumKeyVal.keySet());
		}
		return result;		
	}
	
	private Schema.Builder<ArraySchema> createArraySchema(CustomFieldTemplate field, Schema arrayElementSchema) {
		ArraySchema.Builder result = ArraySchema.builder();
		Object defaultValue = field.getDefaultValue();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}
		return result
			.allItemSchema(arrayElementSchema)
			.requiresArray(true)
			.uniqueItems(true);		
	}
	
	private Schema.Builder<ReferenceSchema> createReferenceSchema(CustomFieldTemplate field, Set<String> allRefs) {
		ReferenceSchema.Builder result = ReferenceSchema.builder();
		Object defaultValue = field.getDefaultValue();
		if (null != defaultValue) {
			result.defaultValue(defaultValue);
		}
		String refCode = field.getEntityClazzCetCode();
		if (StringUtils.isEmpty(refCode)) {
			result.refValue("#entity-classes/" + field.getEntityClazz());
		} else {
			result.refValue(DEFINITIONS_PREFIX + refCode);
			allRefs.add(refCode);
		}
		return result;				
	}
	
	private CustomTemplateProcessor processorOf(CustomEntityTemplate entityTemplate) {
		return new CustomTemplateProcessor() {
			@Override
			String code() {
				return entityTemplate.getCode();
			}
			
			@Override
			Map<String, CustomFieldTemplate> fields() {
				return fieldService.findByAppliesTo(entityTemplate.getAppliesTo());
			}

			@Override
			CustomTemplateProcessor parentTemplate(){
				return entityTemplate.getSuperTemplate() != null ? processorOf(entityTemplate.getSuperTemplate()) : null;
			}

			@Override
			ObjectSchema.Builder createJsonSchemaBuilder(String schemaLocation, Set<String> allRefs) {
				return (ObjectSchema.Builder) ObjectSchema.builder()
					.requiresObject(true)
					.id(entityTemplate.getCode())
					.title(entityTemplate.getName())
					.description(entityTemplate.getDescription())
					.schemaLocation(schemaLocation)					
					;
			}
			
			@Override
			ObjectSchema.Builder toRootJsonSchema(ObjectSchema original, Map<String, Schema> dependencies) {
				RootObjectSchema.Builder builder = RootObjectSchema
						.builder()
						.copyOf(original)
						.specificationVersion(JSON_SCHEMA_VERSION)
						;
					
				dependencies.forEach(builder::addDefinition);
				return builder;
			}

		};
	}
	
	private CustomTemplateProcessor processorOf(CustomRelationshipTemplate relationshipTemplate) {
		return new CustomTemplateProcessor() {
			
			@Override
			String code() {
				return relationshipTemplate.getCode();
			}

			@Override
			Map<String, CustomFieldTemplate> fields() {
				return fieldService.findByAppliesTo(relationshipTemplate.getAppliesTo());
			}

			@Override
			CustomTemplateProcessor parentTemplate(){
				return null;
			}
			
			@Override
			ObjectSchema.Builder createJsonSchemaBuilder(String schemaLocation, Set<String> allRefs) {
				RelationSchema.Builder result = RelationSchema.builder()
					.requiresRelation(true)
					.id(relationshipTemplate.getCode())
					.title(relationshipTemplate.getName())
					.description(relationshipTemplate.getDescription())
					.schemaLocation(schemaLocation)					
					;
				
				CustomEntityTemplate node;
				
				node = relationshipTemplate.getStartNode();
				if (node != null) {
					result.source(createReference(node));
					allRefs.add(node.getCode());
				}
				
				node = relationshipTemplate.getEndNode();
				if (node != null) {
					result.target(createReference(node));
					allRefs.add(node.getCode());
				}
				return result;
			}
			
			@Override
			ObjectSchema.Builder toRootJsonSchema(ObjectSchema original, Map<String, Schema> dependencies) {
				RootRelationSchema.Builder builder = RootRelationSchema
						.builder()
						.copyOf((RelationSchema)original)
						.specificationVersion(JSON_SCHEMA_VERSION)
						;
					
				dependencies.forEach(builder::addDefinition);
				return builder;
			}
			
			private ReferenceSchema createReference(CustomEntityTemplate node) {
				ReferenceSchema.Builder r = ReferenceSchema.builder()
					.refValue(DEFINITIONS_PREFIX + node.getCode());
				return r.build();
			}
			
			@SuppressWarnings("unused")
			private ReferenceViewSchema createReference_obsolete(CustomEntityTemplate node, String nodeKeys) {
				List<String> actualKeys;
				if (nodeKeys != null) {
					actualKeys = Arrays.asList( nodeKeys.split(",") );
				} else {
					actualKeys = processorOf(node)
						.fields()
						.values()
						.stream()
						.filter(CustomFieldTemplate::isUnique)
						.map(BusinessEntity::getCode)
						.collect(Collectors.toList())
					;
				}
				ReferenceViewSchema.Builder r = ReferenceViewSchema.builder()
					.refValue(DEFINITIONS_PREFIX + node.getCode());
				actualKeys.forEach(p -> r.addRefProperty(p.trim()));
				return r.build();
			}
			
		};
	}
	
	abstract static class CustomTemplateProcessor {
		abstract String code();
		abstract CustomTemplateProcessor parentTemplate();
		abstract Map<String, CustomFieldTemplate> fields();
		abstract ObjectSchema.Builder createJsonSchemaBuilder(String schemaLocation, Set<String> allRefs);
		abstract ObjectSchema.Builder toRootJsonSchema(ObjectSchema original, Map<String, Schema> dependencies);
	}
	
	private static final FormatValidator DATE_TIME_FORMAT = FormatValidator.forFormat("date-time");
	private static final String JSON_SCHEMA_VERSION = "http://json-schema.org/draft-07/schema";
	private static final String DEFINITIONS_PREFIX = "#/definitions/";
}

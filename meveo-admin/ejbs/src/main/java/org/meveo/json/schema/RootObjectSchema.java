package org.meveo.json.schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.everit.json.schema.CombinedObjectSchema;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.internal.JSONPrinter;

public class RootObjectSchema extends CombinedObjectSchema {

	public static class Builder extends CombinedObjectSchema.Builder {
	
	    private final Map<String, Schema> definitions = new LinkedHashMap<>();
	    private String version = "http://json-schema.org/draft-06/schema";
	    private CombinedSchema combinedSchema;
		
        @Override
        public RootObjectSchema build() {
            return new RootObjectSchema(this);
        }
        
        public Builder combinedSchema(CombinedSchema schema) {
        	this.combinedSchema = schema;
        	return this;
        }
        
        public Builder addDefinition(String name, Schema schema) {
        	definitions.put(name, schema);
        	return this;
        }
        
        public Builder specificationVersion(String version) {
        	this.version = version;
        	return this;
        }
        
        public Builder copyOf(ObjectSchema other) {
        	additionalProperties(other.permitsAdditionalProperties());
        	other.getPropertySchemas().forEach((k, v) -> addPropertySchema(k, v));
        	other.getRequiredProperties().forEach(p -> addRequiredProperty(p));
        	maxProperties(other.getMaxProperties());
        	minProperties(other.getMinProperties());
        	other.getPatternProperties().forEach((k, v) -> patternProperty(k.pattern(), v));
        	other.getPropertyDependencies().forEach((k, v) -> v.forEach(d -> propertyDependency(k, d)));
        	requiresObject(other.requiresObject());
        	other.getSchemaDependencies().forEach((k, v) -> schemaDependency(k, v));
        	schemaOfAdditionalProperties(other.getSchemaOfAdditionalProperties());
        	propertyNameSchema(other.getPropertyNameSchema());
        	
        	if(other instanceof CombinedObjectSchema) {
        		combinedSchema(((CombinedObjectSchema) other).getCombinedSchema());
        	}
        	
        	
        	return this
    			.defaultValue(other.getDefaultValue())
    			.defaultValue(other.getDescription())
    			.id(other.getId())
    			.nullable(other.isNullable())
    			.readOnly(other.isReadOnly())
				.versionable(other.isVersionable())
    			.schemaLocation(other.getSchemaLocation())
    			.title(other.getTitle())
    			.writeOnly(other.isWriteOnly())
				.storages(other.getStorages())
				.indexType(other.getIndexType())
        		;
        }

		@Override
		public Builder additionalProperties(boolean additionalProperties) {
			super.additionalProperties(additionalProperties);
			return this;
		}

		@Override
		public Builder addPropertySchema(String propName, Schema schema) {
			super.addPropertySchema(propName, schema);
			return this;
		}

		@Override
		public Builder addRequiredProperty(String propertyName) {
			super.addRequiredProperty(propertyName);
			return this;
		}

		@Override
		public Builder maxProperties(Integer maxProperties) {
			super.maxProperties(maxProperties);
			return this;
		}

		@Override
		public Builder minProperties(Integer minProperties) {
			super.minProperties(minProperties);
			return this;
		}

		@Override
		public Builder patternProperty(Pattern pattern, Schema schema) {
			super.patternProperty(pattern, schema);
			return this;
		}

		@Override
		public Builder patternProperty(String pattern, Schema schema) {
			super.patternProperty(pattern, schema);
			return this;
		}

		@Override
		public Builder propertyDependency(String ifPresent, String mustBePresent) {
			super.propertyDependency(ifPresent, mustBePresent);
			return this;
		}

		@Override
		public Builder requiresObject(boolean requiresObject) {
			super.requiresObject(requiresObject);
			return this;
		}

		@Override
		public Builder schemaDependency(String ifPresent, Schema expectedSchema) {
			super.schemaDependency(ifPresent, expectedSchema);
			return this;
		}

		@Override
		public Builder schemaOfAdditionalProperties(Schema schemaOfAdditionalProperties) {
			super.schemaOfAdditionalProperties(schemaOfAdditionalProperties);
			return this;
		}

		@Override
		public Builder propertyNameSchema(Schema propertyNameSchema) {
			super.propertyNameSchema(propertyNameSchema);
			return this;
		}

		@Override
		public Builder title(String title) {
			super.title(title);
			return this;
		}

		@Override
		public Builder description(String description) {
			super.description(description);
			return this;
		}

		@Override
		public Builder id(String id) {
			super.id(id);
			return this;
		}

		@Override
		public Builder schemaLocation(String schemaLocation) {
			super.schemaLocation(schemaLocation);
			return this;
		}

		@Override
		public Builder defaultValue(Object defaultValue) {
			super.defaultValue(defaultValue);
			return this;
		}

		@Override
		public Builder nullable(Boolean nullable) {
			super.nullable(nullable);
			return this;
		}

		@Override
		public Builder readOnly(Boolean readOnly) {
			super.readOnly(readOnly);
			return this;
		}

		@Override
		public Builder writeOnly(Boolean writeOnly) {
			super.writeOnly(writeOnly);
			return this;
		}

		@Override
		public Builder storages(List<String> storages) {
			super.storages(storages);
			return this;
		}

		@Override
		public Builder versionable(Boolean versionable) {
			super.versionable(versionable);
			return this;
		}

		@Override
		public Builder indexType(String indexType) {
			super.indexType(indexType);
			return this;
		}
	}
	
    public static Builder builder() {
        return new Builder();
    }
    
    private final Map<String, Schema> definitions;
    private final String version;
	
	public RootObjectSchema(Builder builder) {
		super(builder);
		setCombinedSchema(builder.combinedSchema);
		this.definitions = Collections.unmodifiableMap( new LinkedHashMap<>(builder.definitions) );
		this.version = builder.version;
	}
	
	public Map<String, Schema> getDefinitions() {
		return definitions;
	}
	
	public String getVersion() {
		return version;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof RootObjectSchema) {
        	RootObjectSchema that = (RootObjectSchema) o;
            return that.canEqual(this) && 
            	   Objects.equals(definitions, that.definitions) &&
            	   Objects.equals(version, that.version) &&
            	   super.equals(o);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), definitions, version);
    }
    
    @Override
    protected void describePropertiesTo(JSONPrinter writer) {
    	writer.ifPresent("$schema", version);
        if (null != definitions && !definitions.isEmpty()) {
            writer.key("definitions").printSchemaMap(definitions);
        }
    	super.describePropertiesTo(writer);
    }
    

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof RootObjectSchema;
    }

}

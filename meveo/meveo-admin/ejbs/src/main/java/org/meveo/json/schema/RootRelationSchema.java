package org.meveo.json.schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.everit.json.schema.ReferenceViewSchema;
import org.everit.json.schema.RelationSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.internal.JSONPrinter;

public class RootRelationSchema extends RelationSchema {

	public static class Builder extends RelationSchema.Builder {
	
	    private final Map<String, Schema> definitions = new LinkedHashMap<>();
	    private String version = "http://json-schema.org/draft-06/schema";
		
        @Override
        public RootRelationSchema build() {
            return new RootRelationSchema(this);
        }
        
        public Builder addDefinition(String name, Schema schema) {
        	definitions.put(name, schema);
        	return this;
        }
        
        public Builder specificationVersion(String version) {
        	this.version = version;
        	return this;
        }
        
        public Builder copyOf(RelationSchema other) {
        	additionalProperties(other.permitsAdditionalProperties());
        	other.getPropertySchemas().forEach((k, v) -> addPropertySchema(k, v));
        	other.getRequiredProperties().forEach(p -> addRequiredProperty(p));
        	maxProperties(other.getMaxProperties());
        	minProperties(other.getMinProperties());
        	other.getPatternProperties().forEach((k, v) -> patternProperty(k.pattern(), v));
        	other.getPropertyDependencies().forEach((k, v) -> v.forEach(d -> propertyDependency(k, d)));
        	requiresRelation(other.requiresRelation());
        	source(other.getSource());
        	target(other.getTarget());
        	other.getSchemaDependencies().forEach((k, v) -> schemaDependency(k, v));
        	schemaOfAdditionalProperties(other.getSchemaOfAdditionalProperties());
        	propertyNameSchema(other.getPropertyNameSchema());
        	
        	
        	return this
    			.defaultValue(other.getDefaultValue())
    			.defaultValue(other.getDescription())
    			.id(other.getId())
    			.nullable(other.isNullable())
    			.readOnly(other.isReadOnly())
    			.schemaLocation(other.getSchemaLocation())
    			.title(other.getTitle())
    			.writeOnly(other.isWriteOnly())
        		;
        }
        
		@Override
		public Builder propertyDependency(String ifPresent, String mustBePresent) {
			super.propertyDependency(ifPresent, mustBePresent);
			return this;
		}
		
        public Builder inbound(ReferenceViewSchema schema) {
        	super.source(schema);
        	return this;
        }
        
        public Builder outbound(ReferenceViewSchema schema) {
        	super.target(schema);
        	return this;
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
		public Builder requiresRelation(boolean requiresRelation) {
			super.requiresRelation(requiresRelation);
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
        
	}
	
    public static Builder builder() {
        return new Builder();
    }
    
    private final Map<String, Schema> definitions;
    private final String version;
	
	public RootRelationSchema(Builder builder) {
		super(builder);
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
        if (o instanceof RootRelationSchema) {
        	RootRelationSchema that = (RootRelationSchema) o;
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
        return other instanceof RootRelationSchema;
    }

}

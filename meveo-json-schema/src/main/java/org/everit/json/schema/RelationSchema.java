package org.everit.json.schema;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.everit.json.schema.internal.JSONPrinter;

public class RelationSchema extends ObjectSchema {
	public static class Builder extends ObjectSchema.Builder {
	
	    private boolean requiresRelation = true;
	    private ReferenceSchema source;
	    private ReferenceSchema target;
		
	    public Builder() {
	    	super();
	    	super.requiresObject(false);
	    }
	    
        @Override
        public RelationSchema build() {
            return new RelationSchema(this);
        }

        @Deprecated
        @Override
        public Builder requiresObject(boolean requiresObject) {
        	throw new UnsupportedOperationException();
        }
        
        public Builder requiresRelation(boolean requiresRelation) {
            this.requiresRelation = requiresRelation;
            return this;
        }
        
        public Builder source(ReferenceSchema schema) {
        	this.source = schema;
        	return this;
        }
        
        public Builder target(ReferenceSchema schema) {
        	this.target = schema;
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
		public Builder propertyDependency(String ifPresent, String mustBePresent) {
			super.propertyDependency(ifPresent, mustBePresent);
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
		public Builder storages(List<String> storages) {
			super.storages(storages);
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
    
    private final boolean requiresRelation;
    private final ReferenceSchema source;
    private final ReferenceSchema target;
	
	public RelationSchema(Builder builder) {
		super(builder);
		this.requiresRelation = builder.requiresRelation;
		this.source = builder.source;
		this.target = builder.target;
	}
	
	@Deprecated
	@Override
    public boolean requiresObject() {
        throw new UnsupportedOperationException();
    }
	
    public boolean requiresRelation() {
        return requiresRelation;
    }
    
    public ReferenceSchema getSource() {
    	return source;
    }
    
    public ReferenceSchema getTarget() {
    	return target;
    }

	
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof RelationSchema) {
        	RelationSchema that = (RelationSchema) o;
            return that.canEqual(this) && 
            	   requiresRelation == that.requiresRelation &&
            	   Objects.equals(source, that.source) &&
            	   Objects.equals(target, that.target) &&
            	   super.equals(o);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), requiresRelation, source, target);
    }
    
    @Override
    protected void describePropertiesTo(JSONPrinter writer) {
        if (requiresRelation) {
            writer.key("type").value("relation");
        }
        if (null != source) {
        	writer.key("source");
        	source.describeTo(writer);
        }
        if (null != target) {
        	writer.key("target");
        	target.describeTo(writer);
        }
    	super.describePropertiesTo(writer);
    }
    

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof RelationSchema;
    }
}

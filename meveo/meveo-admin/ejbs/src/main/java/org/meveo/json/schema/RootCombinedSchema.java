package org.meveo.json.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.Visitor;
import org.everit.json.schema.internal.JSONPrinter;

public class RootCombinedSchema extends Schema {

	public static class Builder extends Schema.Builder<RootCombinedSchema> {
	
	    private final Map<String, Schema> definitions = new LinkedHashMap<>();
	    private final Set<String> primaryDefinitions = new HashSet<>();
	    private String version = "http://json-schema.org/draft-06/schema";
		
        @Override
        public RootCombinedSchema build() {
            return new RootCombinedSchema(this);
        }
        
        public Builder addDefinition(String name, Schema schema) {
        	return addDefinition(name, schema, true);
        }
        
        public Builder addDefinition(String name, Schema schema, boolean topLevel) {
        	definitions.put(name, schema);
        	if (topLevel) {
        		primaryDefinitions.add(name);
        	} else {
        		primaryDefinitions.remove(name);
        	}
        	return this;
        }

        
        public Builder specificationVersion(String version) {
        	this.version = version;
        	return this;
        }
	}
	
    public static Builder builder() {
        return new Builder();
    }
    
    private final Map<String, Schema> definitions;
    private final Set<String> primaryDefinitions;
    private final String version;
	
	public RootCombinedSchema(Builder builder) {
		super(builder);
		this.definitions = Collections.unmodifiableMap( new LinkedHashMap<>(builder.definitions) );
		this.primaryDefinitions = Collections.unmodifiableSet(new HashSet<>(builder.primaryDefinitions));
		this.version = builder.version;
	}
	
	public Map<String, Schema> getDefinitions() {
		return definitions;
	}
	
	public Map<String, Schema> getPrimaryDefinitions() {
		Map<String, Schema> result = new LinkedHashMap<>();
		result.putAll(definitions);
		result.keySet().retainAll(primaryDefinitions);
		return Collections.unmodifiableMap(result);
	}	
	
	public String getVersion() {
		return version;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof RootCombinedSchema) {
        	RootCombinedSchema that = (RootCombinedSchema) o;
            return that.canEqual(this) && 
             	   Objects.equals(definitions, that.definitions) &&
             	   Objects.equals(primaryDefinitions, that.primaryDefinitions) &&
             	   Objects.equals(version, that.version) &&
             	   super.equals(o);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), definitions, primaryDefinitions, version);
    }
    
    @Override
    public boolean definesProperty(final String field) {
        List<Schema> matching = new ArrayList<>();
        for (Schema subschema : getPrimarySubschemas()) {
            if (subschema.definesProperty(field)) {
                matching.add(subschema);
            }
        }
        try {
            getCriterion().validate(primaryDefinitions.size(), matching.size());
        } catch (ValidationException e) {
            return false;
        }
        return true;
    }
    
    @Override
    protected void describePropertiesTo(JSONPrinter writer) {
    	writer.ifPresent("$schema", version);
        if (null != definitions && !definitions.isEmpty()) {
            writer.key("definitions").printSchemaMap(definitions);
        }
        writer.key(getCriterion().toString());
        writer.array();
        definitions
        	.entrySet()
        	.stream()
        	.filter(e -> primaryDefinitions.contains(e.getKey()))
        	.map(e -> e.getValue() instanceof ReferenceSchema ? 
        		(ReferenceSchema)e.getValue() :
        		ReferenceSchema.builder().refValue("#/definitions/" + e.getKey()).build()
        	)
        	.forEach(subschema -> subschema.describeTo(writer));
        writer.endArray();
    	super.describePropertiesTo(writer);
    }
    

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof RootCombinedSchema;
    }
    
	@Override
	protected void accept(Visitor visitor) {
		// Convert to standard combined schema
		Schema.Builder<CombinedSchema> builder = CombinedSchema.builder()
			.criterion(getCriterion())
			.subschemas(getPrimarySubschemas())
			
			.defaultValue(getDefaultValue())
			.defaultValue(getDescription())
			.id(getId())
			.nullable(isNullable())
			.readOnly(isReadOnly())
			.schemaLocation(getSchemaLocation())
			.title(getTitle())
			.writeOnly(isWriteOnly())
		;
		visitor.visit(builder.build());
	}
	
	private Collection<Schema> getPrimarySubschemas() {
		Map<String, Schema> result = new LinkedHashMap<>();
		result.putAll(definitions);
		result.keySet().retainAll(primaryDefinitions);
		return result.values();
	}
	
	private CombinedSchema.ValidationCriterion getCriterion() {
		return CombinedSchema.ONE_CRITERION;
	}
}

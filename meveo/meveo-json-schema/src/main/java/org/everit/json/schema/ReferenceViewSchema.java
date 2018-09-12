package org.everit.json.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.everit.json.schema.internal.JSONPrinter;

public class ReferenceViewSchema extends ReferenceSchema {

	public static class Builder extends ReferenceSchema.Builder {
	
	    private final List<String> refProperties = new ArrayList<>();
		
        @Override
        public ReferenceViewSchema build() {
            return new ReferenceViewSchema(this);
        }
        
        public Builder addRefProperty(String property) {
        	this.refProperties.add(property);
        	return this;
        }
        
        @Override
        public Builder refValue(String refValue) {
        	super.refValue(refValue);
        	return this;
        }
	}
	
    public static Builder builder() {
        return new Builder();
    }
    
    private final List<String> refProperties;
	
	public ReferenceViewSchema(Builder builder) {
		super(builder);
		this.refProperties = Collections.unmodifiableList(new ArrayList<>(builder.refProperties));
	}

	public List<String> getRefferedProperties() {
		return refProperties;
	}	

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof ReferenceViewSchema) {
        	ReferenceViewSchema that = (ReferenceViewSchema) o;
            return that.canEqual(this) && 
             	   Objects.equals(refProperties, that.refProperties) &&
             	   super.equals(o);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), refProperties);
    }
    
    @Override
    public boolean definesProperty(String field) {
    	return super.definesProperty(field) && refProperties.contains(field);
    }
    
    @Override
    protected void describePropertiesTo(JSONPrinter writer) {
    	super.describePropertiesTo(writer);
        if (!refProperties.isEmpty()) {
            writer.key("referred").value(refProperties);
        }
    }
    

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof ReferenceViewSchema;
    }


}

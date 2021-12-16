package org.meveo.interfaces;

import static org.meveo.interfaces.EntityOrRelation.ENTITY;
import static org.meveo.interfaces.EntityOrRelation.RELATION;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * @author clement.bareth
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "discriminator")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Entity.class, name = ENTITY),
        @JsonSubTypes.Type(value = EntityRelation.class, name = RELATION)
})
public abstract class EntityOrRelation implements Serializable {

    private static final long serialVersionUID = 5259238864845049449L;
    
    public static final String ENTITY = "entity";
    public static final String RELATION = "relation";
    
    protected String type;
	protected String name;
	
	protected Map<String, PropertyInstance> properties = new HashMap<>();
	
	protected int index = 0;
	private boolean drop;
	private Map<String, Object> metaData = new HashMap<>();
	
	@JsonIdentityInfo(property = "uid", generator = ObjectIdGenerators.PropertyGenerator.class, scope = String.class)
	public String getUID() {
		return getNameIndexed();
	}

	public Map<String, Object> getMetaData() {
		return metaData;
	}

	public void setMetaData(Map<String, Object> metaData) {
		this.metaData = metaData;
	}

	public String getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	public void rename(String newName){
		this.name = newName;
	}

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonSetter("properties")
    public void setProperties(Map<String, Object> properties) {
    	properties.forEach((name, value) -> {
    		PropertyInstance property = new PropertyInstance();
    		property.setEntityOrRelation(this);
    		property.setName(name);
    		property.setValue(value);
    		this.properties.put(name, property);
    	});
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @JsonIgnore
    public void setDrop(boolean drop){
		this.drop = drop;
	}

	@JsonIgnore
	public boolean isDrop() {
		return drop;
	}

	@JsonProperty("name")
	public String getNameIndexed() {
		if(this.index >= 1) {
			return this.name + this.index;
		}
		return this.name;
	}
	
	@JsonIgnore
	public Map<String, PropertyInstance> getPropertiesInstances() {
		return this.properties;
	}

	@JsonGetter("properties")
	public Map<String, Object> getProperties() {
		Map<String, Object> propertiesAsMap = new HashMap<>();
		this.properties.entrySet()
			.stream()
			.forEach(entry -> propertiesAsMap.put(entry.getKey(), entry.getValue().getValue()));
		 return propertiesAsMap;
	}

	@SuppressWarnings("unchecked")
	public EntityOrRelation addProperty(String name, Object value, boolean multivalued){
		PropertyInstance propertyInstance = this.properties.computeIfAbsent(name, s -> {
			PropertyInstance property = new PropertyInstance();
    		property.setEntityOrRelation(this);
    		property.setName(name);
    		property.setValue(value);
    		return property;
		});
		
		if(multivalued) {
			Set<Object> values = new HashSet<>((Collection<Object>) propertyInstance.getValue());
			values.addAll((Collection<?>) value);
			propertyInstance.setValue(values);
		} else{
			propertyInstance.setValue(value);
		}
		
		return this;
	}

	public void merge(EntityOrRelation variable) {
		final Map<String, Object> variableProperties = variable.getProperties();
		variableProperties.forEach((propertyName, value) -> {
			boolean multivalued = value instanceof Collection;
			addProperty(propertyName, value, multivalued);
		});
	}

	public int getIndex() {
		return index;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + ((this.properties == null) ? 0 : this.properties.hashCode());
		result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
		return result;
	}

	public boolean equals(EntityOrRelation obj) {
		return this.name.equals(obj.name) && this.index == obj.index;
	}

	public String getCompoundName(){
		return name;
	}

	public Map<String, Map<String, Object>> toMap(){
		return Collections.singletonMap(getCompoundName(), getProperties());
	}
	
}

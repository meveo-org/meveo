package org.meveo.interfaces;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.*;

import static org.meveo.interfaces.EntityOrRelation.ENTITY;
import static org.meveo.interfaces.EntityOrRelation.RELATION;

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
	protected Map<String, Object> properties;
	protected int index = 0;

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

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @JsonProperty("name")
	public String getNameIndexed() {
		if(this.index >= 1) {
			return this.name + this.index;
		}
		return this.name;
	}

	public Map<String, Object> getProperties() {
		return this.properties;
	}

	@SuppressWarnings("unchecked")
	public void addProperty(String name, Object value, boolean multivalued){
		if(multivalued){
			Set<Object> values = (Set<Object>) this.properties.computeIfAbsent(name, s -> new HashSet<>());
			values.addAll((Collection<?>) value);
		}else{
			this.properties.put(name, value);
		}
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
		return Collections.singletonMap(getCompoundName(), properties);
	}

	@Override
	public String toString() {
		return getCompoundName();
	}
}

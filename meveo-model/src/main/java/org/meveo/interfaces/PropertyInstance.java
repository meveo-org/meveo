package org.meveo.interfaces;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonManagedReference;

public class PropertyInstance {
	
	private String name;
	
	private EntityOrRelation entityOrRelation;
	
	private Object value;
	private Map<String, Object> metaData;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public EntityOrRelation getEntityOrRelation() {
		return entityOrRelation;
	}
	
	public void setEntityOrRelation(EntityOrRelation entityOrRelation) {
		this.entityOrRelation = entityOrRelation;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public Map<String, Object> getMetaData() {
		return metaData;
	}
	
	public void setMetaData(Map<String, Object> metaData) {
		this.metaData = metaData;
	}
	
	public String getFullType() {
		return entityOrRelation.getType().concat(".").concat(name);
	}
	
	public String getUID() {
		return value.toString();
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

}

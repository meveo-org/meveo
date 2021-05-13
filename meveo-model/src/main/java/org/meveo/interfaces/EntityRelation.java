package org.meveo.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class EntityRelation extends EntityOrRelation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6518730869102165917L;
	private Entity source;
	private Entity target;
	
	@JsonProperty("source")
	public String sourceName() {
		return this.source.getNameIndexed();
	}
	
	@JsonProperty("target")
	public String targetName() {
		return this.target.getNameIndexed();
	}
	
	@JsonIgnore
	public Entity getSource() {
		return this.source;
	}
	
	@JsonIgnore
	public Entity getTarget() {
		return this.target;
	}

	public void setSource(Entity source) {
		this.source = source;
	}

	public void setTarget(Entity target) {
		this.target = target;
	}

	public static class Builder {
		private String type;
		private String name;
		private Entity source;
		private Entity target;
		private Map<String, Object> properties  = new HashMap<>();
		private int index = 0;

		public Builder source(Entity source) {
			this.source = source;
			return this;
		}

		public Builder properties(Map<String, Object> properties){
			this.properties = properties;
			return this;
		}
		
		public Builder target(Entity target) {
			this.target = target;
			return this;
		}

		public Builder type(String type) {
			this.type = type;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder index(int index) {
			this.index  = index;
			return this;
		}
		
		public EntityRelation build() {
			return new EntityRelation(this);
		}
	}

	private EntityRelation(Builder builder) {
		this.type = builder.type;
		this.index = builder.index;
		this.name = builder.name;
		this.source = builder.source;
		this.target = builder.target;
		setProperties(builder.properties);
	}

	@Override
	public String getCompoundName() {
		return sourceName()+"-"+targetName();
	}
	
	@Override
	public String toString() {
		return "EntityRelation [type=" + type + ", getCompoundName()=" + getCompoundName() + "]";
	}
}

package org.meveo.interfaces;

import java.util.HashMap;
import java.util.Map;

public class Entity extends EntityOrRelation {

	/**
	 * 
	 */
	private static final long serialVersionUID = -576882373339110810L;

	public static class Builder {
		private String type;
		private String name;
		private Map<String, Object> properties = new HashMap<>();
		private int index = 0;

		public Builder properties(Map<String, Object> properties){
			this.properties = properties;
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
			this.index = index;
			return this;
		}

		public Entity build() {
			return new Entity(this);
		}
	}

	private Entity(Builder builder) {
		this.index = builder.index;
		this.type = builder.type;
		this.name = builder.name;
		this.properties = builder.properties;
	}
}

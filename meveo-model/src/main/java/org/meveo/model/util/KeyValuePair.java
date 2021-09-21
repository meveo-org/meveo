/**
 * 
 */
package org.meveo.model.util;

import java.util.Objects;

/**
 * 
 * @author heros
 * @since 
 * @version
 */
public class KeyValuePair {
	
	private String key;
	private Object value;
	
	/**
	 * Instantiates a new KeyValuePair
	 *
	 * @param key
	 * @param value
	 */
	public KeyValuePair(String key, Object value) {
		super();
		this.key = key;
		this.value = value;
	}
	/**
	 * @return the {@link #key}
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return the {@link #value}
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyValuePair other = (KeyValuePair) obj;
		return Objects.equals(key, other.key) && Objects.equals(value, other.value);
	}

}

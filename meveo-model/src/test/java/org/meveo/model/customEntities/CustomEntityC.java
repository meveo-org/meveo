/**
 * 
 */
package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class CustomEntityC implements CustomEntity {
	
	private String value;
	
	private String otherValue;
	
	private String uuid;


	@Override
	public String getCetCode() {
		return "CustomEntityC";
	}
	
	
	/**
	 * @return the {@link #value}
	 */
	public String getValue() {
		return value;
	}


	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the {@link #uuid}
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public boolean isEqual(CustomEntity other) {
		if (!(other instanceof CustomEntityC)) {
			return false;
		}
		
		var otherB = (CustomEntityC) other;
		if(otherB.value.equals(this.value)) {
			return true;
		}
		
		return CustomEntity.super.isEqual(other);
	}


	/**
	 * @return the {@link #otherValue}
	 */
	public String getOtherValue() {
		return otherValue;
	}


	/**
	 * @param otherValue the otherValue to set
	 */
	public void setOtherValue(String otherValue) {
		this.otherValue = otherValue;
	}

}

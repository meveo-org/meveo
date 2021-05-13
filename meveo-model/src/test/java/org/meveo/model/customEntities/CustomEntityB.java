/**
 * 
 */
package org.meveo.model.customEntities;

import org.meveo.model.CustomEntity;
import org.meveo.model.customEntities.annotations.Relation;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class CustomEntityB implements CustomEntity {
	
	private String value;
	
	private String otherValue;
	
	private String uuid;
	
	private BtoA circularRef;
	
	@Relation("HasCTarget")
	private CustomEntityC target;


	@Override
	public String getCetCode() {
		return "CustomEntityB";
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
		if (!(other instanceof CustomEntityB)) {
			return false;
		}
		
		var otherB = (CustomEntityB) other;
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


	/**
	 * @return the {@link #target}
	 */
	public CustomEntityC getTarget() {
		return target;
	}


	/**
	 * @param target the target to set
	 */
	public void setTarget(CustomEntityC target) {
		this.target = target;
	}


	/**
	 * @return the {@link #circularRef}
	 */
	public BtoA getCircularRef() {
		return circularRef;
	}


	/**
	 * @param circularRef the circularRef to set
	 */
	public void setCircularRef(BtoA circularRef) {
		this.circularRef = circularRef;
	}

}

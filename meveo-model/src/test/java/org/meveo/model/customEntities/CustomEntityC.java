/**
 * 
 */
package org.meveo.model.customEntities;

import java.util.ArrayList;
import java.util.List;

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
	
	private CtoA circularRef;
	
	private List<String> list = new ArrayList<>();
	
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


	/**
	 * @return the {@link #circularRef}
	 */
	public CtoA getCircularRef() {
		return circularRef;
	}


	/**
	 * @param circularRef the circularRef to set
	 */
	public void setCircularRef(CtoA circularRef) {
		this.circularRef = circularRef;
	}


	/**
	 * @return the {@link #list}
	 */
	public List<String> getList() {
		return list;
	}


	/**
	 * @param list the list to set
	 */
	public void setList(List<String> list) {
		this.list = list;
	}

}

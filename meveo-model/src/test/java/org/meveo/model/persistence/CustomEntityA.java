/**
 * 
 */
package org.meveo.model.persistence;

import org.meveo.model.CustomEntity;
import org.meveo.model.customEntities.annotations.Relation;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class CustomEntityA implements CustomEntity {
	
	private String value;
	
	@Relation("HasTarget")
	private CustomEntityB target;
	
	private AtoB aToBRelation;
	
	private String uuid;

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
	 * @return the {@link #target}
	 */
	public CustomEntityB getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(CustomEntityB target) {
		this.target = target;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String getCetCode() {
		return "CustomEntityA";
	}

	@Override
	public String getUuid() {
		return uuid;
	}

	/**
	 * @return the {@link #aToBRelation}
	 */
	public AtoB getaToBRelation() {
		return aToBRelation;
	}

	/**
	 * @param aToBRelation the aToBRelation to set
	 */
	public void setaToBRelation(AtoB aToBRelation) {
		this.aToBRelation = aToBRelation;
	}
	

}

/**
 * 
 */
package org.meveo.model.customEntities;

import org.meveo.model.CustomRelation;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class CtoA implements CustomRelation<CustomEntityC, CustomEntityA> {
	
	@JsonIgnore
	private CustomEntityA target;
	
	@JsonIgnore
	private CustomEntityC source;
	
	private String test = "test";
	
	private String uuid;
	
	/**
	 * Instantiates a new AtoB
	 *
	 * @param source
	 * @param target
	 */
	public CtoA(CustomEntityC source, CustomEntityA target) {
		super();
		this.source = source;
		this.target = target;
	}

	public CtoA() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getCrtCode() {
		return "AtoB";
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

	/**
	 * @return the {@link #test}
	 */
	public String getTest() {
		return test;
	}

	/**
	 * @param test the test to set
	 */
	public void setTest(String test) {
		this.test = test;
	}

	/**
	 * @return the {@link #target}
	 */
	public CustomEntityA getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(CustomEntityA target) {
		this.target = target;
	}

	/**
	 * @return the {@link #source}
	 */
	public CustomEntityC getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(CustomEntityC source) {
		this.source = source;
	}

}

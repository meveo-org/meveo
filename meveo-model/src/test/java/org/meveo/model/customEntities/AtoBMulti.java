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
public class AtoBMulti implements CustomRelation<CustomEntityA, CustomEntityB> {
	
	@JsonIgnore
	private CustomEntityA source;
	
	@JsonIgnore
	private CustomEntityB target;
	
	private String test = "test";
	
	private String uuid;
	
	/**
	 * Instantiates a new AtoB
	 *
	 * @param source
	 * @param target
	 */
	public AtoBMulti(CustomEntityA source, CustomEntityB target) {
		super();
		this.source = source;
		this.target = target;
	}

	public AtoBMulti() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getCrtCode() {
		return "AtoB";
	}

	/**
	 * @return the {@link #source}
	 */
	public CustomEntityA getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(CustomEntityA source) {
		this.source = source;
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
	
	


}

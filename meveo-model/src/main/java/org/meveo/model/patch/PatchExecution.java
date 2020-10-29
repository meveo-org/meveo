/**
 * 
 */
package org.meveo.model.patch;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * Represent a patch that has been executed
 * 
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 */
@Entity(name = "PatchExecution")
@Table(name = "patch_execution")
public class PatchExecution {

	@Id
	@Column(name = "name")
	private String name;
	
    @Type(type = "numeric_boolean")
	@Column(name = "ran")
	private boolean ran;

	/**
	 * @return the {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the {@link #ran}
	 */
	public boolean isRan() {
		return ran;
	}

	/**
	 * @param ran the ran to set
	 */
	public void setRan(boolean ran) {
		this.ran = ran;
	}
	
}

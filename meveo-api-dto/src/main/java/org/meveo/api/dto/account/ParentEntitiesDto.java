package org.meveo.api.dto.account;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import io.swagger.annotations.ApiModelProperty;

/**
 * A wrapper dto for a list of {@link ParentEntityDto}.
 *
 * @author Tony Alejandro.
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ParentEntitiesDto implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The parent. */
	@ApiModelProperty("List of parent entities")
	private List<ParentEntityDto> parent;

	/**
	 * Gets the parent.
	 *
	 * @return the parent
	 */
	public List<ParentEntityDto> getParent() {
		if (parent == null) {
			parent = new ArrayList<>();
		}
		return parent;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent the new parent
	 */
	public void setParent(List<ParentEntityDto> parent) {
		this.parent = parent;
	}
}
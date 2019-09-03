package org.meveo.model.storage;

/**
 * @author Edward P. Legaspi
 */
public enum DataSeparationTypeEnum {

	LOGICAL, PHYSICAL;

	public String getLabel() {
		return "DataSeparationTypeEnum." + name();
	}
}

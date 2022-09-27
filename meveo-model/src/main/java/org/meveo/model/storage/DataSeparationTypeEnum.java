package org.meveo.model.storage;

/**
 * @author Edward P. Legaspi
 */
/**
 * 
 * @author ClementBareth
 * @since 
 * @version
 */
public enum DataSeparationTypeEnum {

	LOGICAL, PHYSICAL;

	public String getLabel() {
		return "DataSeparationTypeEnum." + name();
	}

	@Override
	public String toString() {
		return getLabel();
	}
	
}

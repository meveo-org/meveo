package org.meveo.api.dto.adapter;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @since 6.9.0
 * @version 6.9.0
 */
public class MapElements {

	@XmlElement
	public String key;
	@XmlElement
	public Integer value;

	private MapElements() {
	} // Required by JAXB

	public MapElements(String key, Integer value) {
		this.key = key;
		this.value = value;
	}
}

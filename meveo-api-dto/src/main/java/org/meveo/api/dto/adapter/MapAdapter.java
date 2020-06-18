package org.meveo.api.dto.adapter;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @since 6.9.0
 * @version 6.9.0
 */
public class MapAdapter extends XmlAdapter<MapElements[], Map<String, Integer>> {

	public MapElements[] marshal(Map<String, Integer> arg0) throws Exception {
		MapElements[] mapElements = new MapElements[arg0.size()];
		int i = 0;
		for (Map.Entry<String, Integer> entry : arg0.entrySet())
			mapElements[i++] = new MapElements(entry.getKey(), entry.getValue());

		return mapElements;
	}

	public Map<String, Integer> unmarshal(MapElements[] arg0) throws Exception {
		Map<String, Integer> r = new HashMap<String, Integer>();
		for (MapElements mapelement : arg0)
			r.put(mapelement.key, mapelement.value);
		return r;
	}
}
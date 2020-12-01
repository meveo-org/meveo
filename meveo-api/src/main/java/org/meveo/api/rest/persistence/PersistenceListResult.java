package org.meveo.api.rest.persistence;

import java.util.List;
import java.util.Map;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.13
 */
public class PersistenceListResult {

	private Integer count;
	private List<Map<String, Object>> result;

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public List<Map<String, Object>> getResult() {
		return result;
	}

	public void setResult(List<Map<String, Object>> result) {
		this.result = result;
	}
}

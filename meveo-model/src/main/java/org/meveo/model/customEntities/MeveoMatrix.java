/**
 * 
 */
package org.meveo.model.customEntities;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.util.CustomFieldUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.type.TypeReference;

public class MeveoMatrix<ValueType> {
	
	private final Map<String, Object> matrix;
	
	@JsonCreator
	public MeveoMatrix(Map<String, Object> matrix) {
		this.matrix = matrix;
	}
	
	@JsonCreator
	public MeveoMatrix(String matrix) {
		this.matrix = JacksonUtil.convert(matrix, new TypeReference<Map<String, Object>>() {});
	}
	
	public ValueType get(String... keys) {
		// TODO: Find a way to define the CFT
		return (ValueType) CustomFieldUtils.matchMatrixValue(null, matrix, keys);
	}
	
	public ValueType getClosestMatch(String... keys) {
		return (ValueType) CustomFieldUtils.matchClosestValue(matrix, StringUtils.join(keys, "|"));
	}

}

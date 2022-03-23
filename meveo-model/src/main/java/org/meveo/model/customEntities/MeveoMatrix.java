/**
 * 
 */
package org.meveo.model.customEntities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.util.CustomFieldUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.type.TypeReference;

public class MeveoMatrix<ValueType> {
	
	@JsonValue
	private Map<String, Object> matrix;
	
	public MeveoMatrix() {
		this.matrix = new HashMap<>();
	}
	
	public MeveoMatrix(Map<String, Object> matrix) {
		this.matrix = matrix;
	}
	
	public ValueType get(String... keys) {
		return (ValueType) CustomFieldUtils.matchMatrixValue(matrix, keys);
	}
	
	public ValueType getClosestMatch(String... keys) {
		return (ValueType) CustomFieldUtils.matchClosestValue(matrix, StringUtils.join(keys, "|"));
	}
	
	/**
	 * @param keysAndValue The first parameters corresponds to the keys and the last parameters to the value
	 */
	@SuppressWarnings("unchecked")
	public void set(Object... keysAndValue) {
		if(keysAndValue.length == 1) {
			throw new IllegalArgumentException("At least two arguments (key and value) should be provided");
		}
		
		int lastElementIndex = keysAndValue.length - 1;

		try {
			ValueType value = (ValueType) keysAndValue[lastElementIndex];
			String[] keys = Arrays.copyOfRange(keysAndValue, 0, lastElementIndex, String[].class);
			String concatenatedKey = StringUtils.join(keys, "|");
			this.matrix.put(concatenatedKey, value);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Incorrect value type or a key is not a string");
		}
		
	}
	
	public String toString() {
		return JacksonUtil.toStringPrettyPrinted(matrix);
	}

	@JsonCreator
	public static <T> MeveoMatrix<T> fromString(String serializedMatrix) {
		var matrixMap = JacksonUtil.fromString(serializedMatrix, new TypeReference<Map<String, Object>>() {});
		return new MeveoMatrix<T>(matrixMap);
	}
	
	@JsonIgnore
	public Map<String, Object> getInnerMap() {
		return this.matrix;
	}

}

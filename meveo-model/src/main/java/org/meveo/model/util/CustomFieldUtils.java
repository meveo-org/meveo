/**
 * 
 */
package org.meveo.model.util;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldMapKeyEnum;
import org.meveo.model.crm.custom.CustomFieldMatrixColumn;
import org.meveo.model.crm.custom.CustomFieldValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author heros
 * @since 
 * @version
 */
public class CustomFieldUtils {

	/**
	 * Match as close as possible map's key to the key provided and return a map value. Match is performed by matching a full string and then reducing one by one symbol untill a
	 * match is found.
	 * 
	 *
	 * @param value      Value to inspect
	 * @param keyToMatch Key to match
	 * @return Map value that closely matches map key
	 */
	@SuppressWarnings("unchecked")
	public static Object matchClosestValue(Object value, String keyToMatch) {
	    if (value == null || !(value instanceof Map) || StringUtils.isEmpty(keyToMatch)) {
	        return null;
	    }
	    Logger log = LoggerFactory.getLogger(CustomFieldUtils.class);
	    Object valueFound = null;
	    Map<String, Object> mapValue = (Map<String, Object>) value;
	    log.trace("matchClosestValue keyToMatch: {} in {}", keyToMatch, mapValue);
	    for (int i = keyToMatch.length(); i > 0; i--) {
	        valueFound = mapValue.get(keyToMatch.substring(0, i));
	        if (valueFound != null) {
	            log.trace("matchClosestValue found value: {} for key: {}", valueFound, keyToMatch.substring(0, i));
	            return valueFound;
	        }
	    }
	
	    return null;
	}

	/**
	 * Match for a given value map's key as the matrix value and return a map value.
	 * 
	 * Map key is assumed to be the following format. Note that MATRIX_STRING and MATRIX_RON keys can be mixed
	 * 
	 * &lt;matrix first key&gt;|&lt;matrix second key&gt;|&lt;range of numbers for the third key&gt;
	 * @param value Value to inspect
	 * @param keys  Keys to match. The order must correspond to the order of the keys during data entry
	 *
	 * @return A value matched
	 */
	@SuppressWarnings("unchecked")
	public static Object matchMatrixValue(Object value, Object... keys) {
	    if (value == null || !(value instanceof Map) || keys == null || keys.length == 0) {
	        return null;
	    }
	
	    Object valueMatched = null;
	
	    for (Entry<String, Object> valueInfo : ((Map<String, Object>) value).entrySet()) {
	        String[] keysParsed = valueInfo.getKey().split("\\" + CustomFieldValue.MATRIX_KEY_SEPARATOR);
	        if (keysParsed.length != keys.length) {
	            continue;
	        }
	
	        boolean allMatched = true;
	        for (int i = 0; i < keysParsed.length && allMatched; i++) {
	        	if (keysParsed[i].contains(CustomFieldValue.RON_VALUE_SEPARATOR)) {
	        		allMatched = isNumberRangeMatch(keysParsed[i], keys[i]);
	        	} else {
	        		allMatched = keysParsed[i].equals(keys[i]);
	        	}
	        }
	
	        if (allMatched) {
	            valueMatched = valueInfo.getValue();
	            break;
	        }
	    }
	
	    return valueMatched;
	}

	/**
	     * Check if a match for a given value map's key as the matrix value is present.
	     * 
	     * Map key is assumed to be the following format. Note that MATRIX_STRING and MATRIX_RON keys can be mixed
	     * 
	     * &lt;matrix first key&gt;|&lt;matrix second key&gt;|&lt;range of numbers for the third key&gt;
	     *
	     * @param cft   Custom field template
	     * @param value Value to inspect
	     * @param keys  Keys to match. The order must correspond to the order of the keys during data entry
	     * @return True if a value was matched
	     */
	    @SuppressWarnings("unchecked")
		public
	   static boolean isMatchMatrixValue(CustomFieldTemplate cft, Object value, Object... keys) {
	        if (value == null || !(value instanceof Map) || keys == null || keys.length == 0) {
	            return false;
	        }
	
	        for (Entry<String, Object> valueInfo : ((Map<String, Object>) value).entrySet()) {
	            String[] keysParsed = valueInfo.getKey().split("\\" + CustomFieldValue.MATRIX_KEY_SEPARATOR);
	            if (keysParsed.length != keys.length) {
	                continue;
	            }
	
	            boolean allMatched = true;
	            for (int i = 0; i < keysParsed.length; i++) {
	                CustomFieldMatrixColumn matrixColumn = cft.getMatrixColumnByIndex(i);
	                if (matrixColumn == null || (matrixColumn.getKeyType() == CustomFieldMapKeyEnum.STRING && !keysParsed[i].equals(keys[i]))
	                        || (matrixColumn.getKeyType() == CustomFieldMapKeyEnum.RON && !isNumberRangeMatch(keysParsed[i], keys[i]))) {
	                    allMatched = false;
	                    break;
	                }
	            }
	
	            if (allMatched) {
	                return true;
	            }
	        }
	
	        return false;
	    }

	/**
	 * Determine if a number value is inside the number range expressed as &lt;number from&gt;&lt;&lt;number to&gt;.
	 *
	 * @param numberRange      Number range value
	 * @param numberToMatchObj A double number o
	 * @return True if number have matched
	 */
	public static boolean isNumberRangeMatch(String numberRange, Object numberToMatchObj) {
	    if (numberToMatchObj == null) {
	        return false;
	    }
	
	    String[] rangeInfo = numberRange.split(CustomFieldValue.RON_VALUE_SEPARATOR);
	    Double fromNumber = null;
	    try {
	        fromNumber = Double.parseDouble(rangeInfo[0]);
	    } catch (NumberFormatException e) { // Ignore the error as value might be empty
	    }
	    Double toNumber = null;
	    if (rangeInfo.length == 2) {
	        try {
	            toNumber = Double.parseDouble(rangeInfo[1]);
	        } catch (NumberFormatException e) { // Ignore the error as value might be empty
	        }
	    }
	
	    // Convert matching number to Double for further comparison
	    Double numberToMatchDbl = null;
	    if (numberToMatchObj instanceof Double) {
	        numberToMatchDbl = (Double) numberToMatchObj;
	
	    } else if (numberToMatchObj instanceof Integer) {
	        numberToMatchDbl = ((Integer) numberToMatchObj).doubleValue();
	
	    } else if (numberToMatchObj instanceof Long) {
	        numberToMatchDbl = ((Long) numberToMatchObj).doubleValue();
	
	    } else if (numberToMatchObj instanceof BigDecimal) {
	        numberToMatchDbl = ((BigDecimal) numberToMatchObj).doubleValue();
	
	    } else if (numberToMatchObj instanceof String) {
	        try {
	            numberToMatchDbl = Double.parseDouble(((String) numberToMatchObj));
	
	        } catch (NumberFormatException e) {
	            Logger log = LoggerFactory.getLogger(CustomFieldUtils.class);
	            log.error("Failed to match CF value for a range of numbers. Value passed is not a number {} {}", numberToMatchObj,
	                    numberToMatchObj != null ? numberToMatchObj.getClass() : null);
	            return false;
	        }
	
	    } else {
	        Logger log = LoggerFactory.getLogger(CustomFieldUtils.class);
	        log.error("Failed to match CF value for a range of numbers. Value passed is not a number {} {}", numberToMatchObj,
	                numberToMatchObj != null ? numberToMatchObj.getClass() : null);
	        return false;
	    }
	
	    if (fromNumber != null && toNumber != null) {
	        if (fromNumber.compareTo(numberToMatchDbl) <= 0 && toNumber.compareTo(numberToMatchDbl) > 0) {
	            return true;
	        }
	    } else if (fromNumber != null) {
	        if (fromNumber.compareTo(numberToMatchDbl) <= 0) {
	            return true;
	        }
	    } else if (toNumber != null) {
	        if (toNumber.compareTo(numberToMatchDbl) > 0) {
	            return true;
	        }
	    }
	    return false;
	}

}

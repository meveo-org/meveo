package org.meveo.api.function;

import java.util.List;
import java.util.Map;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.5.0
 */
public interface IFunctionApi {

	List<Map<String, String>> getSampleInputs(Long functionId);

	List<Map<String, String>> getSampleInputs(String functionCode);

	List<Map<String, String>> getSampleOutputs(Long functionId);

	List<Map<String, String>> getSampleOutputs(String functionCode);
}

/**
 * 
 */
package org.meveo.jmeter.sampler.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;

/**
 * Assertion that fails when the response data matches the given regex or response code
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
public class WarningAssertion extends AbstractTestElement implements Serializable, Assertion {
	
	private static final long serialVersionUID = -3721798875719193357L;
	
	private static final String STATUS_CODE = "statusCode";
	private static final String REGEX = "regex";
	
	@Override
	public AssertionResult getResult(SampleResult response) {
		AssertionResult result = new AssertionResult(getName());
		result.setFailure(false);

		if(getStatusCode() != -1 && getStatusCode() == Integer.parseInt(response.getResponseCode())) {
			result.setFailure(true);
			result.setFailureMessage("[WARN] Status code matches warning assertion");
		}
		
		if(StringUtils.isNotBlank(getRegex()) && response.getResponseDataAsString().matches(getRegex())) {
			result.setFailure(true);
			result.setFailureMessage("[WARN] Response data matches warning assertion");
		}
		
		return result;
	}
	
	/**
	 * @return the status code to compare
	 */
	public int getStatusCode() {
		return getPropertyAsInt(STATUS_CODE, -1);
	}
	
	/**
	 * 
	 * @param code the status code to compare
	 */
	public void setStatusCode(int code) {
		setProperty(STATUS_CODE, code);
	}
	
	/**
	 * 
	 * @param regex the regex to test
	 */
	public void setRegex(String regex) {
		setProperty(REGEX, regex);
	}
	
	/**
	 * 
	 * @return the regex to test
	 */
	public String getRegex() {
		return getPropertyAsString(REGEX);
	}

}

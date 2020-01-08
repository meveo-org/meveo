package org.meveo.service.script.test;

import java.util.List;

public class TestResult {
	
	private String responsData;
	private List<SampleResult> sampleResults;
	
	public TestResult(String responsData, List<SampleResult> sampleResults) {
		super();
		this.responsData = responsData;
		this.sampleResults = sampleResults;
	}
	
	public String getResponsData() {
		return responsData;
	}
	
	public List<SampleResult> getSampleResults() {
		return sampleResults;
	}
	
}

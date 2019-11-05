package org.meveo.model.scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.5.0
 */
@ExportIdentifier({ "code" })
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "meveo_function", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
		@Parameter(name = "sequence_name", value = "meveo_function_seq") })
public class Function extends BusinessEntity {

	private static final long serialVersionUID = -1615762108685208441L;
	
	public static final String UNKNOWN = "Unknown";

	@Column(name = "function_version")
	private Integer functionVersion = 1;

	@Column(name = "test_suite", columnDefinition = "TEXT")
	@Type(type = "json")
	private String testSuite;

	@Column(name = "sample_inputs", columnDefinition = "TEXT")
	@Type(type = "jsonList")
	private List<Map<String, Object>> sampleInputs;

	@Column(name = "sample_outputs", columnDefinition = "TEXT")
	@Type(type = "jsonList")
	private List<Map<String, Object>> sampleOutputs;
	
	@Type(type="numeric_boolean")
    @Column(name = "generate_outputs")
    private Boolean generateOutputs = false;

	public Integer getFunctionVersion() {
		return functionVersion;
	}

	public void setFunctionVersion(Integer functionVersion) {
		this.functionVersion = functionVersion;
	}

	public List<FunctionIO> getInputs() {
		return new ArrayList<>();
	}

	public boolean hasInputs() {
		return false;
	}

	public List<FunctionIO> getOutputs() {
		return new ArrayList<>();
	}

	public boolean hasOutputs() {
		return false;
	}

	public String getTestSuite() {
		return testSuite;
	}

	public void setTestSuite(String testSuite) {
		this.testSuite = testSuite;
	}

	public String getFunctionType() {
		return UNKNOWN;
	}

	public List<Map<String, Object>> getSampleInputs() {
		return sampleInputs;
	}

	public void setSampleInputs(List<Map<String, Object>> sampleInputs) {
		this.sampleInputs = sampleInputs;
	}

	public List<Map<String, Object>> getSampleOutputs() {
		return sampleOutputs;
	}

	public void setSampleOutputs(List<Map<String, Object>> sampleOutputs) {
		this.sampleOutputs = sampleOutputs;
	}

	public Boolean getGenerateOutputs() {
		return generateOutputs == null ? false : generateOutputs;
	}

	public void setGenerateOutputs(Boolean generateOutputs) {
		this.generateOutputs = generateOutputs;
	}
}

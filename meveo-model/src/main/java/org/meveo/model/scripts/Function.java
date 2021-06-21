package org.meveo.model.scripts;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.persistence.JsonTypes;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @version 6.11
 */
@ExportIdentifier({ "code" })
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "meveo_function", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
		@Parameter(name = "sequence_name", value = "meveo_function_seq") })
public class Function extends BusinessEntity {

	private static final long serialVersionUID = -1615762108685208441L;
	
	/**
	* Default function type
	*/
	public static final String UNKNOWN = "Unknown";
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "category_id")
	private FunctionCategory category;

	@Column(name = "function_version")
	private Integer functionVersion = 1;

	@Column(name = "test_suite", columnDefinition = "TEXT")
	@Type(type = JsonTypes.JSON)
	private String testSuite;

	@Column(name = "samples", columnDefinition = "TEXT")
	@Type(type = JsonTypes.JSON_LIST)
	private List<Sample> samples;
	
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

		if(this.testSuite != null) {
			// Make sure the good functionCode is set
			if(FunctionUtils.checkTestSuite(this.testSuite, code) != null) {
				this.testSuite = FunctionUtils.replaceWithCorrectCode(this.testSuite, code);
			}
		}
	}

	public String getFunctionType() {
		return UNKNOWN;
	}

	public Boolean getGenerateOutputs() {
		return generateOutputs == null ? false : generateOutputs;
	}

	public void setGenerateOutputs(Boolean generateOutputs) {
		this.generateOutputs = generateOutputs;
	}

	public List<Sample> getSamples() {
		return samples;
	}

	public void setSamples(List<Sample> samples) {
		this.samples = samples;
	}

	public FunctionCategory getCategory() {
		return category;
	}

	public void setCategory(FunctionCategory category) {
		this.category = category;
	}

}

package org.meveo.model.crm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.model.BaseEntity;
import org.meveo.model.scripts.Function;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.5.0
 */
@Entity
@Table(name = "sample_io")
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
		@Parameter(name = "sequence_name", value = "sample_io_seq") })
public abstract class SampleIO extends BaseEntity {

	private static final long serialVersionUID = 2633395926129056699L;

	@ManyToOne
	@JoinColumn(name = "function_id")
	private Function function;

	@Column(name = "name", length = 255)
	@Size(max = 255, min = 1)
	@NotNull
	private String name;

	@Column(name = "value", length = 255)
	@Size(max = 255)
	private String value;

	@Enumerated(EnumType.STRING)
	@Column(name = "sampleio_type")
	private SampleIOEnum sampleIoType;

	public Function getFunction() {
		return function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public SampleIOEnum getSampleIoType() {
		return sampleIoType;
	}

	public void setSampleIoType(SampleIOEnum sampleIoType) {
		this.sampleIoType = sampleIoType;
	}
}

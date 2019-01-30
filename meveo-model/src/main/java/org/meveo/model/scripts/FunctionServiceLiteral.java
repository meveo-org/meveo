package org.meveo.model.scripts;

import javax.enterprise.util.AnnotationLiteral;

public class FunctionServiceLiteral extends AnnotationLiteral<FunctionServiceFor> implements FunctionServiceFor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String value;
	
	public FunctionServiceLiteral(String value) {
		super();
		this.value = value;
	}

	@Override
	public String value() {
		return this.value;
	}

}

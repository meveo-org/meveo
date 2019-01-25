package org.meveo.model.scripts;

import javax.enterprise.util.AnnotationLiteral;

public class FunctionServiceLiteral extends AnnotationLiteral<FunctionServiceFor> implements FunctionServiceFor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Class<? extends Function> value;
	
	public FunctionServiceLiteral(Class<? extends Function> value) {
		super();
		this.value = value;
	}

	@Override
	public Class<? extends Function> value() {
		return this.value;
	}

}

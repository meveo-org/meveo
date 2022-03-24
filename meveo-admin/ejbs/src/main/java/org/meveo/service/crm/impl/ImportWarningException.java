package org.meveo.service.crm.impl;

import org.meveo.admin.exception.BusinessException;

public class ImportWarningException extends BusinessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8209155644150744827L;
    
	public ImportWarningException(String message) {
        super(message);
    }

}

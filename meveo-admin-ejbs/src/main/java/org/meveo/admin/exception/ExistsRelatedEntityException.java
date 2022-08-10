package org.meveo.admin.exception;

public class ExistsRelatedEntityException extends BusinessException {

    private static final long serialVersionUID = -6868672182563855750L;

    public ExistsRelatedEntityException(String message) {
        super(message);
    }

    public ExistsRelatedEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExistsRelatedEntityException() {
    }
}
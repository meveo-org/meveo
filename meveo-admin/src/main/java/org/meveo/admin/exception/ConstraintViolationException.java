package org.meveo.admin.exception;

public class ConstraintViolationException extends BusinessException {

    private static final long serialVersionUID = -6868672182563855750L;

    public ConstraintViolationException(String message) {
        super(message);
    }

    public ConstraintViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConstraintViolationException() {
    }
}
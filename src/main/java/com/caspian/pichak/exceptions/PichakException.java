package com.caspian.pichak.exceptions;

public class PichakException extends Exception {
    private String errorCode;

    public PichakException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(final String errorCode) {
        this.errorCode = errorCode;
    }
}

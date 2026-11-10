package com.ocdlive.ocd.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final String messageKey;
    private final Object[] args;

    protected BaseApiException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.messageKey = null;
        this.args = new Object[0];
    }

    protected BaseApiException(String message, HttpStatus status, String errorCode, String messageKey, Object... args) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.args = args != null ? args : new Object[0];
    }

    protected BaseApiException(String message, Throwable cause, HttpStatus status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
        this.messageKey = null;
        this.args = new Object[0];
    }
}

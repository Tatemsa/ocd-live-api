package com.ocdlive.ocd.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseApiException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }

    public BadRequestException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }

    public BadRequestException(String message, String errorCode, String messageKey, Object... args) {
        super(message, HttpStatus.BAD_REQUEST, errorCode, messageKey, args);
    }
}

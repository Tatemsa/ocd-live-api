package com.ocdlive.ocd.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseApiException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}


package com.ocdlive.ocd.exception;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends BaseApiException {

    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("Un(e) %s avec %s '%s' existe déjà.", resourceName, fieldName, fieldValue),
                HttpStatus.CONFLICT,
                "RESOURCE_ALREADY_EXISTS",
                "error.resource.already_exists",
                resourceName, fieldName, fieldValue
        );
    }

    public ResourceAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, "RESOURCE_ALREADY_EXISTS");
    }
}

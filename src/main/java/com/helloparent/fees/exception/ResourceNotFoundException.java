package com.helloparent.fees.exception;

import org.springframework.http.HttpStatus;

/** 404 – e.g. "Student not found with id 9". */
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resource, Object id) {
        super(HttpStatus.NOT_FOUND, resource.toUpperCase().replace(' ', '_') + "_NOT_FOUND",
                resource + " not found with id " + id);
    }
}

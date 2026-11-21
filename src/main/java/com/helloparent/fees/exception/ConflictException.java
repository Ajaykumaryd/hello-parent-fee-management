package com.helloparent.fees.exception;

import org.springframework.http.HttpStatus;

/** 409 – duplicate data or state that clashes with the request (e.g. fee already paid). */
public class ConflictException extends ApiException {
    public ConflictException(String code, String message) {
        super(HttpStatus.CONFLICT, code, message);
    }
}

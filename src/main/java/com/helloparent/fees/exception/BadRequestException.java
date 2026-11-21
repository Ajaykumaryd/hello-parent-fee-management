package com.helloparent.fees.exception;

import org.springframework.http.HttpStatus;

/** 400 – invalid input that bean validation can't express (e.g. academic year 2025-30). */
public class BadRequestException extends ApiException {
    public BadRequestException(String code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }
}

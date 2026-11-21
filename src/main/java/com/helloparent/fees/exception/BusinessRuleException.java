package com.helloparent.fees.exception;

import org.springframework.http.HttpStatus;

/** 422 – request is well-formed but breaks a business rule (e.g. overpayment). */
public class BusinessRuleException extends ApiException {
    public BusinessRuleException(String code, String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, code, message);
    }
}

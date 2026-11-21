package com.helloparent.fees.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/** Single error shape for every failure. fieldErrors only present for validation errors. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(Instant timestamp, int status, String error, String code,
                            String message, String path, Map<String, String> fieldErrors) { }

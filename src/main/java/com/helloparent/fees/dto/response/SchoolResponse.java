package com.helloparent.fees.dto.response;

import com.helloparent.fees.entity.School;

public record SchoolResponse(Long id, String name, String code) {
    public static SchoolResponse from(School s) {
        return new SchoolResponse(s.getId(), s.getName(), s.getCode());
    }
}

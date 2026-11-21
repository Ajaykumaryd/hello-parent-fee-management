package com.helloparent.fees.entity;

import jakarta.persistence.*;

/** A school (tenant). Every student and fee belongs to exactly one school. */
@Entity
@Table(name = "schools", uniqueConstraints = @UniqueConstraint(name = "uk_school_code", columnNames = "code"))
public class School extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    /** Short unique business code, e.g. "DPS-GGN". Stored upper-case. */
    @Column(nullable = false, length = 30)
    private String code;

    protected School() { }

    public School(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() { return name; }
    public String getCode() { return code; }
}

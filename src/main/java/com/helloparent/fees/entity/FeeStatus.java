package com.helloparent.fees.entity;

/** Derived from paidAmount vs totalAmount — never set by clients. */
public enum FeeStatus {
    PENDING,  // nothing paid
    PARTIAL,  // some paid
    PAID      // fully paid (no more payments accepted)
}

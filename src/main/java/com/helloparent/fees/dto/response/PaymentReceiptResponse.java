package com.helloparent.fees.dto.response;

/** Returned after recording a payment: the payment + the fee's updated balance/status. */
public record PaymentReceiptResponse(PaymentResponse payment, FeeRecordResponse fee) { }

package com.helloparent.fees.dto.response;

import com.helloparent.fees.entity.Payment;
import com.helloparent.fees.entity.PaymentMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PaymentResponse(Long id, Long feeRecordId, BigDecimal amount, LocalDate paymentDate,
                              PaymentMode mode, String transactionReference, String remarks, Instant recordedAt) {

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getFeeRecord().getId(), p.getAmount(), p.getPaymentDate(),
                p.getMode(), p.getTransactionReference(), p.getRemarks(), p.getCreatedAt());
    }
}

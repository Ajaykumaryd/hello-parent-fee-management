package com.helloparent.fees.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** An immutable payment made against a fee record. */
@Entity
@Table(name = "payments",
        uniqueConstraints = @UniqueConstraint(name = "uk_payment_reference", columnNames = "transaction_reference"),
        indexes = @Index(name = "idx_payment_fee", columnList = "fee_record_id"))
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_record_id", nullable = false)
    private FeeRecord feeRecord;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMode mode;

    /** Gateway / receipt / cheque number. Unique -> blocks double-recording the same payment. */
    @Column(name = "transaction_reference", length = 64)
    private String transactionReference;

    @Column(length = 255)
    private String remarks;

    protected Payment() { }

    public Payment(FeeRecord feeRecord, BigDecimal amount, LocalDate paymentDate, PaymentMode mode,
                   String transactionReference, String remarks) {
        this.feeRecord = feeRecord;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.mode = mode;
        this.transactionReference = transactionReference;
        this.remarks = remarks;
    }

    public FeeRecord getFeeRecord() { return feeRecord; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public PaymentMode getMode() { return mode; }
    public String getTransactionReference() { return transactionReference; }
    public String getRemarks() { return remarks; }
}

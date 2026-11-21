package com.helloparent.fees.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One fee a student owes, e.g. "TUITION for 2025-26".
 * paidAmount is a running total kept in sync with payments, so lists/summaries
 * never need to re-sum the payments table.
 */
@Entity
@Table(name = "fee_records",
        uniqueConstraints = @UniqueConstraint(name = "uk_fee_student_year_type",
                columnNames = {"student_id", "academic_year", "fee_type"}),
        indexes = {
                @Index(name = "idx_fee_school_status", columnList = "school_id, status"),
                @Index(name = "idx_fee_student", columnList = "student_id")
        })
public class FeeRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /** Denormalised from student so school-level queries skip a join and use the index. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    /** Format "YYYY-YY", e.g. "2025-26". */
    @Column(name = "academic_year", nullable = false, length = 7)
    private String academicYear;

    /** e.g. TUITION, TRANSPORT, EXAM. Stored upper-case. */
    @Column(name = "fee_type", nullable = false, length = 40)
    private String feeType;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FeeStatus status = FeeStatus.PENDING;

    /** Optimistic lock: second safety net on top of the pessimistic row lock used for payments. */
    @Version
    private Long version;

    protected FeeRecord() { }

    public FeeRecord(Student student, String academicYear, String feeType, String description,
                     BigDecimal totalAmount, LocalDate dueDate) {
        this.student = student;
        this.school = student.getSchool();
        this.academicYear = academicYear;
        this.feeType = feeType;
        this.description = description;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
    }

    /** Amount still to be paid (never negative). */
    public BigDecimal getOutstandingAmount() {
        return totalAmount.subtract(paidAmount).max(BigDecimal.ZERO);
    }

    /** Overdue = not fully paid and due date already passed. */
    public boolean isOverdue(LocalDate today) {
        return status != FeeStatus.PAID && dueDate.isBefore(today);
    }

    /**
     * Applies a payment and re-derives status. Callers must validate the amount first
     * (service layer does it); this guard keeps the entity consistent regardless.
     */
    public void applyPayment(BigDecimal amount) {
        if (amount.compareTo(getOutstandingAmount()) > 0) {
            throw new IllegalStateException("Payment exceeds outstanding amount");
        }
        paidAmount = paidAmount.add(amount);
        status = paidAmount.compareTo(totalAmount) >= 0 ? FeeStatus.PAID : FeeStatus.PARTIAL;
    }

    public Student getStudent() { return student; }
    public School getSchool() { return school; }
    public String getAcademicYear() { return academicYear; }
    public String getFeeType() { return feeType; }
    public String getDescription() { return description; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public LocalDate getDueDate() { return dueDate; }
    public FeeStatus getStatus() { return status; }
}

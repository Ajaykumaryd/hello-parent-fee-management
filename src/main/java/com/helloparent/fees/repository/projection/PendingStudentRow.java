package com.helloparent.fees.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One row per student with unpaid fees (aggregated in the DB, not in Java). */
public record PendingStudentRow(
        Long studentId,
        String studentName,
        String admissionNumber,
        String grade,
        Long schoolId,
        String schoolName,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount,
        Long pendingFeeCount,
        LocalDate earliestDueDate) { }

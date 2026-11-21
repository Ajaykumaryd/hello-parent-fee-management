package com.helloparent.fees.dto.response;

import com.helloparent.fees.repository.projection.PendingStudentRow;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PendingStudentResponse(Long studentId, String studentName, String admissionNumber, String grade,
                                     Long schoolId, String schoolName,
                                     BigDecimal totalAmount, BigDecimal paidAmount, BigDecimal outstandingAmount,
                                     long pendingFeeCount, LocalDate earliestDueDate, boolean overdue) {

    public static PendingStudentResponse from(PendingStudentRow r, LocalDate today) {
        return new PendingStudentResponse(r.studentId(), r.studentName(), r.admissionNumber(), r.grade(),
                r.schoolId(), r.schoolName(), r.totalAmount(), r.paidAmount(), r.outstandingAmount(),
                r.pendingFeeCount(), r.earliestDueDate(),
                r.earliestDueDate() != null && r.earliestDueDate().isBefore(today));
    }
}

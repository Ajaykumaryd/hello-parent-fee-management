package com.helloparent.fees.dto.response;

import com.helloparent.fees.entity.FeeRecord;
import com.helloparent.fees.entity.FeeStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeRecordResponse(Long id, Long studentId, String studentName, Long schoolId,
                                String academicYear, String feeType, String description,
                                BigDecimal totalAmount, BigDecimal paidAmount, BigDecimal outstandingAmount,
                                LocalDate dueDate, FeeStatus status, boolean overdue) {

    public static FeeRecordResponse from(FeeRecord f, LocalDate today) {
        return new FeeRecordResponse(f.getId(), f.getStudent().getId(), f.getStudent().getName(),
                f.getSchool().getId(), f.getAcademicYear(), f.getFeeType(), f.getDescription(),
                f.getTotalAmount(), f.getPaidAmount(), f.getOutstandingAmount(),
                f.getDueDate(), f.getStatus(), f.isOverdue(today));
    }
}

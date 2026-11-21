package com.helloparent.fees.dto.response;

import java.math.BigDecimal;

/** School-level collection snapshot. collectionRatePercent = collected / total * 100. */
public record FeeSummaryResponse(Long schoolId, String schoolName, String academicYear,
                                 long totalFeeRecords,
                                 BigDecimal totalFeeAmount,
                                 BigDecimal totalCollected,
                                 BigDecimal totalOutstanding,
                                 BigDecimal overdueAmount,
                                 BigDecimal collectionRatePercent,
                                 long paidCount, long partialCount, long pendingCount,
                                 long studentsWithPendingFees) { }

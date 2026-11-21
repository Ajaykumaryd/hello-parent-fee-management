package com.helloparent.fees.repository.projection;

/**
 * Raw school-level totals produced by a single aggregate query.
 * Typed as Number because SUM(CASE ...) result types vary (bigint / numeric);
 * the service converts them to BigDecimal / long.
 */
public record FeeSummaryRow(
        Number totalFeeRecords,
        Number totalFeeAmount,
        Number totalCollected,
        Number overdueAmount,
        Number paidCount,
        Number partialCount,
        Number pendingCount) { }

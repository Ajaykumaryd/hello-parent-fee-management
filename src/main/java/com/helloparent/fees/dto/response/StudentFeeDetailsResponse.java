package com.helloparent.fees.dto.response;

import java.math.BigDecimal;
import java.util.List;

/** Student + all their fee records + totals across those records. */
public record StudentFeeDetailsResponse(StudentResponse student,
                                        BigDecimal totalAmount,
                                        BigDecimal totalPaid,
                                        BigDecimal totalOutstanding,
                                        List<FeeRecordResponse> fees) { }

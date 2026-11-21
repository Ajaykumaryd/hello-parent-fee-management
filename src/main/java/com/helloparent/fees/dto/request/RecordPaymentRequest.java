package com.helloparent.fees.dto.request;

import com.helloparent.fees.entity.PaymentMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordPaymentRequest(
        @Schema(example = "20000.00", description = "Must be > 0 and <= outstanding amount")
        @NotNull @DecimalMin(value = "0.01", message = "must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "max 10 digits and 2 decimals")
        BigDecimal amount,

        @Schema(example = "UPI") @NotNull PaymentMode mode,

        @Schema(example = "2026-01-15", description = "Defaults to today. Cannot be in the future")
        @PastOrPresent LocalDate paymentDate,

        @Schema(example = "UPI-TXN-889911", description = "Optional, globally unique. Duplicate -> 409 (prevents double entry)")
        @Size(max = 64) String transactionReference,

        @Schema(example = "First installment") @Size(max = 255) String remarks) { }

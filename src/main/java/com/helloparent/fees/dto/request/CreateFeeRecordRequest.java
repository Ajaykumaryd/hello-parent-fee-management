package com.helloparent.fees.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateFeeRecordRequest(
        @Schema(example = "1") @NotNull @Positive Long studentId,

        @Schema(example = "2025-26", description = "YYYY-YY, second part must be the next year")
        @NotBlank @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "must be in format YYYY-YY, e.g. 2025-26")
        String academicYear,

        @Schema(example = "TUITION", description = "Stored upper-case. One record per student + year + type")
        @NotBlank @Size(max = 40) @Pattern(regexp = "^[A-Za-z_ ]+$", message = "letters, spaces and '_' only")
        String feeType,

        @Schema(example = "Annual tuition fee") @Size(max = 255) String description,

        @Schema(example = "50000.00")
        @NotNull @DecimalMin(value = "0.01", message = "must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "max 10 digits and 2 decimals")
        BigDecimal totalAmount,

        @Schema(example = "2026-04-30") @NotNull LocalDate dueDate) { }

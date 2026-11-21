package com.helloparent.fees.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateStudentRequest(
        @Schema(example = "1") @NotNull @Positive Long schoolId,
        @Schema(example = "Aarav Sharma") @NotBlank @Size(max = 120) String name,
        @Schema(example = "ADM-2025-001", description = "Unique within the school")
        @NotBlank @Size(max = 40) String admissionNumber,
        @Schema(example = "5-A") @NotBlank @Size(max = 20) String grade) { }

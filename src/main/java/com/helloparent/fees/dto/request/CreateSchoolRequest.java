package com.helloparent.fees.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateSchoolRequest(
        @Schema(example = "Delhi Public School, Gurgaon")
        @NotBlank @Size(max = 150) String name,

        @Schema(example = "DPS-GGN", description = "Unique code; letters, digits, '-' only")
        @NotBlank @Size(max = 30) @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "only letters, digits and '-' allowed")
        String code) { }

package com.helloparent.fees.controller;

import com.helloparent.fees.dto.request.CreateSchoolRequest;
import com.helloparent.fees.dto.response.ErrorResponse;
import com.helloparent.fees.dto.response.FeeSummaryResponse;
import com.helloparent.fees.dto.response.SchoolResponse;
import com.helloparent.fees.service.FeeService;
import com.helloparent.fees.service.SchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/schools")
@Tag(name = "Schools", description = "School setup + fee collection summary")
@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class SchoolController {

    private final SchoolService schoolService;
    private final FeeService feeService;

    public SchoolController(SchoolService schoolService, FeeService feeService) {
        this.schoolService = schoolService;
        this.feeService = feeService;
    }

    @PostMapping
    @Operation(summary = "Create a school")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "409", description = "School code already exists", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<SchoolResponse> create(@Valid @RequestBody CreateSchoolRequest request) {
        SchoolResponse body = schoolService.create(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(body.id()).toUri()).body(body);
    }

    @GetMapping("/{schoolId}")
    @Operation(summary = "Get a school")
    public SchoolResponse get(@PathVariable Long schoolId) {
        return schoolService.get(schoolId);
    }

    @GetMapping("/{schoolId}/fee-summary")
    @Operation(summary = "Fee collection summary for a school",
            description = "Total billed, collected, outstanding, overdue, collection %, status counts and "
                    + "number of students with dues. Optional academicYear filter.")
    public FeeSummaryResponse summary(@PathVariable Long schoolId,
                                      @Parameter(example = "2025-26") @RequestParam(required = false) String academicYear) {
        return feeService.getSchoolSummary(schoolId, academicYear);
    }
}

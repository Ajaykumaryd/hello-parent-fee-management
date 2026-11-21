package com.helloparent.fees.controller;

import com.helloparent.fees.dto.request.CreateFeeRecordRequest;
import com.helloparent.fees.dto.response.ErrorResponse;
import com.helloparent.fees.dto.response.FeeRecordResponse;
import com.helloparent.fees.dto.response.PageResponse;
import com.helloparent.fees.dto.response.PendingStudentResponse;
import com.helloparent.fees.service.FeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/fees")
@Tag(name = "Fees", description = "Fee records and pending-fee reporting")
@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class FeeController {

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    @PostMapping
    @Operation(summary = "Create a fee record for a student",
            description = "One record per student + academicYear + feeType. Status starts as PENDING.")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "409", description = "Same fee already exists", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<FeeRecordResponse> create(@Valid @RequestBody CreateFeeRecordRequest request) {
        FeeRecordResponse body = feeService.create(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(body.id()).toUri()).body(body);
    }

    @GetMapping("/{feeId}")
    @Operation(summary = "Get a fee record")
    public FeeRecordResponse get(@PathVariable Long feeId) {
        return feeService.get(feeId);
    }

    @GetMapping("/pending-students")
    @Operation(summary = "List students with pending fees (paginated)",
            description = "One row per student with unpaid/partially-paid fees, sorted by outstanding amount (highest first). "
                    + "Filters: schoolId, academicYear, overdueOnly.")
    public PageResponse<PendingStudentResponse> pendingStudents(
            @Parameter(example = "1") @RequestParam(required = false) Long schoolId,
            @Parameter(example = "2025-26") @RequestParam(required = false) String academicYear,
            @RequestParam(defaultValue = "false") boolean overdueOnly,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return feeService.getStudentsWithPendingFees(schoolId, academicYear, overdueOnly, page, size);
    }
}

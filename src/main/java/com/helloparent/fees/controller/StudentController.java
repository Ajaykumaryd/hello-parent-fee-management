package com.helloparent.fees.controller;

import com.helloparent.fees.dto.request.CreateStudentRequest;
import com.helloparent.fees.dto.response.ErrorResponse;
import com.helloparent.fees.dto.response.PageResponse;
import com.helloparent.fees.dto.response.StudentFeeDetailsResponse;
import com.helloparent.fees.dto.response.StudentResponse;
import com.helloparent.fees.entity.FeeStatus;
import com.helloparent.fees.service.FeeService;
import com.helloparent.fees.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/students")
@Tag(name = "Students", description = "Student setup + a student's fee details")
@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class StudentController {

    private final StudentService studentService;
    private final FeeService feeService;

    public StudentController(StudentService studentService, FeeService feeService) {
        this.studentService = studentService;
        this.feeService = feeService;
    }

    @PostMapping
    @Operation(summary = "Create a student in a school")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "409", description = "Admission number already used in this school", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody CreateStudentRequest request) {
        StudentResponse body = studentService.create(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(body.id()).toUri()).body(body);
    }

    @GetMapping
    @Operation(summary = "List students of a school (paginated, sorted by name)")
    public PageResponse<StudentResponse> list(@RequestParam Long schoolId,
                                              @RequestParam(defaultValue = "0") @Min(0) int page,
                                              @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return studentService.getStudentsBySchool(schoolId, page, size);
    }

    @GetMapping("/{studentId}")
    @Operation(summary = "Get a student")
    public StudentResponse get(@PathVariable Long studentId) {
        return studentService.get(studentId);
    }

    @GetMapping("/{studentId}/fees")
    @Operation(summary = "Fee details of a student",
            description = "All fee records of the student with paid/outstanding per fee and overall totals. "
                    + "Optional status filter: PENDING, PARTIAL, PAID.")
    public StudentFeeDetailsResponse fees(@PathVariable Long studentId,
                                          @RequestParam(required = false) FeeStatus status) {
        return feeService.getStudentFees(studentId, status);
    }
}

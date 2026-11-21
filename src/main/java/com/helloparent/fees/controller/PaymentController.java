package com.helloparent.fees.controller;

import com.helloparent.fees.dto.request.RecordPaymentRequest;
import com.helloparent.fees.dto.response.ErrorResponse;
import com.helloparent.fees.dto.response.PageResponse;
import com.helloparent.fees.dto.response.PaymentReceiptResponse;
import com.helloparent.fees.dto.response.PaymentResponse;
import com.helloparent.fees.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fees/{feeId}/payments")
@Tag(name = "Payments", description = "Record payments and view payment history of a fee")
@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "404", description = "Fee record not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record a payment against a fee",
            description = "Partial payments allowed. Returns the payment and the fee's updated balance/status.")
    @ApiResponse(responseCode = "201", description = "Payment recorded")
    @ApiResponse(responseCode = "409", description = "Fee already paid / duplicate transactionReference / concurrent update",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Amount exceeds outstanding",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PaymentReceiptResponse record(@PathVariable Long feeId, @Valid @RequestBody RecordPaymentRequest request) {
        return paymentService.recordPayment(feeId, request);
    }

    @GetMapping
    @Operation(summary = "Payment history of a fee (paginated, newest first)")
    public PageResponse<PaymentResponse> history(@PathVariable Long feeId,
                                                 @RequestParam(defaultValue = "0") @Min(0) int page,
                                                 @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return paymentService.getPaymentHistory(feeId, page, size);
    }
}

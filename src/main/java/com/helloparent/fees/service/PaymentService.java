package com.helloparent.fees.service;

import com.helloparent.fees.dto.request.RecordPaymentRequest;
import com.helloparent.fees.dto.response.FeeRecordResponse;
import com.helloparent.fees.dto.response.PageResponse;
import com.helloparent.fees.dto.response.PaymentReceiptResponse;
import com.helloparent.fees.dto.response.PaymentResponse;
import com.helloparent.fees.entity.FeeRecord;
import com.helloparent.fees.entity.FeeStatus;
import com.helloparent.fees.entity.Payment;
import com.helloparent.fees.exception.BusinessRuleException;
import com.helloparent.fees.exception.ConflictException;
import com.helloparent.fees.exception.ResourceNotFoundException;
import com.helloparent.fees.repository.FeeRecordRepository;
import com.helloparent.fees.repository.PaymentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

/** Records payments and serves payment history. */
@Service
public class PaymentService {

    private final FeeRecordRepository feeRepository;
    private final PaymentRepository paymentRepository;
    private final Clock clock;

    public PaymentService(FeeRecordRepository feeRepository, PaymentRepository paymentRepository, Clock clock) {
        this.feeRepository = feeRepository;
        this.paymentRepository = paymentRepository;
        this.clock = clock;
    }

    /**
     * Records a payment atomically: insert payment + update fee balance/status in ONE transaction.
     *
     * Edge cases handled:
     *  - fee not found                         -> 404
     *  - duplicate transactionReference        -> 409 (no double entry on client retries)
     *  - fee already fully paid                -> 409
     *  - amount > outstanding (overpayment)    -> 422 (message shows the outstanding amount)
     *  - two payments at the same time         -> row lock (SELECT FOR UPDATE) serialises them
     */
    @Transactional
    public PaymentReceiptResponse recordPayment(Long feeId, RecordPaymentRequest req) {
        String reference = trimToNull(req.transactionReference());
        if (reference != null && paymentRepository.existsByTransactionReference(reference)) {
            throw new ConflictException("DUPLICATE_PAYMENT", "Payment with reference " + reference + " is already recorded");
        }

        // Lock the fee row until commit so balances can't be read stale by a parallel request.
        FeeRecord fee = feeRepository.findByIdForUpdate(feeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee record", feeId));

        if (fee.getStatus() == FeeStatus.PAID) {
            throw new ConflictException("FEE_ALREADY_PAID", "Fee record " + feeId + " is already fully paid");
        }
        BigDecimal amount = req.amount().setScale(2, RoundingMode.UNNECESSARY);
        if (amount.compareTo(fee.getOutstandingAmount()) > 0) {
            throw new BusinessRuleException("PAYMENT_EXCEEDS_OUTSTANDING",
                    "Payment " + amount + " exceeds outstanding amount " + fee.getOutstandingAmount());
        }

        LocalDate today = LocalDate.now(clock);
        LocalDate paymentDate = req.paymentDate() != null ? req.paymentDate() : today;

        fee.applyPayment(amount); // updates paidAmount + status; flushed on commit (dirty checking)
        Payment payment = paymentRepository.save(
                new Payment(fee, amount, paymentDate, req.mode(), reference, trimToNull(req.remarks())));

        return new PaymentReceiptResponse(PaymentResponse.from(payment), FeeRecordResponse.from(fee, today));
    }

    /** Payment history of a fee, newest first. Unknown fee -> 404 (not an empty page). */
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getPaymentHistory(Long feeId, int page, int size) {
        if (!feeRepository.existsById(feeId)) {
            throw new ResourceNotFoundException("Fee record", feeId);
        }
        var sort = Sort.by(Sort.Order.desc("paymentDate"), Sort.Order.desc("id"));
        return PageResponse.of(paymentRepository.findByFeeRecordId(feeId, PageRequest.of(page, size, sort)),
                PaymentResponse::from);
    }

    private static String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}

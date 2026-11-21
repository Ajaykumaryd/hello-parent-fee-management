package com.helloparent.fees.service;

import com.helloparent.fees.TestData;
import com.helloparent.fees.dto.request.RecordPaymentRequest;
import com.helloparent.fees.dto.response.PaymentReceiptResponse;
import com.helloparent.fees.entity.FeeRecord;
import com.helloparent.fees.entity.FeeStatus;
import com.helloparent.fees.entity.Payment;
import com.helloparent.fees.entity.PaymentMode;
import com.helloparent.fees.exception.BusinessRuleException;
import com.helloparent.fees.exception.ConflictException;
import com.helloparent.fees.exception.ResourceNotFoundException;
import com.helloparent.fees.repository.FeeRecordRepository;
import com.helloparent.fees.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 1, 15);

    @Mock FeeRecordRepository feeRepository;
    @Mock PaymentRepository paymentRepository;

    PaymentService service;
    FeeRecord fee;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        service = new PaymentService(feeRepository, paymentRepository, clock);
        fee = TestData.fee(10L, TestData.student(1L, TestData.school(1L)), "50000.00", TODAY.plusDays(30));
    }

    private RecordPaymentRequest req(String amount, String ref) {
        return new RecordPaymentRequest(new BigDecimal(amount), PaymentMode.UPI, null, ref, null);
    }

    @Test
    void partialPayment_updatesBalanceAndStatus_andDefaultsDateToToday() {
        when(feeRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(fee));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentReceiptResponse receipt = service.recordPayment(10L, req("20000", "TXN-1"));

        assertThat(receipt.fee().paidAmount()).isEqualByComparingTo("20000");
        assertThat(receipt.fee().outstandingAmount()).isEqualByComparingTo("30000");
        assertThat(receipt.fee().status()).isEqualTo(FeeStatus.PARTIAL);
        assertThat(receipt.payment().paymentDate()).isEqualTo(TODAY);
    }

    @Test
    void exactRemainingAmount_marksFeePaid() {
        when(feeRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(fee));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        service.recordPayment(10L, req("50000.00", null));

        assertThat(fee.getStatus()).isEqualTo(FeeStatus.PAID);
        assertThat(fee.getOutstandingAmount()).isEqualByComparingTo("0");
    }

    @Test
    void overpayment_isRejected_andNothingSaved() {
        when(feeRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(fee));

        assertThatThrownBy(() -> service.recordPayment(10L, req("50000.01", null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("exceeds outstanding");
        verify(paymentRepository, never()).save(any());
        assertThat(fee.getPaidAmount()).isEqualByComparingTo("0");
    }

    @Test
    void paymentOnFullyPaidFee_isConflict() {
        fee.applyPayment(new BigDecimal("50000.00"));
        when(feeRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(fee));

        assertThatThrownBy(() -> service.recordPayment(10L, req("1", null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already fully paid");
    }

    @Test
    void duplicateTransactionReference_isConflict_beforeLockingFee() {
        when(paymentRepository.existsByTransactionReference("TXN-1")).thenReturn(true);

        assertThatThrownBy(() -> service.recordPayment(10L, req("100", "  TXN-1 ")))
                .isInstanceOf(ConflictException.class);
        verify(feeRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void unknownFee_isNotFound() {
        when(feeRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recordPayment(99L, req("100", null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void blankReference_isStoredAsNull_soItNeverCollides() {
        when(feeRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(fee));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        service.recordPayment(10L, req("100", "   "));

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getTransactionReference()).isNull();
        verify(paymentRepository, never()).existsByTransactionReference(any());
    }

    @Test
    void paymentHistory_unknownFee_isNotFound() {
        when(feeRepository.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> service.getPaymentHistory(5L, 0, 20))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

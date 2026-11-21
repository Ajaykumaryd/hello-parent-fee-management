package com.helloparent.fees.entity;

import com.helloparent.fees.TestData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pure domain rules of FeeRecord (no Spring). */
class FeeRecordTest {

    private final LocalDate today = LocalDate.of(2026, 1, 15);

    private FeeRecord fee(LocalDate due) {
        return TestData.fee(1L, TestData.student(1L, TestData.school(1L)), "1000.00", due);
    }

    @Test
    void statusMovesPendingToPartialToPaid() {
        FeeRecord f = fee(today);
        assertThat(f.getStatus()).isEqualTo(FeeStatus.PENDING);
        f.applyPayment(new BigDecimal("400"));
        assertThat(f.getStatus()).isEqualTo(FeeStatus.PARTIAL);
        f.applyPayment(new BigDecimal("600"));
        assertThat(f.getStatus()).isEqualTo(FeeStatus.PAID);
    }

    @Test
    void entityRefusesOverpaymentEvenIfServiceCheckIsBypassed() {
        FeeRecord f = fee(today);
        assertThatThrownBy(() -> f.applyPayment(new BigDecimal("1000.01"))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void overdueOnlyWhenUnpaidAndPastDue() {
        assertThat(fee(today.minusDays(1)).isOverdue(today)).isTrue();
        assertThat(fee(today).isOverdue(today)).isFalse();            // due today is not overdue
        FeeRecord paid = fee(today.minusDays(1));
        paid.applyPayment(new BigDecimal("1000"));
        assertThat(paid.isOverdue(today)).isFalse();
    }
}

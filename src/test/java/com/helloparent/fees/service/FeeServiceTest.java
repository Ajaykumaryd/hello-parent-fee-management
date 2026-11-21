package com.helloparent.fees.service;

import com.helloparent.fees.TestData;
import com.helloparent.fees.dto.request.CreateFeeRecordRequest;
import com.helloparent.fees.dto.response.FeeRecordResponse;
import com.helloparent.fees.dto.response.FeeSummaryResponse;
import com.helloparent.fees.dto.response.StudentFeeDetailsResponse;
import com.helloparent.fees.entity.FeeRecord;
import com.helloparent.fees.entity.FeeStatus;
import com.helloparent.fees.entity.School;
import com.helloparent.fees.entity.Student;
import com.helloparent.fees.exception.BadRequestException;
import com.helloparent.fees.exception.ConflictException;
import com.helloparent.fees.exception.ResourceNotFoundException;
import com.helloparent.fees.repository.FeeRecordRepository;
import com.helloparent.fees.repository.projection.FeeSummaryRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeeServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 1, 15);

    @Mock FeeRecordRepository feeRepository;
    @Mock StudentService studentService;
    @Mock SchoolService schoolService;

    FeeService service;
    School school;
    Student student;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        service = new FeeService(feeRepository, studentService, schoolService, clock);
        school = TestData.school(1L);
        student = TestData.student(7L, school);
    }

    private CreateFeeRecordRequest createReq(String year, String type) {
        return new CreateFeeRecordRequest(7L, year, type, " Annual ", new BigDecimal("50000"), TODAY.plusDays(30));
    }

    @Test
    void create_normalizesFeeType_andStartsPending() {
        when(studentService.getEntity(7L)).thenReturn(student);
        when(feeRepository.save(any(FeeRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        FeeRecordResponse res = service.create(createReq("2025-26", " exam fee "));

        assertThat(res.feeType()).isEqualTo("EXAM_FEE");
        assertThat(res.description()).isEqualTo("Annual");
        assertThat(res.status()).isEqualTo(FeeStatus.PENDING);
        assertThat(res.outstandingAmount()).isEqualByComparingTo("50000");
        assertThat(res.totalAmount().scale()).isEqualTo(2);
    }

    @Test
    void create_duplicateFee_isConflict() {
        when(studentService.getEntity(7L)).thenReturn(student);
        when(feeRepository.existsByStudentIdAndAcademicYearAndFeeType(7L, "2025-26", "TUITION")).thenReturn(true);

        assertThatThrownBy(() -> service.create(createReq("2025-26", "tuition")))
                .isInstanceOf(ConflictException.class);
        verify(feeRepository, never()).save(any());
    }

    @Test
    void create_nonConsecutiveAcademicYear_isBadRequest() {
        when(studentService.getEntity(7L)).thenReturn(student);

        assertThatThrownBy(() -> service.create(createReq("2025-27", "TUITION")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void create_unknownStudent_isNotFound() {
        when(studentService.getEntity(7L)).thenThrow(new ResourceNotFoundException("Student", 7L));

        assertThatThrownBy(() -> service.create(createReq("2025-26", "TUITION")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void studentFees_sumsTotals_andFlagsOverdue() {
        FeeRecord overdue = TestData.fee(1L, student, "50000", TODAY.minusDays(1));
        overdue.applyPayment(new BigDecimal("20000"));
        FeeRecord upcoming = TestData.fee(2L, student, "10000", TODAY.plusDays(5));
        when(studentService.getEntity(7L)).thenReturn(student);
        when(feeRepository.findByStudent(7L, null)).thenReturn(List.of(overdue, upcoming));

        StudentFeeDetailsResponse res = service.getStudentFees(7L, null);

        assertThat(res.totalAmount()).isEqualByComparingTo("60000");
        assertThat(res.totalPaid()).isEqualByComparingTo("20000");
        assertThat(res.totalOutstanding()).isEqualByComparingTo("40000");
        assertThat(res.fees().get(0).overdue()).isTrue();
        assertThat(res.fees().get(1).overdue()).isFalse();
    }

    @Test
    void summary_computesOutstandingAndRate() {
        when(schoolService.getEntity(1L)).thenReturn(school);
        when(feeRepository.summarizeSchool(1L, null, TODAY)).thenReturn(new FeeSummaryRow(
                3L, new BigDecimal("150000"), new BigDecimal("50000"), new BigDecimal("30000"), 1L, 1L, 1L));
        when(feeRepository.countStudentsWithPendingFees(1L, null)).thenReturn(2L);

        FeeSummaryResponse res = service.getSchoolSummary(1L, null);

        assertThat(res.totalOutstanding()).isEqualByComparingTo("100000");
        assertThat(res.collectionRatePercent()).isEqualByComparingTo("33.33");
        assertThat(res.studentsWithPendingFees()).isEqualTo(2);
    }

    @Test
    void summary_schoolWithNoFees_returnsZeros_notDivideByZero() {
        when(schoolService.getEntity(1L)).thenReturn(school);
        when(feeRepository.summarizeSchool(1L, null, TODAY)).thenReturn(new FeeSummaryRow(0L, null, null, null, 0, 0, 0));

        FeeSummaryResponse res = service.getSchoolSummary(1L, " ");

        assertThat(res.totalFeeAmount()).isEqualByComparingTo("0");
        assertThat(res.collectionRatePercent()).isEqualByComparingTo("0");
        assertThat(res.academicYear()).isNull();
    }

    @Test
    void pendingStudents_unknownSchool_isNotFound() {
        when(schoolService.getEntity(42L)).thenThrow(new ResourceNotFoundException("School", 42L));

        assertThatThrownBy(() -> service.getStudentsWithPendingFees(42L, null, false, 0, 20))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(feeRepository, never()).findStudentsWithPendingFees(any(), any(), anyBoolean(), any(), any());
    }
}

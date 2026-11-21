package com.helloparent.fees.service;

import com.helloparent.fees.dto.request.CreateFeeRecordRequest;
import com.helloparent.fees.dto.response.*;
import com.helloparent.fees.entity.FeeRecord;
import com.helloparent.fees.entity.FeeStatus;
import com.helloparent.fees.entity.School;
import com.helloparent.fees.entity.Student;
import com.helloparent.fees.exception.ConflictException;
import com.helloparent.fees.exception.ResourceNotFoundException;
import com.helloparent.fees.repository.FeeRecordRepository;
import com.helloparent.fees.repository.projection.FeeSummaryRow;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Fee records: create, read, per-student details, pending list and school summary. */
@Service
public class FeeService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final FeeRecordRepository feeRepository;
    private final StudentService studentService;
    private final SchoolService schoolService;
    private final Clock clock;

    public FeeService(FeeRecordRepository feeRepository, StudentService studentService,
                      SchoolService schoolService, Clock clock) {
        this.feeRepository = feeRepository;
        this.studentService = studentService;
        this.schoolService = schoolService;
        this.clock = clock;
    }

    /**
     * Creates a fee for a student.
     * Rules: student must exist (404), year must be valid (400),
     * one fee per student + year + type (409 — DB unique key also guards races).
     */
    @Transactional
    public FeeRecordResponse create(CreateFeeRecordRequest req) {
        Student student = studentService.getEntity(req.studentId());
        String year = AcademicYears.validateOptional(req.academicYear());
        String feeType = normalizeFeeType(req.feeType());

        if (feeRepository.existsByStudentIdAndAcademicYearAndFeeType(student.getId(), year, feeType)) {
            throw new ConflictException("DUPLICATE_FEE_RECORD",
                    feeType + " fee for " + year + " already exists for student " + student.getId());
        }
        FeeRecord fee = new FeeRecord(student, year, feeType, trimToNull(req.description()),
                req.totalAmount().setScale(2, RoundingMode.UNNECESSARY), req.dueDate());
        return FeeRecordResponse.from(feeRepository.save(fee), today());
    }

    @Transactional(readOnly = true)
    public FeeRecordResponse get(Long feeId) {
        FeeRecord fee = feeRepository.findWithStudentById(feeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee record", feeId));
        return FeeRecordResponse.from(fee, today());
    }

    /** All fees of one student (optionally by status) + combined totals. */
    @Transactional(readOnly = true)
    public StudentFeeDetailsResponse getStudentFees(Long studentId, FeeStatus status) {
        Student student = studentService.getEntity(studentId);
        LocalDate today = today();
        List<FeeRecord> fees = feeRepository.findByStudent(studentId, status);

        BigDecimal total = sum(fees, FeeRecord::getTotalAmount);
        BigDecimal paid = sum(fees, FeeRecord::getPaidAmount);
        return new StudentFeeDetailsResponse(StudentResponse.from(student), total, paid, total.subtract(paid),
                fees.stream().map(f -> FeeRecordResponse.from(f, today)).toList());
    }

    /**
     * Paginated list of students owing money, highest outstanding first.
     * schoolId / academicYear are optional filters; unknown school -> 404 (not a silent empty list).
     */
    @Transactional(readOnly = true)
    public PageResponse<PendingStudentResponse> getStudentsWithPendingFees(Long schoolId, String academicYear,
                                                                           boolean overdueOnly, int page, int size) {
        if (schoolId != null) {
            schoolService.getEntity(schoolId);
        }
        String year = AcademicYears.validateOptional(academicYear);
        LocalDate today = today();
        var result = feeRepository.findStudentsWithPendingFees(schoolId, year, overdueOnly, today,
                PageRequest.of(page, size));
        return PageResponse.of(result, row -> PendingStudentResponse.from(row, today));
    }

    /** Collection vs outstanding for a school (optionally one academic year). */
    @Transactional(readOnly = true)
    public FeeSummaryResponse getSchoolSummary(Long schoolId, String academicYear) {
        School school = schoolService.getEntity(schoolId);
        String year = AcademicYears.validateOptional(academicYear);
        FeeSummaryRow row = feeRepository.summarizeSchool(schoolId, year, today());
        long studentsPending = feeRepository.countStudentsWithPendingFees(schoolId, year);

        BigDecimal total = money(row.totalFeeAmount());
        BigDecimal collected = money(row.totalCollected());
        BigDecimal rate = total.signum() == 0 ? BigDecimal.ZERO.setScale(2)
                : collected.multiply(HUNDRED).divide(total, 2, RoundingMode.HALF_UP);

        return new FeeSummaryResponse(school.getId(), school.getName(), year,
                count(row.totalFeeRecords()), total, collected, total.subtract(collected), money(row.overdueAmount()),
                rate, count(row.paidCount()), count(row.partialCount()), count(row.pendingCount()), studentsPending);
    }

    // ---------- helpers ----------

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    /** " tuition fee " -> "TUITION_FEE" so the same type can't be stored twice in different spellings. */
    static String normalizeFeeType(String feeType) {
        return feeType.trim().replaceAll("\\s+", "_").toUpperCase();
    }

    private static String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    /** Null-safe, always 2 decimals (DB aggregates can come back null or with other scales). */
    private static BigDecimal money(Number v) {
        BigDecimal d = v == null ? BigDecimal.ZERO
                : v instanceof BigDecimal bd ? bd : new BigDecimal(v.toString());
        return d.setScale(2, RoundingMode.HALF_UP);
    }

    private static long count(Number v) {
        return v == null ? 0L : v.longValue();
    }

    private static BigDecimal sum(List<FeeRecord> fees, java.util.function.Function<FeeRecord, BigDecimal> f) {
        return fees.stream().map(f).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }
}

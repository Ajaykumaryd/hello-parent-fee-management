package com.helloparent.fees.repository;

import com.helloparent.fees.entity.FeeRecord;
import com.helloparent.fees.entity.FeeStatus;
import com.helloparent.fees.repository.projection.FeeSummaryRow;
import com.helloparent.fees.repository.projection.PendingStudentRow;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FeeRecordRepository extends JpaRepository<FeeRecord, Long> {

    boolean existsByStudentIdAndAcademicYearAndFeeType(Long studentId, String academicYear, String feeType);

    @EntityGraph(attributePaths = {"student", "school"})
    Optional<FeeRecord> findWithStudentById(Long id);

    /**
     * SELECT ... FOR UPDATE. Two concurrent payments on the same fee are serialised,
     * so paidAmount can never overshoot totalAmount.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FeeRecord f WHERE f.id = :id")
    Optional<FeeRecord> findByIdForUpdate(@Param("id") Long id);

    /** All fees of a student, optional status filter, newest due date first. */
    @Query("""
            SELECT f FROM FeeRecord f
            WHERE f.student.id = :studentId
              AND (:status IS NULL OR f.status = :status)
            ORDER BY f.dueDate DESC, f.id DESC""")
    List<FeeRecord> findByStudent(@Param("studentId") Long studentId, @Param("status") FeeStatus status);

    /**
     * Students with at least one unpaid fee, aggregated per student.
     * Ordered by highest outstanding first (most useful for follow-up), id as tie-breaker
     * so pages are stable. Separate countQuery = number of distinct students.
     */
    @Query(value = """
            SELECT new com.helloparent.fees.repository.projection.PendingStudentRow(
                s.id, s.name, s.admissionNumber, s.grade, sc.id, sc.name,
                SUM(f.totalAmount), SUM(f.paidAmount), SUM(f.totalAmount - f.paidAmount),
                COUNT(f), MIN(f.dueDate))
            FROM FeeRecord f JOIN f.student s JOIN f.school sc
            WHERE f.status <> com.helloparent.fees.entity.FeeStatus.PAID
              AND (:schoolId IS NULL OR sc.id = :schoolId)
              AND (:academicYear IS NULL OR f.academicYear = :academicYear)
              AND (:overdueOnly = false OR f.dueDate < :today)
            GROUP BY s.id, s.name, s.admissionNumber, s.grade, sc.id, sc.name
            ORDER BY SUM(f.totalAmount - f.paidAmount) DESC, s.id ASC""",
            countQuery = """
            SELECT COUNT(DISTINCT f.student.id) FROM FeeRecord f
            WHERE f.status <> com.helloparent.fees.entity.FeeStatus.PAID
              AND (:schoolId IS NULL OR f.school.id = :schoolId)
              AND (:academicYear IS NULL OR f.academicYear = :academicYear)
              AND (:overdueOnly = false OR f.dueDate < :today)""")
    Page<PendingStudentRow> findStudentsWithPendingFees(@Param("schoolId") Long schoolId,
                                                        @Param("academicYear") String academicYear,
                                                        @Param("overdueOnly") boolean overdueOnly,
                                                        @Param("today") LocalDate today,
                                                        Pageable pageable);

    /** All school totals in ONE query. COALESCE keeps zeros (not nulls) for a school with no fees. */
    @Query("""
            SELECT new com.helloparent.fees.repository.projection.FeeSummaryRow(
                COUNT(f),
                COALESCE(SUM(f.totalAmount), 0),
                COALESCE(SUM(f.paidAmount), 0),
                COALESCE(SUM(CASE WHEN f.status <> com.helloparent.fees.entity.FeeStatus.PAID
                                   AND f.dueDate < :today
                                  THEN f.totalAmount - f.paidAmount ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN f.status = com.helloparent.fees.entity.FeeStatus.PAID THEN 1 ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN f.status = com.helloparent.fees.entity.FeeStatus.PARTIAL THEN 1 ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN f.status = com.helloparent.fees.entity.FeeStatus.PENDING THEN 1 ELSE 0 END), 0))
            FROM FeeRecord f
            WHERE f.school.id = :schoolId
              AND (:academicYear IS NULL OR f.academicYear = :academicYear)""")
    FeeSummaryRow summarizeSchool(@Param("schoolId") Long schoolId,
                                  @Param("academicYear") String academicYear,
                                  @Param("today") LocalDate today);

    @Query("""
            SELECT COUNT(DISTINCT f.student.id) FROM FeeRecord f
            WHERE f.school.id = :schoolId
              AND f.status <> com.helloparent.fees.entity.FeeStatus.PAID
              AND (:academicYear IS NULL OR f.academicYear = :academicYear)""")
    long countStudentsWithPendingFees(@Param("schoolId") Long schoolId,
                                      @Param("academicYear") String academicYear);
}

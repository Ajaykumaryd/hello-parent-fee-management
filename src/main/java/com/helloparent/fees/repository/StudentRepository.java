package com.helloparent.fees.repository;

import com.helloparent.fees.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsBySchoolIdAndAdmissionNumber(Long schoolId, String admissionNumber);

    /** Loads school in the same query (avoids a lazy-load round trip). */
    @EntityGraph(attributePaths = "school")
    Optional<Student> findWithSchoolById(Long id);

    @EntityGraph(attributePaths = "school")
    Page<Student> findBySchoolId(Long schoolId, Pageable pageable);
}

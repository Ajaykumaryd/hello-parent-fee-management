package com.helloparent.fees.repository;

import com.helloparent.fees.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {
    boolean existsByCode(String code);
}

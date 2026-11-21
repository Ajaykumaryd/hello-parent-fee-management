package com.helloparent.fees.service;

import com.helloparent.fees.dto.request.CreateSchoolRequest;
import com.helloparent.fees.dto.response.SchoolResponse;
import com.helloparent.fees.entity.School;
import com.helloparent.fees.exception.ConflictException;
import com.helloparent.fees.exception.ResourceNotFoundException;
import com.helloparent.fees.repository.SchoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolService {

    private final SchoolRepository schoolRepository;

    public SchoolService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    /** Creates a school; code is case-insensitive unique ("dps-ggn" == "DPS-GGN"). */
    @Transactional
    public SchoolResponse create(CreateSchoolRequest req) {
        String code = req.code().trim().toUpperCase();
        if (schoolRepository.existsByCode(code)) {
            throw new ConflictException("DUPLICATE_SCHOOL_CODE", "School with code " + code + " already exists");
        }
        return SchoolResponse.from(schoolRepository.save(new School(req.name().trim(), code)));
    }

    @Transactional(readOnly = true)
    public SchoolResponse get(Long id) {
        return SchoolResponse.from(getEntity(id));
    }

    /** Shared lookup used by other services -> consistent 404 message. */
    @Transactional(readOnly = true)
    public School getEntity(Long id) {
        return schoolRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("School", id));
    }
}

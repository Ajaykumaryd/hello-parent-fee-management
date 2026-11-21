package com.helloparent.fees.service;

import com.helloparent.fees.dto.request.CreateStudentRequest;
import com.helloparent.fees.dto.response.PageResponse;
import com.helloparent.fees.dto.response.StudentResponse;
import com.helloparent.fees.entity.School;
import com.helloparent.fees.entity.Student;
import com.helloparent.fees.exception.ConflictException;
import com.helloparent.fees.exception.ResourceNotFoundException;
import com.helloparent.fees.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final SchoolService schoolService;

    public StudentService(StudentRepository studentRepository, SchoolService schoolService) {
        this.studentRepository = studentRepository;
        this.schoolService = schoolService;
    }

    /** School must exist (404); admission number must be unique inside that school (409). */
    @Transactional
    public StudentResponse create(CreateStudentRequest req) {
        School school = schoolService.getEntity(req.schoolId());
        String admissionNo = req.admissionNumber().trim().toUpperCase();
        if (studentRepository.existsBySchoolIdAndAdmissionNumber(school.getId(), admissionNo)) {
            throw new ConflictException("DUPLICATE_ADMISSION_NUMBER",
                    "Admission number " + admissionNo + " already exists in school " + school.getId());
        }
        Student saved = studentRepository.save(
                new Student(req.name().trim(), admissionNo, req.grade().trim().toUpperCase(), school));
        return StudentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public StudentResponse get(Long id) {
        return StudentResponse.from(getEntity(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> getStudentsBySchool(Long schoolId, int page, int size) {
        schoolService.getEntity(schoolId); // 404 if school doesn't exist
        Sort sort = Sort.by("name").ascending().and(Sort.by("id").ascending());
        Page<Student> students = studentRepository.findBySchoolId(schoolId, PageRequest.of(page, size, sort));
        return PageResponse.of(students, StudentResponse::from);
    }

    /** Student with school eagerly loaded. */
    @Transactional(readOnly = true)
    public Student getEntity(Long id) {
        return studentRepository.
                findWithSchoolById(id).orElseThrow(()
                        -> new ResourceNotFoundException("Student", id));
    }
}

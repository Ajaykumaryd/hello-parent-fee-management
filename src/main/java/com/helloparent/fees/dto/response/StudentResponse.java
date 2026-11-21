package com.helloparent.fees.dto.response;

import com.helloparent.fees.entity.Student;

public record StudentResponse(Long id, String name, String admissionNumber, String grade,
                              Long schoolId, String schoolName) {
    /** Caller must have school loaded (or be inside a transaction). */
    public static StudentResponse from(Student s) {
        return new StudentResponse(s.getId(), s.getName(), s.getAdmissionNumber(), s.getGrade(),
                s.getSchool().getId(), s.getSchool().getName());
    }
}

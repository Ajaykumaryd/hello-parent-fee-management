package com.helloparent.fees.service;

import com.helloparent.fees.TestData;
import com.helloparent.fees.dto.request.CreateStudentRequest;
import com.helloparent.fees.dto.response.StudentResponse;
import com.helloparent.fees.entity.School;
import com.helloparent.fees.entity.Student;
import com.helloparent.fees.exception.ConflictException;
import com.helloparent.fees.exception.ResourceNotFoundException;
import com.helloparent.fees.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock StudentRepository studentRepository;
    @Mock SchoolService schoolService;

    StudentService service;
    School school;

    @BeforeEach
    void setUp() {
        service = new StudentService(studentRepository, schoolService);
        school = TestData.school(1L);
    }

    @Test
    void create_normalizesFields_andLinksSchool() {
        when(schoolService.getEntity(1L)).thenReturn(school);
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentResponse res = service.create(new CreateStudentRequest(1L, " Riya ", " adm-7 ", " 5-a "));

        assertThat(res.name()).isEqualTo("Riya");
        assertThat(res.admissionNumber()).isEqualTo("ADM-7");
        assertThat(res.grade()).isEqualTo("5-A");
        assertThat(res.schoolId()).isEqualTo(1L);
    }

    @Test
    void create_unknownSchool_isNotFound() {
        when(schoolService.getEntity(5L)).thenThrow(new ResourceNotFoundException("School", 5L));

        assertThatThrownBy(() -> service.create(new CreateStudentRequest(5L, "A", "X", "1")))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(studentRepository, never()).save(any());
    }

    @Test
    void create_duplicateAdmissionNumberInSameSchool_isConflict() {
        when(schoolService.getEntity(1L)).thenReturn(school);
        when(studentRepository.existsBySchoolIdAndAdmissionNumber(1L, "ADM-7")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateStudentRequest(1L, "A", "adm-7", "1")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void get_unknownStudent_isNotFound() {
        when(studentRepository.findWithSchoolById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(3L)).isInstanceOf(ResourceNotFoundException.class);
    }
}

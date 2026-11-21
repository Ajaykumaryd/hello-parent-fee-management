package com.helloparent.fees.service;

import com.helloparent.fees.TestData;
import com.helloparent.fees.dto.request.CreateSchoolRequest;
import com.helloparent.fees.dto.response.SchoolResponse;
import com.helloparent.fees.entity.School;
import com.helloparent.fees.exception.ConflictException;
import com.helloparent.fees.exception.ResourceNotFoundException;
import com.helloparent.fees.repository.SchoolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolServiceTest {

    @Mock SchoolRepository schoolRepository;
    @InjectMocks SchoolService service;

    @Test
    void create_trimsName_andUppercasesCode() {
        when(schoolRepository.save(any(School.class))).thenAnswer(inv -> inv.getArgument(0));

        SchoolResponse res = service.create(new CreateSchoolRequest("  Green Valley  ", " gv-01 "));

        assertThat(res.name()).isEqualTo("Green Valley");
        assertThat(res.code()).isEqualTo("GV-01");
    }

    @Test
    void create_duplicateCode_isConflict_caseInsensitive() {
        when(schoolRepository.existsByCode("GV-01")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateSchoolRequest("X", "gv-01")))
                .isInstanceOf(ConflictException.class);
        verify(schoolRepository, never()).save(any());
    }

    @Test
    void get_unknownSchool_isNotFound() {
        when(schoolRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(9L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("School not found with id 9");
    }

    @Test
    void get_existingSchool_returnsIt() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(TestData.school(1L)));

        assertThat(service.get(1L).id()).isEqualTo(1L);
    }
}

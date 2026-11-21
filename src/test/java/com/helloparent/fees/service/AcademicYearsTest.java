package com.helloparent.fees.service;

import com.helloparent.fees.exception.BadRequestException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcademicYearsTest {

    @Test
    void validYears() {
        assertThat(AcademicYears.validateOptional(" 2025-26 ")).isEqualTo("2025-26");
        assertThat(AcademicYears.validateOptional("2099-00")).isEqualTo("2099-00"); // century rollover
        assertThat(AcademicYears.validateOptional(null)).isNull();
        assertThat(AcademicYears.validateOptional("")).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025-27", "2025", "25-26", "2025/26", "abcd-ef"})
    void invalidYears(String year) {
        assertThatThrownBy(() -> AcademicYears.validateOptional(year)).isInstanceOf(BadRequestException.class);
    }
}

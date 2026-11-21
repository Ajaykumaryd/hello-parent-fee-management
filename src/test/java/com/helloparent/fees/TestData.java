package com.helloparent.fees;

import com.helloparent.fees.entity.FeeRecord;
import com.helloparent.fees.entity.School;
import com.helloparent.fees.entity.Student;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Small builders for unit tests (ids set manually since there is no DB). */
public final class TestData {

    private TestData() { }

    public static School school(long id) {
        School s = new School("Test School", "TS-" + id);
        s.setId(id);
        return s;
    }

    public static Student student(long id, School school) {
        Student s = new Student("Student " + id, "ADM-" + id, "5-A", school);
        s.setId(id);
        return s;
    }

    public static FeeRecord fee(long id, Student student, String total, LocalDate due) {
        FeeRecord f = new FeeRecord(student, "2025-26", "TUITION", null, new BigDecimal(total), due);
        f.setId(id);
        return f;
    }
}

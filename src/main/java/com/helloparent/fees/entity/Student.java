package com.helloparent.fees.entity;

import jakarta.persistence.*;

/** A student. Admission number is unique only inside a school. */
@Entity
@Table(name = "students",
        uniqueConstraints = @UniqueConstraint(name = "uk_student_school_admission",
                columnNames = {"school_id", "admission_number"}),
        indexes = @Index(name = "idx_student_school", columnList = "school_id"))
public class Student extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "admission_number", nullable = false, length = 40)
    private String admissionNumber;

    /** Class / grade, e.g. "5-A". */
    @Column(nullable = false, length = 20)
    private String grade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    protected Student() { }

    public Student(String name, String admissionNumber, String grade, School school) {
        this.name = name;
        this.admissionNumber = admissionNumber;
        this.grade = grade;
        this.school = school;
    }

    public String getName() { return name; }
    public String getAdmissionNumber() { return admissionNumber; }
    public String getGrade() { return grade; }
    public School getSchool() { return school; }
}

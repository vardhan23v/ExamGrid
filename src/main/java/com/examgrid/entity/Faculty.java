package com.examgrid.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "faculty")
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Faculty code is required")
    @Column(nullable = false, unique = true, length = 15)
    private String facultyCode;

    @NotBlank(message = "Name is required")
    @Column(nullable = false, length = 80)
    private String name;

    @NotBlank(message = "Department is required")
    @Column(nullable = false, length = 10)
    private String department;

    @Email(message = "Enter a valid email")
    @Column(length = 100)
    private String email;

    @Min(value = 1, message = "Maximum duties must be at least 1")
    @Max(value = 20, message = "Maximum duties cannot exceed 20")
    private int maxDuties = 6;

    public Faculty() {
    }

    public Faculty(String facultyCode, String name, String department, String email, int maxDuties) {
        this.facultyCode = facultyCode;
        this.name = name;
        this.department = department;
        this.email = email;
        this.maxDuties = maxDuties;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFacultyCode() {
        return facultyCode;
    }

    public void setFacultyCode(String facultyCode) {
        this.facultyCode = facultyCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getMaxDuties() {
        return maxDuties;
    }

    public void setMaxDuties(int maxDuties) {
        this.maxDuties = maxDuties;
    }
}

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
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "USN is required")
    @Pattern(regexp = "^[A-Za-z0-9]{6,15}$", message = "USN must be 6 to 15 letters or digits")
    @Column(nullable = false, unique = true, length = 15)
    private String usn;

    @NotBlank(message = "Name is required")
    @Column(nullable = false, length = 80)
    private String name;

    @NotBlank(message = "Branch is required")
    @Column(nullable = false, length = 10)
    private String branch;

    @Min(value = 1, message = "Semester must be between 1 and 8")
    @Max(value = 8, message = "Semester must be between 1 and 8")
    private int semester;

    @Email(message = "Enter a valid email")
    @Column(length = 100)
    private String email;

    public Student() {
    }

    public Student(String usn, String name, String branch, int semester, String email) {
        this.usn = usn;
        this.name = name;
        this.branch = branch;
        this.semester = semester;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsn() {
        return usn;
    }

    public void setUsn(String usn) {
        this.usn = usn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

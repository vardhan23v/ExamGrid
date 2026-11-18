package com.examgrid.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examgrid.entity.Faculty;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByFacultyCodeIgnoreCase(String facultyCode);

    boolean existsByFacultyCodeIgnoreCase(String facultyCode);

    List<Faculty> findAllByOrderByNameAsc();
}

package com.examgrid.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examgrid.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUsnIgnoreCase(String usn);

    boolean existsByUsnIgnoreCase(String usn);

    List<Student> findByBranchAndSemesterOrderByUsnAsc(String branch, int semester);

    List<Student> findAllByOrderByUsnAsc();
}

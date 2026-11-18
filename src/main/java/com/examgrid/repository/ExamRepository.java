package com.examgrid.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examgrid.entity.Exam;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByExamDateAndStartTime(LocalDate examDate, LocalTime startTime);

    List<Exam> findAllByOrderByExamDateAscStartTimeAscSubjectCodeAsc();
}

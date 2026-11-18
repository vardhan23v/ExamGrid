package com.examgrid.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.examgrid.entity.InvigilationDuty;

public interface InvigilationDutyRepository extends JpaRepository<InvigilationDuty, Long> {

    @Query("select d from InvigilationDuty d join fetch d.faculty join fetch d.hall " +
           "order by d.examDate, d.startTime, d.hall.hallName")
    List<InvigilationDuty> findAllWithDetails();

    @Query("select d from InvigilationDuty d join fetch d.faculty join fetch d.hall " +
           "where d.examDate = :date order by d.startTime, d.hall.hallName")
    List<InvigilationDuty> findByDate(@Param("date") LocalDate date);

    @Query("select d from InvigilationDuty d join fetch d.faculty join fetch d.hall " +
           "where upper(d.faculty.facultyCode) = upper(:code) order by d.examDate, d.startTime")
    List<InvigilationDuty> findByFacultyCode(@Param("code") String code);

    long countByFacultyId(Long facultyId);

    @Modifying
    @Query("delete from InvigilationDuty d where d.examDate = :date and d.startTime = :time")
    void deleteSlot(@Param("date") LocalDate date, @Param("time") LocalTime time);

    @Modifying
    @Query("delete from InvigilationDuty d where d.faculty.id = :facultyId")
    void deleteByFacultyId(@Param("facultyId") Long facultyId);

    @Modifying
    @Query("delete from InvigilationDuty d where d.hall.id = :hallId")
    void deleteByHallId(@Param("hallId") Long hallId);
}

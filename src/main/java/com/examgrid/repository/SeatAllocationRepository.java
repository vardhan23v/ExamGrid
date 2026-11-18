package com.examgrid.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.examgrid.entity.SeatAllocation;

public interface SeatAllocationRepository extends JpaRepository<SeatAllocation, Long> {

    @Query("select s from SeatAllocation s join fetch s.student join fetch s.exam join fetch s.hall " +
           "where s.examDate = :date and s.startTime = :time " +
           "order by s.hall.hallName, s.rowNumber, s.columnNumber")
    List<SeatAllocation> findSlotAllocations(@Param("date") LocalDate date, @Param("time") LocalTime time);

    @Query("select s from SeatAllocation s join fetch s.student join fetch s.exam join fetch s.hall " +
           "where s.hall.id = :hallId and s.examDate = :date and s.startTime = :time " +
           "order by s.rowNumber, s.columnNumber")
    List<SeatAllocation> findHallAllocations(@Param("hallId") Long hallId,
                                             @Param("date") LocalDate date,
                                             @Param("time") LocalTime time);

    @Query("select s from SeatAllocation s join fetch s.student join fetch s.exam join fetch s.hall " +
           "where upper(s.student.usn) = upper(:usn) order by s.examDate, s.startTime")
    List<SeatAllocation> findByUsn(@Param("usn") String usn);

    @Modifying
    @Query("delete from SeatAllocation s where s.examDate = :date and s.startTime = :time")
    void deleteSlot(@Param("date") LocalDate date, @Param("time") LocalTime time);

    @Modifying
    @Query("delete from SeatAllocation s where s.student.id = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);

    @Modifying
    @Query("delete from SeatAllocation s where s.exam.id = :examId")
    void deleteByExamId(@Param("examId") Long examId);

    @Modifying
    @Query("delete from SeatAllocation s where s.hall.id = :hallId")
    void deleteByHallId(@Param("hallId") Long hallId);
}

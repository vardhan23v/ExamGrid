package com.examgrid.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.examgrid.entity.SeatAllocation;

public record SeatInfo(String usn,
                       String studentName,
                       String branch,
                       String subjectCode,
                       String subjectName,
                       LocalDate examDate,
                       LocalTime startTime,
                       LocalTime endTime,
                       String hallName,
                       String block,
                       String seatNumber,
                       int row,
                       int column) {

    public static SeatInfo from(SeatAllocation seat) {
        return new SeatInfo(
                seat.getStudent().getUsn(),
                seat.getStudent().getName(),
                seat.getStudent().getBranch(),
                seat.getExam().getSubjectCode(),
                seat.getExam().getSubjectName(),
                seat.getExamDate(),
                seat.getExam().getStartTime(),
                seat.getExam().getEndTime(),
                seat.getHall().getHallName(),
                seat.getHall().getBlock(),
                seat.getSeatNumber(),
                seat.getRowNumber(),
                seat.getColumnNumber());
    }
}

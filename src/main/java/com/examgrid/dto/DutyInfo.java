package com.examgrid.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.examgrid.entity.InvigilationDuty;

public record DutyInfo(Long dutyId,
                       String facultyCode,
                       String facultyName,
                       String department,
                       String hallName,
                       String block,
                       LocalDate examDate,
                       LocalTime startTime,
                       LocalTime endTime) {

    public static DutyInfo from(InvigilationDuty duty) {
        return new DutyInfo(
                duty.getId(),
                duty.getFaculty().getFacultyCode(),
                duty.getFaculty().getName(),
                duty.getFaculty().getDepartment(),
                duty.getHall().getHallName(),
                duty.getHall().getBlock(),
                duty.getExamDate(),
                duty.getStartTime(),
                duty.getEndTime());
    }
}

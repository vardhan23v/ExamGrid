package com.examgrid.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record InvigilationResult(LocalDate examDate,
                                 LocalTime startTime,
                                 LocalTime endTime,
                                 int hallsCovered,
                                 int invigilatorsAssigned) {
}

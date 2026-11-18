package com.examgrid.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record SeatingResult(LocalDate examDate,
                            LocalTime startTime,
                            int examsInSlot,
                            int studentsSeated,
                            int hallsUsed,
                            int adjacentSameSubjectPairs) {
}

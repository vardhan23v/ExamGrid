package com.examgrid.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ExamSlot(LocalDate examDate,
                       LocalTime startTime,
                       LocalTime endTime,
                       List<String> subjects) {
}

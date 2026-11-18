package com.examgrid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.examgrid.dto.SeatInfo;
import com.examgrid.dto.SeatingResult;
import com.examgrid.entity.Exam;
import com.examgrid.entity.Hall;
import com.examgrid.exception.AllocationException;
import com.examgrid.exception.InsufficientCapacityException;
import com.examgrid.repository.ExamRepository;
import com.examgrid.repository.HallRepository;

@SpringBootTest
@Transactional
class SeatingAllocationServiceTest {

    private static final LocalDate DAY_ONE = LocalDate.of(2026, 12, 7);
    private static final LocalTime MORNING = LocalTime.of(9, 30);

    @Autowired
    private SeatingAllocationService seatingAllocationService;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private ExamRepository examRepository;

    @Test
    void everyStudentGetsExactlyOneSeat() {
        SeatingResult result = seatingAllocationService.allocateSeats(DAY_ONE, MORNING);

        assertEquals(148, result.studentsSeated());
        List<SeatInfo> seats = seatingAllocationService.findSlotSeats(DAY_ONE, MORNING);
        Set<String> usns = new HashSet<>();
        Set<String> seatNumbers = new HashSet<>();
        for (SeatInfo seat : seats) {
            assertTrue(usns.add(seat.usn()), "student seated twice: " + seat.usn());
            assertTrue(seatNumbers.add(seat.seatNumber()), "seat used twice: " + seat.seatNumber());
        }
    }

    @Test
    void neighboursNeverWriteTheSameSubjectWhenSpaceAllows() {
        SeatingResult result = seatingAllocationService.allocateSeats(DAY_ONE, MORNING);
        assertEquals(0, result.adjacentSameSubjectPairs());

        Map<String, String> subjectAt = new HashMap<>();
        for (SeatInfo seat : seatingAllocationService.findSlotSeats(DAY_ONE, MORNING)) {
            subjectAt.put(seat.hallName() + ":" + seat.row() + ":" + seat.column(), seat.subjectCode());
        }
        for (SeatInfo seat : seatingAllocationService.findSlotSeats(DAY_ONE, MORNING)) {
            String right = subjectAt.get(seat.hallName() + ":" + seat.row() + ":" + (seat.column() + 1));
            String behind = subjectAt.get(seat.hallName() + ":" + (seat.row() + 1) + ":" + seat.column());
            if (right != null) {
                assertNotEquals(seat.subjectCode(), right);
            }
            if (behind != null) {
                assertNotEquals(seat.subjectCode(), behind);
            }
        }
    }

    @Test
    void noHallIsFilledBeyondItsCapacity() {
        seatingAllocationService.allocateSeats(DAY_ONE, MORNING);
        Map<String, Integer> used = new HashMap<>();
        for (SeatInfo seat : seatingAllocationService.findSlotSeats(DAY_ONE, MORNING)) {
            used.merge(seat.hallName(), 1, Integer::sum);
        }
        for (Hall hall : hallRepository.findAll()) {
            assertTrue(used.getOrDefault(hall.getHallName(), 0) <= hall.getCapacity());
        }
    }

    @Test
    void rerunningAllocationReplacesOldPlan() {
        seatingAllocationService.allocateSeats(DAY_ONE, MORNING);
        seatingAllocationService.allocateSeats(DAY_ONE, MORNING);
        assertEquals(148, seatingAllocationService.findSlotSeats(DAY_ONE, MORNING).size());
    }

    @Test
    void failsWhenHallsAreTooSmall() {
        hallRepository.deleteAll(hallRepository.findAll().stream()
                .filter(h -> !h.getHallName().equals("LH-101")).toList());
        assertThrows(InsufficientCapacityException.class,
                () -> seatingAllocationService.allocateSeats(DAY_ONE, MORNING));
    }

    @Test
    void failsWhenNoExamInSlot() {
        assertThrows(AllocationException.class,
                () -> seatingAllocationService.allocateSeats(LocalDate.of(2027, 1, 1), MORNING));
    }

    @Test
    void singleSubjectSlotLeavesGapsInsteadOfClashing() {
        Exam onlyExam = new Exam("CS9999", "Elective", "CSE", 3, LocalDate.of(2026, 12, 20),
                LocalTime.of(10, 0), LocalTime.of(13, 0));
        examRepository.save(onlyExam);

        SeatingResult result = seatingAllocationService.allocateSeats(onlyExam.getExamDate(), onlyExam.getStartTime());

        assertEquals(40, result.studentsSeated());
        assertEquals(0, result.adjacentSameSubjectPairs());
    }
}

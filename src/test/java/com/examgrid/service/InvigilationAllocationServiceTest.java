package com.examgrid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.examgrid.dto.DutyInfo;
import com.examgrid.dto.InvigilationResult;
import com.examgrid.exception.AllocationException;
import com.examgrid.exception.ResourceNotFoundException;
import com.examgrid.repository.FacultyRepository;

@SpringBootTest
@Transactional
class InvigilationAllocationServiceTest {

    private static final LocalDate DAY_ONE = LocalDate.of(2026, 12, 7);
    private static final LocalDate DAY_TWO = LocalDate.of(2026, 12, 8);
    private static final LocalTime MORNING = LocalTime.of(9, 30);
    private static final LocalTime AFTERNOON = LocalTime.of(14, 0);

    @Autowired
    private SeatingAllocationService seatingAllocationService;

    @Autowired
    private InvigilationAllocationService invigilationAllocationService;

    @Autowired
    private FacultyRepository facultyRepository;

    @Test
    void needsSeatingBeforeInvigilation() {
        assertThrows(AllocationException.class,
                () -> invigilationAllocationService.allocateInvigilators(DAY_ONE, MORNING));
    }

    @Test
    void everyUsedHallGetsEnoughInvigilators() {
        seatingAllocationService.allocateSeats(DAY_ONE, MORNING);
        InvigilationResult result = invigilationAllocationService.allocateInvigilators(DAY_ONE, MORNING);

        assertTrue(result.hallsCovered() > 0);
        assertTrue(result.invigilatorsAssigned() >= result.hallsCovered());
    }

    @Test
    void noFacultyHasTwoDutiesAtTheSameTime() {
        seatingAllocationService.allocateSeats(DAY_ONE, MORNING);
        invigilationAllocationService.allocateInvigilators(DAY_ONE, MORNING);
        seatingAllocationService.allocateSeats(DAY_TWO, MORNING);
        invigilationAllocationService.allocateInvigilators(DAY_TWO, MORNING);
        seatingAllocationService.allocateSeats(DAY_TWO, AFTERNOON);
        invigilationAllocationService.allocateInvigilators(DAY_TWO, AFTERNOON);

        Set<String> facultySlots = new HashSet<>();
        for (DutyInfo duty : invigilationAllocationService.findAllDuties()) {
            String key = duty.facultyCode() + "@" + duty.examDate() + "T" + duty.startTime();
            assertTrue(facultySlots.add(key), "time clash for " + key);
        }
    }

    @Test
    void workloadIsBalanced() {
        seatingAllocationService.allocateSeats(DAY_ONE, MORNING);
        invigilationAllocationService.allocateInvigilators(DAY_ONE, MORNING);
        seatingAllocationService.allocateSeats(DAY_TWO, MORNING);
        invigilationAllocationService.allocateInvigilators(DAY_TWO, MORNING);

        Map<String, Integer> load = new HashMap<>();
        facultyRepository.findAll().forEach(f -> load.put(f.getFacultyCode(), 0));
        for (DutyInfo duty : invigilationAllocationService.findAllDuties()) {
            load.merge(duty.facultyCode(), 1, Integer::sum);
        }
        int max = load.values().stream().max(Integer::compare).orElse(0);
        int min = load.values().stream().min(Integer::compare).orElse(0);
        assertTrue(max - min <= 1, "workload spread too large: " + load);
    }

    @Test
    void failsWhenFacultyAreNotEnough() {
        seatingAllocationService.allocateSeats(DAY_ONE, MORNING);
        facultyRepository.findAll().forEach(f -> f.setMaxDuties(0));
        assertThrows(AllocationException.class,
                () -> invigilationAllocationService.allocateInvigilators(DAY_ONE, MORNING));
    }

    @Test
    void invigilatorCountFollowsStudentsPerInvigilatorRule() {
        assertEquals(1, invigilationAllocationService.invigilatorsNeeded(1));
        assertEquals(1, invigilationAllocationService.invigilatorsNeeded(30));
        assertEquals(2, invigilationAllocationService.invigilatorsNeeded(31));
        assertEquals(3, invigilationAllocationService.invigilatorsNeeded(64));
    }

    @Test
    void rosterOfUnknownFacultyFails() {
        assertThrows(ResourceNotFoundException.class, () -> invigilationAllocationService.findDutiesOfFaculty("NOPE"));
    }
}

package com.examgrid.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examgrid.dto.DutyInfo;
import com.examgrid.dto.InvigilationResult;
import com.examgrid.entity.Faculty;
import com.examgrid.entity.Hall;
import com.examgrid.entity.InvigilationDuty;
import com.examgrid.entity.SeatAllocation;
import com.examgrid.exception.AllocationException;
import com.examgrid.exception.ResourceNotFoundException;
import com.examgrid.repository.FacultyRepository;
import com.examgrid.repository.InvigilationDutyRepository;
import com.examgrid.repository.SeatAllocationRepository;

@Service
public class InvigilationAllocationService {

    private final SeatAllocationRepository seatAllocationRepository;
    private final InvigilationDutyRepository invigilationDutyRepository;
    private final FacultyRepository facultyRepository;
    private final int studentsPerInvigilator;

    public InvigilationAllocationService(SeatAllocationRepository seatAllocationRepository,
                                         InvigilationDutyRepository invigilationDutyRepository,
                                         FacultyRepository facultyRepository,
                                         @Value("${examgrid.students-per-invigilator:30}") int studentsPerInvigilator) {
        this.seatAllocationRepository = seatAllocationRepository;
        this.invigilationDutyRepository = invigilationDutyRepository;
        this.facultyRepository = facultyRepository;
        this.studentsPerInvigilator = studentsPerInvigilator;
    }

    @Transactional
    public InvigilationResult allocateInvigilators(LocalDate examDate, LocalTime startTime) {
        List<SeatAllocation> seats = seatAllocationRepository.findSlotAllocations(examDate, startTime);
        if (seats.isEmpty()) {
            throw new AllocationException("Generate the seating plan for " + examDate + " " + startTime
                    + " before assigning invigilators");
        }

        LocalTime endTime = seats.stream()
                .map(s -> s.getExam().getEndTime())
                .max(Comparator.naturalOrder())
                .orElseThrow();

        Map<Long, Hall> hallsById = new LinkedHashMap<>();
        Map<Long, Integer> studentsInHall = new HashMap<>();
        for (SeatAllocation seat : seats) {
            hallsById.putIfAbsent(seat.getHall().getId(), seat.getHall());
            studentsInHall.merge(seat.getHall().getId(), 1, Integer::sum);
        }

        invigilationDutyRepository.deleteSlot(examDate, startTime);

        List<Faculty> allFaculty = facultyRepository.findAll();
        if (allFaculty.isEmpty()) {
            throw new AllocationException("No faculty members are available for invigilation");
        }

        Map<Long, List<InvigilationDuty>> dutiesOf = new HashMap<>();
        for (InvigilationDuty duty : invigilationDutyRepository.findAllWithDetails()) {
            dutiesOf.computeIfAbsent(duty.getFaculty().getId(), id -> new ArrayList<>()).add(duty);
        }

        List<InvigilationDuty> newDuties = new ArrayList<>();
        for (Hall hall : hallsById.values()) {
            int needed = invigilatorsNeeded(studentsInHall.get(hall.getId()));
            for (int i = 0; i < needed; i++) {
                Faculty chosen = pickLeastLoadedFreeFaculty(allFaculty, dutiesOf, examDate, startTime, endTime);
                if (chosen == null) {
                    throw new AllocationException("Not enough free invigilators for hall " + hall.getHallName()
                            + " on " + examDate + " " + startTime + ". Add faculty or raise their duty limit.");
                }
                InvigilationDuty duty = new InvigilationDuty(chosen, hall, examDate, startTime, endTime);
                dutiesOf.computeIfAbsent(chosen.getId(), id -> new ArrayList<>()).add(duty);
                newDuties.add(duty);
            }
        }

        invigilationDutyRepository.saveAll(newDuties);
        return new InvigilationResult(examDate, startTime, endTime, hallsById.size(), newDuties.size());
    }

    int invigilatorsNeeded(int students) {
        return Math.max(1, (students + studentsPerInvigilator - 1) / studentsPerInvigilator);
    }

    private Faculty pickLeastLoadedFreeFaculty(List<Faculty> allFaculty,
                                               Map<Long, List<InvigilationDuty>> dutiesOf,
                                               LocalDate date, LocalTime from, LocalTime to) {
        Faculty best = null;
        int bestLoad = Integer.MAX_VALUE;
        for (Faculty faculty : allFaculty) {
            List<InvigilationDuty> duties = dutiesOf.getOrDefault(faculty.getId(), List.of());
            if (duties.size() >= faculty.getMaxDuties()) {
                continue;
            }
            boolean clash = duties.stream().anyMatch(d -> d.overlaps(date, from, to));
            if (clash) {
                continue;
            }
            if (duties.size() < bestLoad
                    || (duties.size() == bestLoad && faculty.getName().compareTo(best.getName()) < 0)) {
                best = faculty;
                bestLoad = duties.size();
            }
        }
        return best;
    }

    @Transactional(readOnly = true)
    public List<DutyInfo> findAllDuties() {
        return invigilationDutyRepository.findAllWithDetails().stream().map(DutyInfo::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DutyInfo> findDutiesByDate(LocalDate date) {
        return invigilationDutyRepository.findByDate(date).stream().map(DutyInfo::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DutyInfo> findDutiesOfFaculty(String facultyCode) {
        if (!facultyRepository.existsByFacultyCodeIgnoreCase(facultyCode)) {
            throw new ResourceNotFoundException("Faculty", facultyCode);
        }
        return invigilationDutyRepository.findByFacultyCode(facultyCode).stream().map(DutyInfo::from).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> workloadSummary() {
        Map<String, Long> workload = new LinkedHashMap<>();
        for (Faculty faculty : facultyRepository.findAllByOrderByNameAsc()) {
            workload.put(faculty.getName(), invigilationDutyRepository.countByFacultyId(faculty.getId()));
        }
        return workload;
    }
}

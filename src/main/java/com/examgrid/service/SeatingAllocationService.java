package com.examgrid.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examgrid.dto.HallLayout;
import com.examgrid.dto.HallLayout.SeatCell;
import com.examgrid.dto.SeatInfo;
import com.examgrid.dto.SeatingResult;
import com.examgrid.entity.Exam;
import com.examgrid.entity.Hall;
import com.examgrid.entity.SeatAllocation;
import com.examgrid.entity.Student;
import com.examgrid.exception.AllocationException;
import com.examgrid.exception.InsufficientCapacityException;
import com.examgrid.exception.ResourceNotFoundException;
import com.examgrid.repository.ExamRepository;
import com.examgrid.repository.HallRepository;
import com.examgrid.repository.InvigilationDutyRepository;
import com.examgrid.repository.SeatAllocationRepository;
import com.examgrid.repository.StudentRepository;

@Service
public class SeatingAllocationService {

    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;
    private final HallRepository hallRepository;
    private final SeatAllocationRepository seatAllocationRepository;
    private final InvigilationDutyRepository invigilationDutyRepository;

    public SeatingAllocationService(ExamRepository examRepository,
                                    StudentRepository studentRepository,
                                    HallRepository hallRepository,
                                    SeatAllocationRepository seatAllocationRepository,
                                    InvigilationDutyRepository invigilationDutyRepository) {
        this.examRepository = examRepository;
        this.studentRepository = studentRepository;
        this.hallRepository = hallRepository;
        this.seatAllocationRepository = seatAllocationRepository;
        this.invigilationDutyRepository = invigilationDutyRepository;
    }

    @Transactional
    public SeatingResult allocateSeats(LocalDate examDate, LocalTime startTime) {
        List<Exam> exams = examRepository.findByExamDateAndStartTime(examDate, startTime);
        if (exams.isEmpty()) {
            throw new AllocationException("No exams are scheduled on " + examDate + " at " + startTime);
        }

        Map<Exam, Deque<Student>> waiting = new LinkedHashMap<>();
        int totalStudents = 0;
        for (Exam exam : exams) {
            List<Student> students = studentRepository
                    .findByBranchAndSemesterOrderByUsnAsc(exam.getBranch(), exam.getSemester());
            if (!students.isEmpty()) {
                waiting.put(exam, new ArrayDeque<>(students));
                totalStudents += students.size();
            }
        }
        if (totalStudents == 0) {
            throw new AllocationException("No students are registered for the exams in this slot");
        }

        List<Hall> halls = new ArrayList<>(hallRepository.findAll());
        halls.sort(Comparator.comparingInt(Hall::getCapacity).reversed().thenComparing(Hall::getHallName));
        int totalCapacity = halls.stream().mapToInt(Hall::getCapacity).sum();
        if (totalStudents > totalCapacity) {
            throw new InsufficientCapacityException(totalStudents, totalCapacity);
        }

        invigilationDutyRepository.deleteSlot(examDate, startTime);
        seatAllocationRepository.deleteSlot(examDate, startTime);

        List<SeatAllocation> seats = new ArrayList<>();
        int remainingStudents = totalStudents;
        int remainingSeats = totalCapacity;
        int hallsUsed = 0;
        int clashes = 0;

        for (Hall hall : halls) {
            if (remainingStudents == 0) {
                break;
            }
            hallsUsed++;
            Exam[][] grid = new Exam[hall.getNumberOfRows() + 1][hall.getNumberOfColumns() + 1];

            for (int row = 1; row <= hall.getNumberOfRows() && remainingStudents > 0; row++) {
                for (int col = 1; col <= hall.getNumberOfColumns() && remainingStudents > 0; col++) {
                    Exam left = grid[row][col - 1];
                    Exam front = grid[row - 1][col];

                    Exam chosen = pickExam(waiting, left, front, true);
                    if (chosen == null) {
                        chosen = pickExam(waiting, left, front, false);
                    }
                    if (chosen == null) {
                        if (remainingSeats > remainingStudents) {
                            remainingSeats--;
                            continue;
                        }
                        chosen = largestGroup(waiting);
                        clashes++;
                    }

                    Student student = waiting.get(chosen).poll();
                    grid[row][col] = chosen;
                    seats.add(new SeatAllocation(chosen, student, hall, row, col));
                    remainingStudents--;
                    remainingSeats--;
                }
            }
        }

        seatAllocationRepository.saveAll(seats);
        return new SeatingResult(examDate, startTime, exams.size(), seats.size(), hallsUsed, clashes);
    }

    private Exam pickExam(Map<Exam, Deque<Student>> waiting, Exam left, Exam front, boolean avoidSameBranch) {
        Exam best = null;
        int bestCount = 0;
        for (Map.Entry<Exam, Deque<Student>> entry : waiting.entrySet()) {
            Exam exam = entry.getKey();
            int count = entry.getValue().size();
            if (count == 0 || exam == left || exam == front) {
                continue;
            }
            if (avoidSameBranch && (sameBranch(exam, left) || sameBranch(exam, front))) {
                continue;
            }
            if (count > bestCount) {
                best = exam;
                bestCount = count;
            }
        }
        return best;
    }

    private boolean sameBranch(Exam exam, Exam neighbour) {
        return neighbour != null && exam.getBranch().equalsIgnoreCase(neighbour.getBranch());
    }

    private Exam largestGroup(Map<Exam, Deque<Student>> waiting) {
        return waiting.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .max(Comparator.comparingInt(e -> e.getValue().size()))
                .map(Map.Entry::getKey)
                .orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<SeatInfo> findSeatsByUsn(String usn) {
        if (!studentRepository.existsByUsnIgnoreCase(usn)) {
            throw new ResourceNotFoundException("Student", usn);
        }
        return seatAllocationRepository.findByUsn(usn).stream().map(SeatInfo::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SeatInfo> findSlotSeats(LocalDate examDate, LocalTime startTime) {
        return seatAllocationRepository.findSlotAllocations(examDate, startTime)
                .stream().map(SeatInfo::from).toList();
    }

    @Transactional(readOnly = true)
    public List<HallLayout> buildHallLayouts(LocalDate examDate, LocalTime startTime) {
        List<SeatAllocation> seats = seatAllocationRepository.findSlotAllocations(examDate, startTime);
        Map<Long, List<SeatAllocation>> byHall = new LinkedHashMap<>();
        Map<Long, Integer> colourOfExam = new HashMap<>();
        for (SeatAllocation seat : seats) {
            byHall.computeIfAbsent(seat.getHall().getId(), id -> new ArrayList<>()).add(seat);
            colourOfExam.putIfAbsent(seat.getExam().getId(), colourOfExam.size() % 6);
        }

        List<HallLayout> layouts = new ArrayList<>();
        for (List<SeatAllocation> hallSeats : byHall.values()) {
            Hall hall = hallSeats.get(0).getHall();
            SeatCell[][] cells = new SeatCell[hall.getNumberOfRows()][hall.getNumberOfColumns()];
            for (SeatAllocation seat : hallSeats) {
                cells[seat.getRowNumber() - 1][seat.getColumnNumber() - 1] = new SeatCell(
                        seatLabel(seat.getRowNumber(), seat.getColumnNumber()),
                        seat.getStudent().getUsn(),
                        seat.getExam().getSubjectCode(),
                        colourOfExam.get(seat.getExam().getId()));
            }
            List<List<SeatCell>> grid = new ArrayList<>();
            for (int r = 0; r < hall.getNumberOfRows(); r++) {
                List<SeatCell> line = new ArrayList<>();
                for (int c = 0; c < hall.getNumberOfColumns(); c++) {
                    SeatCell cell = cells[r][c];
                    line.add(cell != null ? cell : new SeatCell(seatLabel(r + 1, c + 1), null, null, -1));
                }
                grid.add(line);
            }
            layouts.add(new HallLayout(hall.getId(), hall.getHallName(), hall.getBlock(),
                    hall.getNumberOfRows(), hall.getNumberOfColumns(), hallSeats.size(), grid));
        }
        return layouts;
    }

    public static String seatLabel(int row, int column) {
        return String.valueOf((char) ('A' + row - 1)) + column;
    }
}

package com.examgrid.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examgrid.dto.ExamSlot;
import com.examgrid.entity.Exam;
import com.examgrid.exception.InvalidRequestException;
import com.examgrid.exception.ResourceNotFoundException;
import com.examgrid.repository.ExamRepository;
import com.examgrid.repository.SeatAllocationRepository;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final SeatAllocationRepository seatAllocationRepository;

    public ExamService(ExamRepository examRepository, SeatAllocationRepository seatAllocationRepository) {
        this.examRepository = examRepository;
        this.seatAllocationRepository = seatAllocationRepository;
    }

    public List<Exam> findAll() {
        return examRepository.findAllByOrderByExamDateAscStartTimeAscSubjectCodeAsc();
    }

    public Exam findById(Long id) {
        return examRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Exam", id));
    }

    public long count() {
        return examRepository.count();
    }

    public String findTimingProblem(Exam exam) {
        if (exam.getStartTime() == null || exam.getEndTime() == null || exam.getExamDate() == null) {
            return null;
        }
        if (!exam.getEndTime().isAfter(exam.getStartTime())) {
            return "End time must be after start time";
        }
        for (Exam other : examRepository.findAll()) {
            boolean sameClass = other.getBranch().equalsIgnoreCase(exam.getBranch())
                    && other.getSemester() == exam.getSemester();
            boolean overlaps = other.getExamDate().equals(exam.getExamDate())
                    && other.getStartTime().isBefore(exam.getEndTime())
                    && exam.getStartTime().isBefore(other.getEndTime());
            if (sameClass && overlaps && !other.getId().equals(exam.getId())) {
                return exam.getBranch().toUpperCase() + " semester " + exam.getSemester()
                        + " already has " + other.getSubjectCode() + " at this time";
            }
        }
        return null;
    }

    @Transactional
    public Exam save(Exam exam) {
        String problem = findTimingProblem(exam);
        if (problem != null) {
            throw new InvalidRequestException(problem);
        }
        exam.setSubjectCode(exam.getSubjectCode().toUpperCase());
        exam.setBranch(exam.getBranch().toUpperCase());
        return examRepository.save(exam);
    }

    @Transactional
    public Exam update(Long id, Exam changes) {
        findById(id);
        changes.setId(id);
        seatAllocationRepository.deleteByExamId(id);
        return save(changes);
    }

    @Transactional
    public void delete(Long id) {
        Exam exam = findById(id);
        seatAllocationRepository.deleteByExamId(id);
        examRepository.delete(exam);
    }

    public List<ExamSlot> findSlots() {
        Map<String, List<Exam>> grouped = new LinkedHashMap<>();
        for (Exam exam : findAll()) {
            String key = exam.getExamDate() + " " + exam.getStartTime();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(exam);
        }
        List<ExamSlot> slots = new ArrayList<>();
        for (List<Exam> exams : grouped.values()) {
            Exam first = exams.get(0);
            List<String> subjects = exams.stream()
                    .map(e -> e.getSubjectCode() + " (" + e.getBranch() + "-" + e.getSemester() + ")")
                    .toList();
            slots.add(new ExamSlot(first.getExamDate(), first.getStartTime(), first.getEndTime(), subjects));
        }
        return slots;
    }
}

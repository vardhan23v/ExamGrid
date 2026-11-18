package com.examgrid.controller.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.examgrid.dto.ExamSlot;
import com.examgrid.entity.Exam;
import com.examgrid.service.ExamService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/exams")
public class ExamApiController {

    private final ExamService examService;

    public ExamApiController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public List<Exam> getAll() {
        return examService.findAll();
    }

    @GetMapping("/slots")
    public List<ExamSlot> getSlots() {
        return examService.findSlots();
    }

    @GetMapping("/{id}")
    public Exam getOne(@PathVariable Long id) {
        return examService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Exam> create(@Valid @RequestBody Exam exam) {
        exam.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(examService.save(exam));
    }

    @PutMapping("/{id}")
    public Exam update(@PathVariable Long id, @Valid @RequestBody Exam exam) {
        return examService.update(id, exam);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        examService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

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

import com.examgrid.entity.Faculty;
import com.examgrid.service.FacultyService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/faculty")
public class FacultyApiController {

    private final FacultyService facultyService;

    public FacultyApiController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    public List<Faculty> getAll() {
        return facultyService.findAll();
    }

    @GetMapping("/{id}")
    public Faculty getOne(@PathVariable Long id) {
        return facultyService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Faculty> create(@Valid @RequestBody Faculty faculty) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facultyService.create(faculty));
    }

    @PutMapping("/{id}")
    public Faculty update(@PathVariable Long id, @Valid @RequestBody Faculty faculty) {
        return facultyService.update(id, faculty);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facultyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

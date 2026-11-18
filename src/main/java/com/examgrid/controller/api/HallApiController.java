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

import com.examgrid.entity.Hall;
import com.examgrid.service.HallService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/halls")
public class HallApiController {

    private final HallService hallService;

    public HallApiController(HallService hallService) {
        this.hallService = hallService;
    }

    @GetMapping
    public List<Hall> getAll() {
        return hallService.findAll();
    }

    @GetMapping("/{id}")
    public Hall getOne(@PathVariable Long id) {
        return hallService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Hall> create(@Valid @RequestBody Hall hall) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hallService.create(hall));
    }

    @PutMapping("/{id}")
    public Hall update(@PathVariable Long id, @Valid @RequestBody Hall hall) {
        return hallService.update(id, hall);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hallService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

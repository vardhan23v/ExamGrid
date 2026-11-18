package com.examgrid.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examgrid.entity.Hall;
import com.examgrid.exception.DuplicateResourceException;
import com.examgrid.exception.ResourceNotFoundException;
import com.examgrid.repository.HallRepository;
import com.examgrid.repository.InvigilationDutyRepository;
import com.examgrid.repository.SeatAllocationRepository;

@Service
public class HallService {

    private final HallRepository hallRepository;
    private final SeatAllocationRepository seatAllocationRepository;
    private final InvigilationDutyRepository invigilationDutyRepository;

    public HallService(HallRepository hallRepository,
                       SeatAllocationRepository seatAllocationRepository,
                       InvigilationDutyRepository invigilationDutyRepository) {
        this.hallRepository = hallRepository;
        this.seatAllocationRepository = seatAllocationRepository;
        this.invigilationDutyRepository = invigilationDutyRepository;
    }

    public List<Hall> findAll() {
        return hallRepository.findAllByOrderByHallNameAsc();
    }

    public Hall findById(Long id) {
        return hallRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hall", id));
    }

    public boolean nameExists(String hallName) {
        return hallRepository.existsByHallNameIgnoreCase(hallName);
    }

    public long count() {
        return hallRepository.count();
    }

    public int totalCapacity() {
        return hallRepository.findAll().stream().mapToInt(Hall::getCapacity).sum();
    }

    @Transactional
    public Hall create(Hall hall) {
        if (nameExists(hall.getHallName())) {
            throw new DuplicateResourceException("Hall " + hall.getHallName() + " already exists");
        }
        hall.setId(null);
        hall.setHallName(hall.getHallName().toUpperCase());
        return hallRepository.save(hall);
    }

    @Transactional
    public Hall update(Long id, Hall changes) {
        Hall existing = findById(id);
        existing.setBlock(changes.getBlock());
        existing.setNumberOfRows(changes.getNumberOfRows());
        existing.setNumberOfColumns(changes.getNumberOfColumns());
        return hallRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Hall hall = findById(id);
        invigilationDutyRepository.deleteByHallId(id);
        seatAllocationRepository.deleteByHallId(id);
        hallRepository.delete(hall);
    }
}

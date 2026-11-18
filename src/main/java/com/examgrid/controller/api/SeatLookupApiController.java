package com.examgrid.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.examgrid.dto.SeatInfo;
import com.examgrid.service.SeatingAllocationService;

@RestController
@RequestMapping("/api/seats")
public class SeatLookupApiController {

    private final SeatingAllocationService seatingAllocationService;

    public SeatLookupApiController(SeatingAllocationService seatingAllocationService) {
        this.seatingAllocationService = seatingAllocationService;
    }

    @GetMapping("/{usn}")
    public List<SeatInfo> findSeats(@PathVariable String usn) {
        return seatingAllocationService.findSeatsByUsn(usn.trim());
    }
}

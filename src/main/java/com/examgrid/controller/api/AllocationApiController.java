package com.examgrid.controller.api;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.examgrid.dto.InvigilationResult;
import com.examgrid.dto.SeatInfo;
import com.examgrid.dto.SeatingResult;
import com.examgrid.service.InvigilationAllocationService;
import com.examgrid.service.SeatingAllocationService;

@RestController
@RequestMapping("/api/allocations")
public class AllocationApiController {

    private final SeatingAllocationService seatingAllocationService;
    private final InvigilationAllocationService invigilationAllocationService;

    public AllocationApiController(SeatingAllocationService seatingAllocationService,
                                   InvigilationAllocationService invigilationAllocationService) {
        this.seatingAllocationService = seatingAllocationService;
        this.invigilationAllocationService = invigilationAllocationService;
    }

    @PostMapping("/seating")
    public SeatingResult generateSeating(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                         @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime) {
        return seatingAllocationService.allocateSeats(date, startTime);
    }

    @GetMapping("/seating")
    public List<SeatInfo> getSeating(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                     @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime) {
        return seatingAllocationService.findSlotSeats(date, startTime);
    }

    @PostMapping("/invigilation")
    public InvigilationResult generateInvigilation(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                   @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime) {
        return invigilationAllocationService.allocateInvigilators(date, startTime);
    }
}

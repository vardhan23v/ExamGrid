package com.examgrid.controller.api;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.examgrid.dto.DutyInfo;
import com.examgrid.service.InvigilationAllocationService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/duties")
public class DutyApiController {

    private final InvigilationAllocationService invigilationAllocationService;

    public DutyApiController(InvigilationAllocationService invigilationAllocationService) {
        this.invigilationAllocationService = invigilationAllocationService;
    }

    @GetMapping
    public List<DutyInfo> getDuties(@RequestParam(required = false)
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return date == null
                ? invigilationAllocationService.findAllDuties()
                : invigilationAllocationService.findDutiesByDate(date);
    }

    @GetMapping("/faculty/{facultyCode}")
    public List<DutyInfo> getFacultyRoster(@PathVariable String facultyCode, Principal principal,
                                           HttpServletRequest request) {
        boolean admin = request.isUserInRole("ADMIN");
        if (!admin && !principal.getName().equalsIgnoreCase(facultyCode)) {
            throw new AccessDeniedException("You can only view your own duty roster");
        }
        return invigilationAllocationService.findDutiesOfFaculty(facultyCode);
    }

    @GetMapping("/workload")
    public Map<String, Long> getWorkload() {
        return invigilationAllocationService.workloadSummary();
    }
}

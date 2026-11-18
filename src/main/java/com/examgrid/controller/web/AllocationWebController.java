package com.examgrid.controller.web;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.examgrid.dto.InvigilationResult;
import com.examgrid.dto.SeatingResult;
import com.examgrid.service.ExamService;
import com.examgrid.service.InvigilationAllocationService;
import com.examgrid.service.SeatingAllocationService;

@Controller
@RequestMapping("/admin")
public class AllocationWebController {

    private final ExamService examService;
    private final SeatingAllocationService seatingAllocationService;
    private final InvigilationAllocationService invigilationAllocationService;

    public AllocationWebController(ExamService examService,
                                   SeatingAllocationService seatingAllocationService,
                                   InvigilationAllocationService invigilationAllocationService) {
        this.examService = examService;
        this.seatingAllocationService = seatingAllocationService;
        this.invigilationAllocationService = invigilationAllocationService;
    }

    @GetMapping("/allocation")
    public String allocationPage(Model model) {
        model.addAttribute("slots", examService.findSlots());
        return "admin/allocation";
    }

    @PostMapping("/allocation/seating")
    public String generateSeating(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                  @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
                                  RedirectAttributes redirect) {
        SeatingResult result = seatingAllocationService.allocateSeats(date, startTime);
        String message = result.studentsSeated() + " students seated in " + result.hallsUsed() + " hall(s) for "
                + result.examsInSlot() + " exam(s).";
        if (result.adjacentSameSubjectPairs() > 0) {
            message += " Warning: " + result.adjacentSameSubjectPairs()
                    + " seat(s) had to be placed next to the same subject because halls were nearly full.";
        } else {
            message += " No two neighbouring students write the same subject.";
        }
        redirect.addFlashAttribute("success", message);
        redirect.addAttribute("date", date.toString());
        redirect.addAttribute("startTime", startTime.toString());
        return "redirect:/admin/seating";
    }

    @PostMapping("/allocation/invigilation")
    public String generateInvigilation(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                       @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
                                       RedirectAttributes redirect) {
        InvigilationResult result = invigilationAllocationService.allocateInvigilators(date, startTime);
        redirect.addFlashAttribute("success", result.invigilatorsAssigned() + " invigilator(s) assigned to "
                + result.hallsCovered() + " hall(s) with no time clashes.");
        redirect.addAttribute("date", date.toString());
        return "redirect:/admin/duties";
    }

    @GetMapping("/seating")
    public String viewSeating(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
                              Model model) {
        model.addAttribute("date", date);
        model.addAttribute("startTime", startTime);
        model.addAttribute("layouts", seatingAllocationService.buildHallLayouts(date, startTime));
        return "admin/seating";
    }

    @GetMapping("/duties")
    public String viewDuties(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             Model model) {
        model.addAttribute("date", date);
        model.addAttribute("duties", date == null
                ? invigilationAllocationService.findAllDuties()
                : invigilationAllocationService.findDutiesByDate(date));
        model.addAttribute("workload", invigilationAllocationService.workloadSummary());
        return "admin/duties";
    }
}

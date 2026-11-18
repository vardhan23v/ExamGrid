package com.examgrid.controller.web;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.examgrid.service.FacultyService;
import com.examgrid.service.StudentService;

@Controller
public class PortalController {

    private final FacultyService facultyService;
    private final StudentService studentService;

    public PortalController(FacultyService facultyService, StudentService studentService) {
        this.facultyService = facultyService;
        this.studentService = studentService;
    }

    @GetMapping("/faculty/duties")
    public String facultyDuties(Principal principal, Model model) {
        model.addAttribute("faculty", facultyService.findByCode(principal.getName()));
        return "faculty/duties";
    }

    @GetMapping("/student/seat")
    public String studentSeat(Principal principal, Model model) {
        model.addAttribute("student", studentService.findByUsn(principal.getName()));
        return "student/seat";
    }
}

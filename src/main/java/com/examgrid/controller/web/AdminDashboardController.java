package com.examgrid.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.examgrid.service.ExamService;
import com.examgrid.service.FacultyService;
import com.examgrid.service.HallService;
import com.examgrid.service.InvigilationAllocationService;
import com.examgrid.service.StudentService;

@Controller
public class AdminDashboardController {

    private final StudentService studentService;
    private final FacultyService facultyService;
    private final HallService hallService;
    private final ExamService examService;
    private final InvigilationAllocationService invigilationAllocationService;

    public AdminDashboardController(StudentService studentService, FacultyService facultyService,
                                    HallService hallService, ExamService examService,
                                    InvigilationAllocationService invigilationAllocationService) {
        this.studentService = studentService;
        this.facultyService = facultyService;
        this.hallService = hallService;
        this.examService = examService;
        this.invigilationAllocationService = invigilationAllocationService;
    }

    @GetMapping({"/admin", "/admin/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("studentCount", studentService.count());
        model.addAttribute("facultyCount", facultyService.count());
        model.addAttribute("hallCount", hallService.count());
        model.addAttribute("totalCapacity", hallService.totalCapacity());
        model.addAttribute("examCount", examService.count());
        model.addAttribute("slots", examService.findSlots());
        model.addAttribute("workload", invigilationAllocationService.workloadSummary());
        return "admin/dashboard";
    }
}

package com.examgrid.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.examgrid.entity.Student;
import com.examgrid.service.StudentService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/students")
public class StudentWebController {

    private final StudentService studentService;

    public StudentWebController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("students", studentService.findAll());
        return "admin/students";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.findById(id));
        model.addAttribute("students", studentService.findAll());
        return "admin/students";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("student") Student student, BindingResult result,
                       Model model, RedirectAttributes redirect) {
        if (student.getId() == null && !result.hasFieldErrors("usn") && studentService.usnExists(student.getUsn())) {
            result.rejectValue("usn", "duplicate", "This USN is already registered");
        }
        if (result.hasErrors()) {
            model.addAttribute("students", studentService.findAll());
            return "admin/students";
        }
        if (student.getId() == null) {
            studentService.create(student);
            redirect.addFlashAttribute("success", "Student " + student.getUsn().toUpperCase()
                    + " added. Login password: Welcome@123");
        } else {
            studentService.update(student.getId(), student);
            redirect.addFlashAttribute("success", "Student details updated");
        }
        return "redirect:/admin/students";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        studentService.delete(id);
        redirect.addFlashAttribute("success", "Student removed");
        return "redirect:/admin/students";
    }
}

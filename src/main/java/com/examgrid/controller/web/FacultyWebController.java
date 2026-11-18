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

import com.examgrid.entity.Faculty;
import com.examgrid.service.FacultyService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/faculty")
public class FacultyWebController {

    private final FacultyService facultyService;

    public FacultyWebController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("faculty", new Faculty());
        model.addAttribute("facultyList", facultyService.findAll());
        return "admin/faculty";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("faculty", facultyService.findById(id));
        model.addAttribute("facultyList", facultyService.findAll());
        return "admin/faculty";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("faculty") Faculty faculty, BindingResult result,
                       Model model, RedirectAttributes redirect) {
        if (faculty.getId() == null && !result.hasFieldErrors("facultyCode")
                && facultyService.codeExists(faculty.getFacultyCode())) {
            result.rejectValue("facultyCode", "duplicate", "This faculty code already exists");
        }
        if (result.hasErrors()) {
            model.addAttribute("facultyList", facultyService.findAll());
            return "admin/faculty";
        }
        if (faculty.getId() == null) {
            facultyService.create(faculty);
            redirect.addFlashAttribute("success", "Faculty " + faculty.getFacultyCode().toUpperCase()
                    + " added. Login password: Welcome@123");
        } else {
            facultyService.update(faculty.getId(), faculty);
            redirect.addFlashAttribute("success", "Faculty details updated");
        }
        return "redirect:/admin/faculty";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        facultyService.delete(id);
        redirect.addFlashAttribute("success", "Faculty removed");
        return "redirect:/admin/faculty";
    }
}

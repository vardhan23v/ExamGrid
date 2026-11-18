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

import com.examgrid.entity.Exam;
import com.examgrid.service.ExamService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/exams")
public class ExamWebController {

    private final ExamService examService;

    public ExamWebController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("exam", new Exam());
        model.addAttribute("exams", examService.findAll());
        return "admin/exams";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("exam", examService.findById(id));
        model.addAttribute("exams", examService.findAll());
        return "admin/exams";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("exam") Exam exam, BindingResult result,
                       Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            String problem = examService.findTimingProblem(exam);
            if (problem != null) {
                result.rejectValue("endTime", "timing", problem);
            }
        }
        if (result.hasErrors()) {
            model.addAttribute("exams", examService.findAll());
            return "admin/exams";
        }
        if (exam.getId() == null) {
            examService.save(exam);
            redirect.addFlashAttribute("success", "Exam " + exam.getSubjectCode().toUpperCase() + " scheduled");
        } else {
            examService.update(exam.getId(), exam);
            redirect.addFlashAttribute("success", "Exam updated. Re-run seating for its slot.");
        }
        return "redirect:/admin/exams";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        examService.delete(id);
        redirect.addFlashAttribute("success", "Exam removed");
        return "redirect:/admin/exams";
    }
}

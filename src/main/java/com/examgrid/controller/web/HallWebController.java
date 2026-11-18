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

import com.examgrid.entity.Hall;
import com.examgrid.service.HallService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/halls")
public class HallWebController {

    private final HallService hallService;

    public HallWebController(HallService hallService) {
        this.hallService = hallService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("hall", new Hall());
        model.addAttribute("halls", hallService.findAll());
        return "admin/halls";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("hall", hallService.findById(id));
        model.addAttribute("halls", hallService.findAll());
        return "admin/halls";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("hall") Hall hall, BindingResult result,
                       Model model, RedirectAttributes redirect) {
        if (hall.getId() == null && !result.hasFieldErrors("hallName") && hallService.nameExists(hall.getHallName())) {
            result.rejectValue("hallName", "duplicate", "A hall with this name already exists");
        }
        if (result.hasErrors()) {
            model.addAttribute("halls", hallService.findAll());
            return "admin/halls";
        }
        if (hall.getId() == null) {
            hallService.create(hall);
            redirect.addFlashAttribute("success", "Hall " + hall.getHallName().toUpperCase() + " added");
        } else {
            hallService.update(hall.getId(), hall);
            redirect.addFlashAttribute("success", "Hall updated");
        }
        return "redirect:/admin/halls";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        hallService.delete(id);
        redirect.addFlashAttribute("success", "Hall removed");
        return "redirect:/admin/halls";
    }
}

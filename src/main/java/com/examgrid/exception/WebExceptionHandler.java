package com.examgrid.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice(basePackages = "com.examgrid.controller.web")
public class WebExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        return showError(model, 404, "Record not found", ex.getMessage());
    }

    @ExceptionHandler(InsufficientCapacityException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public String handleCapacity(InsufficientCapacityException ex, Model model) {
        return showError(model, 422, "Insufficient hall capacity",
                ex.getMessage() + ". Add more halls or increase rows/columns, then run the allocation again.");
    }

    @ExceptionHandler(AllocationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public String handleAllocation(AllocationException ex, Model model) {
        return showError(model, 422, "Allocation could not be completed", ex.getMessage());
    }

    @ExceptionHandler({InvalidRequestException.class, DuplicateResourceException.class,
            MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(Exception ex, Model model) {
        return showError(model, 400, "Invalid request", ex.getMessage());
    }

    private String showError(Model model, int status, String title, String message) {
        model.addAttribute("status", status);
        model.addAttribute("title", title);
        model.addAttribute("message", message);
        return "error/app-error";
    }
}

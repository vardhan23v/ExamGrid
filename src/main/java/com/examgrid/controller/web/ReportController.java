package com.examgrid.controller.web;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.examgrid.service.ReportPdfService;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    private final ReportPdfService reportPdfService;

    public ReportController(ReportPdfService reportPdfService) {
        this.reportPdfService = reportPdfService;
    }

    @GetMapping("/seating")
    public ResponseEntity<byte[]> seatingChart(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                               @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime) {
        byte[] pdf = reportPdfService.seatingChart(date, startTime);
        String fileName = "seating_" + date + "_" + startTime.toString().replace(":", "") + ".pdf";
        return pdfResponse(pdf, fileName);
    }

    @GetMapping("/duties")
    public ResponseEntity<byte[]> dutyRoster(@RequestParam(required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        byte[] pdf = reportPdfService.dutyRoster(date);
        String fileName = date == null ? "duty_roster_all.pdf" : "duty_roster_" + date + ".pdf";
        return pdfResponse(pdf, fileName);
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String fileName) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

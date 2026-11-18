package com.examgrid.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.examgrid.dto.DutyInfo;
import com.examgrid.dto.HallLayout;
import com.examgrid.dto.HallLayout.SeatCell;
import com.examgrid.dto.SeatInfo;
import com.examgrid.exception.AllocationException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class ReportPdfService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a");
    private static final Color HEADER_BG = new Color(13, 59, 102);
    private static final Color[] SUBJECT_COLOURS = {
            new Color(207, 226, 255), new Color(209, 231, 221), new Color(255, 243, 205),
            new Color(248, 215, 218), new Color(226, 217, 243), new Color(207, 244, 252)
    };

    private final SeatingAllocationService seatingAllocationService;
    private final InvigilationAllocationService invigilationAllocationService;

    public ReportPdfService(SeatingAllocationService seatingAllocationService,
                            InvigilationAllocationService invigilationAllocationService) {
        this.seatingAllocationService = seatingAllocationService;
        this.invigilationAllocationService = invigilationAllocationService;
    }

    public byte[] seatingChart(LocalDate date, LocalTime startTime) {
        List<HallLayout> layouts = seatingAllocationService.buildHallLayouts(date, startTime);
        if (layouts.isEmpty()) {
            throw new AllocationException("No seating plan exists for " + date + " " + startTime);
        }
        List<SeatInfo> seats = seatingAllocationService.findSlotSeats(date, startTime);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            boolean firstPage = true;
            for (HallLayout hall : layouts) {
                if (!firstPage) {
                    document.newPage();
                }
                firstPage = false;
                addTitle(document, "Seating Chart - " + hall.hallName() + " (" + hall.block() + ")",
                        "Date: " + date.format(DATE) + "    Time: " + startTime.format(TIME)
                                + "    Students: " + hall.occupied() + " / " + (hall.rows() * hall.columns()));

                PdfPTable grid = new PdfPTable(hall.columns());
                grid.setWidthPercentage(100);
                for (List<SeatCell> row : hall.grid()) {
                    for (SeatCell cell : row) {
                        String text = cell.isEmpty() ? cell.label() + "\n-" : cell.label() + "\n" + cell.usn() + "\n" + cell.subjectCode();
                        PdfPCell pdfCell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 8)));
                        pdfCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfCell.setPadding(4);
                        pdfCell.setMinimumHeight(38);
                        if (!cell.isEmpty()) {
                            pdfCell.setBackgroundColor(SUBJECT_COLOURS[cell.colorIndex() % SUBJECT_COLOURS.length]);
                        }
                        grid.addCell(pdfCell);
                    }
                }
                document.add(grid);

                document.add(new Paragraph("Black board / Invigilator desk",
                        FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9)));
                document.newPage();
                addTitle(document, "Attendance Sheet - " + hall.hallName(),
                        "Date: " + date.format(DATE) + "    Time: " + startTime.format(TIME));

                PdfPTable list = new PdfPTable(new float[]{1, 2, 4, 2, 2, 3});
                list.setWidthPercentage(100);
                addHeader(list, "Sl", "Seat", "Name", "USN", "Subject", "Signature");
                int sl = 1;
                for (SeatInfo seat : seats) {
                    if (!seat.hallName().equals(hall.hallName())) {
                        continue;
                    }
                    addRow(list, String.valueOf(sl++), seat.seatNumber(), seat.studentName(), seat.usn(),
                            seat.subjectCode(), "");
                }
                document.add(list);
            }
            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Could not create seating chart PDF", e);
        }
    }

    public byte[] dutyRoster(LocalDate date) {
        List<DutyInfo> duties = date == null
                ? invigilationAllocationService.findAllDuties()
                : invigilationAllocationService.findDutiesByDate(date);
        if (duties.isEmpty()) {
            throw new AllocationException("No invigilation duties have been assigned yet");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            addTitle(document, "Invigilation Duty Roster",
                    date == null ? "All exam days" : "Date: " + date.format(DATE));

            PdfPTable table = new PdfPTable(new float[]{1, 2, 3, 4, 2, 2, 3});
            table.setWidthPercentage(100);
            addHeader(table, "Sl", "Date", "Time", "Faculty", "Dept", "Hall", "Signature");
            int sl = 1;
            for (DutyInfo duty : duties) {
                addRow(table, String.valueOf(sl++), duty.examDate().format(DATE),
                        duty.startTime().format(TIME) + " - " + duty.endTime().format(TIME),
                        duty.facultyName() + " (" + duty.facultyCode() + ")",
                        duty.department(), duty.hallName(), "");
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Could not create duty roster PDF", e);
        }
    }

    private void addTitle(Document document, String title, String subtitle) throws DocumentException {
        Paragraph college = new Paragraph("NMAM Institute of Technology, Nitte - Examination Cell",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, HEADER_BG));
        college.setAlignment(Element.ALIGN_CENTER);
        document.add(college);

        Paragraph heading = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
        heading.setAlignment(Element.ALIGN_CENTER);
        document.add(heading);

        Paragraph info = new Paragraph(subtitle, FontFactory.getFont(FontFactory.HELVETICA, 10));
        info.setAlignment(Element.ALIGN_CENTER);
        info.setSpacingAfter(10);
        document.add(info);
    }

    private void addHeader(PdfPTable table, String... headings) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        for (String heading : headings) {
            PdfPCell cell = new PdfPCell(new Phrase(heading, font));
            cell.setBackgroundColor(HEADER_BG);
            cell.setPadding(5);
            table.addCell(cell);
        }
        table.setHeaderRows(1);
    }

    private void addRow(PdfPTable table, String... values) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value, font));
            cell.setPadding(5);
            cell.setMinimumHeight(18);
            table.addCell(cell);
        }
    }
}

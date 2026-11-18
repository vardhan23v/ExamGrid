package com.examgrid.dto;

import java.util.List;

public record HallLayout(Long hallId,
                         String hallName,
                         String block,
                         int rows,
                         int columns,
                         int occupied,
                         List<List<SeatCell>> grid) {

    public record SeatCell(String label, String usn, String subjectCode, int colorIndex) {

        public boolean isEmpty() {
            return usn == null;
        }
    }
}

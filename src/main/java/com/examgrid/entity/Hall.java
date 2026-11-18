package com.examgrid.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "halls")
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Hall name is required")
    @Column(nullable = false, unique = true, length = 30)
    private String hallName;

    @NotBlank(message = "Block is required")
    @Column(nullable = false, length = 30)
    private String block;

    @Min(value = 1, message = "Rows must be at least 1")
    @Max(value = 30, message = "Rows cannot exceed 30")
    @Column(name = "total_rows")
    private int numberOfRows;

    @Min(value = 1, message = "Columns must be at least 1")
    @Max(value = 20, message = "Columns cannot exceed 20")
    @Column(name = "total_columns")
    private int numberOfColumns;

    public Hall() {
    }

    public Hall(String hallName, String block, int numberOfRows, int numberOfColumns) {
        this.hallName = hallName;
        this.block = block;
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
    }

    public int getCapacity() {
        return numberOfRows * numberOfColumns;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public void setNumberOfColumns(int numberOfColumns) {
        this.numberOfColumns = numberOfColumns;
    }
}

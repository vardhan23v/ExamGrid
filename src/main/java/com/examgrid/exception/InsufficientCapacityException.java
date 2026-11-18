package com.examgrid.exception;

public class InsufficientCapacityException extends RuntimeException {

    private final int required;
    private final int available;

    public InsufficientCapacityException(int required, int available) {
        super("Insufficient hall capacity: " + required + " students need seats but only "
                + available + " seats are available");
        this.required = required;
        this.available = available;
    }

    public int getRequired() {
        return required;
    }

    public int getAvailable() {
        return available;
    }
}

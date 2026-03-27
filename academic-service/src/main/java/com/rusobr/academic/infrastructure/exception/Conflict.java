package com.rusobr.academic.infrastructure.exception;

public class Conflict extends RuntimeException {
    public Conflict(String message) {
        super(message);
    }
}

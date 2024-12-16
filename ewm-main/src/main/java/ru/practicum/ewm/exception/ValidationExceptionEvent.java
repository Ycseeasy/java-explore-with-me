package ru.practicum.ewm.exception;

public class ValidationExceptionEvent extends RuntimeException {
    public ValidationExceptionEvent(String message) {
        super(message);
    }
}
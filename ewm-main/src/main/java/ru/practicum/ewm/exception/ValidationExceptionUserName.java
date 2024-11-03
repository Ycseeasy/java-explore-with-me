package ru.practicum.ewm.exception;

public class ValidationExceptionUserName extends RuntimeException {
    public ValidationExceptionUserName(String message) {
        super(message);
    }
}
package ru.practicum.ewm.exception;

public class OverflowLimitException extends RuntimeException {
    public OverflowLimitException(String message) {
        super(message);
    }
}
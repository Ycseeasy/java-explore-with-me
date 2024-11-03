package ru.practicum.ewm.exception;

public class RepeatParticipationRequestException extends RuntimeException {
    public RepeatParticipationRequestException(String message) {
        super(message);
    }
}
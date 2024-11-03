package ru.practicum.ewm.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
public class ApiError  {
    private final List<String> errors;
    private final String message;
    private final String reason;
    private final HttpStatus status;
    private final String timestamp;
}
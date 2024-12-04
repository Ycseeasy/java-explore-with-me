package ru.practicum.ewm.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class ErrorHandler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgumentException(IllegalArgumentException ex) throws IOException {
        return buildApiError(List.of(), HttpStatus.BAD_REQUEST, "Некорректный запрос", ex.getLocalizedMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField());
        }
        return buildApiError(errors, HttpStatus.BAD_REQUEST, "Запрашиваемый объект не найден", ex.getLocalizedMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getMessage());
        }
        return buildApiError(errors, HttpStatus.BAD_REQUEST, "Запрашиваемый объект не найден", ex.getLocalizedMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) throws IOException {
        return buildApiError(Collections.singletonList(getStackTraceAsString(e)), HttpStatus.NOT_FOUND, "Запрашиваемый объект не найден", e.getLocalizedMessage());
    }

    @ExceptionHandler(ValidationExceptionFindCategory.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleValidationExceptionFindCategory(ValidationExceptionFindCategory e) throws IOException {
        return buildApiError(Collections.singletonList(getStackTraceAsString(e)), HttpStatus.CONFLICT, "Для запрошенной операции условия не выполнены", e.getLocalizedMessage());
    }

    @ExceptionHandler(DuplicateNameException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDuplicateNameException(DuplicateNameException e) throws IOException {
        return buildApiError(Collections.singletonList(getStackTraceAsString(e)), HttpStatus.CONFLICT, "Произошло дублирование имени", e.getLocalizedMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDuplicateEmailException(DuplicateEmailException e) throws IOException {
        return buildApiError(Collections.singletonList(getStackTraceAsString(e)), HttpStatus.CONFLICT, "Произошло дублирование почты", e.getLocalizedMessage());
    }

    @ExceptionHandler(EventDateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleEventDateException(EventDateException e) throws IOException {
        return buildApiError(Collections.singletonList(getStackTraceAsString(e)), HttpStatus.CONFLICT, "Некорректное время", e.getLocalizedMessage());
    }

    @ExceptionHandler(StateArgumentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleStateArgumentException(StateArgumentException e) throws IOException {
        return buildApiError(Collections.singletonList(getStackTraceAsString(e)), HttpStatus.CONFLICT, "Запрашиваемый объект не найден", e.getLocalizedMessage());
    }

    @ExceptionHandler(OverflowLimitException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleOverflowLimitException(OverflowLimitException e) throws IOException {
        return buildApiError(Collections.singletonList(getStackTraceAsString(e)), HttpStatus.CONFLICT, "Достигнут лимит памяти", e.getLocalizedMessage());
    }

    @ExceptionHandler(StatusParticipationRequestException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleStatusParticipationRequestException(StatusParticipationRequestException e) throws IOException {
        return buildApiError(Collections.singletonList(getStackTraceAsString(e)), HttpStatus.CONFLICT, "Получен статус NOT PENDING", e.getLocalizedMessage());
    }

    @ExceptionHandler(RepeatParticipationRequestException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleRepeatParticipationRequestException(RepeatParticipationRequestException e) throws IOException {
        return buildApiError(Collections.singletonList(getStackTraceAsString(e)), HttpStatus.CONFLICT, "Повтор запроса", e.getLocalizedMessage());
    }

    private ApiError buildApiError(List<String> errors, HttpStatus status, String reason, String message) {
        return ApiError.builder().errors(errors).status(status).reason(reason).message(message).timestamp(LocalDateTime.now().format(DATE_TIME_FORMATTER)).build();
    }

    private String getStackTraceAsString(Exception e) throws IOException {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            return sw.toString();
        }
    }
}

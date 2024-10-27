package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.practicum.ewm.location.model.Location;

@Data
public class NewEventDto {

    @NotNull
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    @Positive
    private Long category;

    @NotNull
    @NotBlank
    @Size(min = 20, max = 2000)
    private String description;

    @NotNull
    @NotBlank
    private String eventDate;

    @NotNull
    private Location location;

    private Boolean paid;

    @Min(0)
    private Integer participantLimit;

    private Boolean requestModeration;

    @NotNull
    @NotBlank
    @Size(min = 3, max = 120)
    private String title;
}
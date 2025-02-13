package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.practicum.ewm.location.model.Location;

@Data
public class NewEventDto {

    @NotNull
    @NotBlank
    @Size(min = 20, message = "annotation size must be more 20 characters")
    @Size(max = 2000, message = "annotation size must be less 2000 characters")
    private String annotation;

    @Positive
    private Long category;

    @NotNull
    @NotBlank
    @Size(min = 20, message = "description size must be more 20 characters")
    @Size(max = 7000, message = "description size must be less 7000 characters")
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
    @Size(min = 3, message = "title size must be more 3 characters")
    @Size(max = 120, message = "title size must be less 120 characters")
    private String title;
}
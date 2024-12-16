package ru.practicum.ewm.event.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.ewm.event.model.StateAction;
import ru.practicum.ewm.location.model.Location;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventUserRequest {

    @Size(min = 20, message = "annotation size must be more 20 characters")
    @Size(max = 2000, message = "annotation size must be less 7000 characters")
    private String annotation;

    @Positive
    private Long category;

    @Size(min = 20, message = "description size must be more 20 characters")
    @Size(max = 7000, message = "description size must be less 7000 characters")
    private String description;

    private String eventDate;

    private Location location;

    private Boolean paid;

    @Positive
    private Integer participantLimit;

    private Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    private StateAction stateAction;

    @Size(min = 3, message = "title size must be more 3 characters")
    @Size(max = 120, message = "title size must be less 120 characters")
    private String title;
}

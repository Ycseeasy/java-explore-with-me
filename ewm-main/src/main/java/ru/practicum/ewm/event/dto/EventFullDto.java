package ru.practicum.ewm.event.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.user.dto.UserShortDto;

@Data
@Builder
public class EventFullDto {

    private Long id;
    private String title;
    private String annotation;
    private CategoryDto category;
    private Boolean paid;
    private String eventDate;
    private UserShortDto initiator;
    private Long confirmedRequests;
    private String description;
    private Integer participantLimit;
    private String state;
    private String createdOn;
    private Location location;
    private Boolean requestModeration;
    private String publishedOn;
    private Long views;
}
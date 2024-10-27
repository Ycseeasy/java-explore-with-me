package ru.practicum.ewm.event.mapper;

import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public static EventFullDto toEventFullDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn().format(DATE_TIME_FORMATTER))
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(DATE_TIME_FORMATTER))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(event.getLocation()).paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn() == null ? null : event.getPublishedOn().format(DATE_TIME_FORMATTER))
                .requestModeration(event.getRequestModeration())
                .state(event.getState() == null ? null : event.getState().toString())
                .title(event.getTitle()).views(event.getViews())
                .build();
    }

    public static EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate().format(DATE_TIME_FORMATTER))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid()).title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static Event toEvent(NewEventDto newEventDto, User initiator, Category category) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .createdOn(LocalDateTime.now())
                .description(newEventDto.getDescription())
                .eventDate(LocalDateTime.parse(newEventDto.getEventDate(), DATE_TIME_FORMATTER))
                .initiator(initiator)
                .location(newEventDto.getLocation())
                .confirmedRequests(0L)
                .paid(newEventDto.getPaid() != null && newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration() == null || newEventDto.getRequestModeration())
                .state(State.PENDING)
                .title(newEventDto.getTitle())
                .views(0L)
                .build();
    }

    public static Event toEvent(UpdateEventAdminRequest updateEventAdminRequest, Event oldEvent, Category category) {
        return Event.builder()
                .id(oldEvent.getId())
                .annotation(updateEventAdminRequest.getAnnotation() == null ? oldEvent.getAnnotation() : updateEventAdminRequest.getAnnotation())
                .category(updateEventAdminRequest.getCategory() == null ? oldEvent.getCategory() : category)
                .confirmedRequests(oldEvent.getConfirmedRequests())
                .createdOn(oldEvent.getCreatedOn())
                .description(updateEventAdminRequest.getDescription() == null ? oldEvent.getDescription() : updateEventAdminRequest.getDescription())
                .eventDate(updateEventAdminRequest.getEventDate() == null ? oldEvent.getEventDate() : LocalDateTime.parse(updateEventAdminRequest.getEventDate(), DATE_TIME_FORMATTER))
                .initiator(oldEvent.getInitiator())
                .location(updateEventAdminRequest.getLocation() == null ? oldEvent.getLocation() : updateEventAdminRequest.getLocation())
                .paid(updateEventAdminRequest.getPaid() == null ? oldEvent.getPaid() : updateEventAdminRequest.getPaid())
                .participantLimit(updateEventAdminRequest.getParticipantLimit() == null ? oldEvent.getParticipantLimit() : updateEventAdminRequest.getParticipantLimit())
                .requestModeration(updateEventAdminRequest.getRequestModeration() == null ? oldEvent.getRequestModeration() : updateEventAdminRequest.getRequestModeration())
                .state(oldEvent.getState())
                .title(updateEventAdminRequest.getTitle() == null ? oldEvent.getTitle() : updateEventAdminRequest.getTitle())
                .views(oldEvent.getViews())
                .build();
    }

    public static Event toEvent(UpdateEventUserRequest updateEventUserRequest, Event oldEvent, Category category) {
        return Event.builder()
                .id(oldEvent.getId())
                .annotation(updateEventUserRequest.getAnnotation() == null ? oldEvent.getAnnotation() : updateEventUserRequest.getAnnotation())
                .category(updateEventUserRequest.getCategory() == null ? oldEvent.getCategory() : category)
                .confirmedRequests(oldEvent.getConfirmedRequests())
                .createdOn(oldEvent.getCreatedOn())
                .description(updateEventUserRequest.getDescription() == null ? oldEvent.getDescription() : updateEventUserRequest.getDescription())
                .eventDate(updateEventUserRequest.getEventDate() == null ? oldEvent.getEventDate() : LocalDateTime.parse(updateEventUserRequest.getEventDate(), DATE_TIME_FORMATTER))
                .initiator(oldEvent.getInitiator())
                .location(updateEventUserRequest.getLocation() == null ? oldEvent.getLocation() : updateEventUserRequest.getLocation())
                .paid(updateEventUserRequest.getPaid() == null ? oldEvent.getPaid() : updateEventUserRequest.getPaid())
                .participantLimit(updateEventUserRequest.getParticipantLimit() == null ? oldEvent.getParticipantLimit() : updateEventUserRequest.getParticipantLimit())
                .requestModeration(updateEventUserRequest.getRequestModeration() == null ? oldEvent.getRequestModeration() : updateEventUserRequest.getRequestModeration())
                .title(updateEventUserRequest.getTitle() == null ? oldEvent.getTitle() : updateEventUserRequest.getTitle())
                .views(oldEvent.getViews())
                .build();

    }

}
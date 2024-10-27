package ru.practicum.ewm.controllers.priv;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/events")
@Validated
@RequiredArgsConstructor
public class EventPrivateController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(HttpServletRequest request, @NotNull @Positive @PathVariable(required = false) Long userId, @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from, @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return eventService.getEventsPrivate(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(HttpServletRequest request, @Positive @PathVariable Long userId, @NotNull @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.addEventPrivate(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(HttpServletRequest request, @Positive @PathVariable(required = false) Long userId, @Positive @PathVariable(required = false) Long eventId) {
        return eventService.getEventPrivate(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventUserRequest(HttpServletRequest request, @Positive @PathVariable(required = false) Long userId, @Positive @PathVariable(required = false) Long eventId, @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        return eventService.updateEventPrivate(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsEventsUser(HttpServletRequest request, @Positive @PathVariable Long userId, @Positive @PathVariable Long eventId) {
        return eventService.getRequestsEventsUserPrivate(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequestStatus(HttpServletRequest request, @Positive @PathVariable Long userId, @Positive @PathVariable Long eventId, @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        return eventService.updateEventRequestStatusPrivate(userId, eventId, eventRequestStatusUpdateRequest);
    }
}
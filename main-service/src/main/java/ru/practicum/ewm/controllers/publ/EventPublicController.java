package ru.practicum.ewm.controllers.publ;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
@Validated
@RequiredArgsConstructor
public class EventPublicController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsAndStatsPublic(HttpServletRequest request,
                                                       @RequestParam(required = false) String text,
                                                       @RequestParam(required = false) List<Long> categories,
                                                       @RequestParam(required = false) Boolean paid,
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                       @RequestParam(required = false) LocalDateTime rangeStart,
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                       @RequestParam(required = false) LocalDateTime rangeEnd,
                                                       @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                       @RequestParam(required = false) String sort,
                                                       @PositiveOrZero
                                                       @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                       @Positive
                                                       @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return eventService.getEventsAndStatsPublic(request,
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size);
    }

    @GetMapping("/{Id}")
    public EventFullDto getEventByIdAndStatsPublic(HttpServletRequest request,
                                                   @Positive @PathVariable("Id") Long eventId) {
        return eventService.getEventByIdAndStatsPublic(request, eventId);
    }

}
package ru.practicum.ewm.event.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.ewm.dto.stats.EndpointHitDto;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.model.Status;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.*;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.repository.LocationRepository;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.participation.mapper.ParticipationMapper;
import ru.practicum.ewm.participation.model.ParticipationRequest;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;

    @Transactional
    @Override
    public List<EventShortDto> getEventsPrivate(Long userId, Integer from, Integer size) {
        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);
        return eventRepository.getEventsByInitiatorId(userId, pageable).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventFullDto addEventPrivate(Long userId, NewEventDto newEventDto) {
        LocalDateTime start = LocalDateTime.parse(newEventDto.getEventDate(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        if (start.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IllegalArgumentException("Incorrectly time");
        }
        if (newEventDto.getParticipantLimit() == null) {
            newEventDto.setParticipantLimit(0);
        }
        locationRepository.save(newEventDto.getLocation());
        Category category = categoryRepository.findById(newEventDto.getCategory()).orElse(null);
        User user = userRepository.getUserById(userId);
        return EventMapper.toEventFullDto(eventRepository.save(EventMapper.toEvent(newEventDto, user, category)));
    }

    @Transactional
    @Override
    public EventFullDto getEventPrivate(Long userId, Long eventId) {
        return EventMapper.toEventFullDto(eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId
                        + " and event with ID " + eventId + " not found.")));
    }


    @Transactional
    @Override
    public EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event oldEvent = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId
                        + " and event with ID " + eventId + " not found."));

        validateUpdateEventPrivate(oldEvent, updateEventUserRequest);

        if (updateEventUserRequest.getLocation() != null) {
            Location location = locationRepository.save(updateEventUserRequest.getLocation());
            updateEventUserRequest.setLocation(location);
        }

        Category newCategory;
        if (updateEventUserRequest.getCategory() != null) {
            newCategory = categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with ID " + updateEventUserRequest.getCategory()
                            + " not found."));
        } else {
            newCategory = oldEvent.getCategory();
        }
        Event upEvent = oldEvent;
        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction().equals("SEND_TO_REVIEW")) {
                upEvent = EventMapper.toEvent(updateEventUserRequest, oldEvent, newCategory);
                upEvent.setState(State.PENDING);
            }
            if (updateEventUserRequest.getStateAction().equals("CANCEL_REVIEW")) {
                upEvent.setState(State.CANCELED);

            }
        }

        upEvent.setId(eventId);

        return EventMapper.toEventFullDto(eventRepository.save(upEvent));
    }

    private void validateUpdateEventPrivate(@NotNull(message = "Event not found") Event oldEvent,
                                            UpdateEventUserRequest updateEventUserRequest) {

        if (oldEvent.getState() != null && oldEvent.getState().equals(State.PUBLISHED)) {
            throw new StateArgumentException("Event with state PENDING or CANCELED can be changed. This Event state is "
                    + oldEvent.getState());
        }

        LocalDateTime start = oldEvent.getEventDate();
        if (updateEventUserRequest.getEventDate() != null) {
            if (LocalDateTime.parse(updateEventUserRequest.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).isBefore(start.plusHours(2))) {
                throw new IllegalArgumentException("Start time " + start + " is earlier or equally Event Date");
            }
        }
    }

    @Transactional
    @Override
    public List<ParticipationRequestDto> getRequestsEventsUserPrivate(Long userId, Long eventId) {
        return participationRepository.getParticipationRequestsByEvent(eventId).stream()
                .map(ParticipationMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateEventRequestStatusPrivate(Long userId,
                                                                          Long eventId,
                                                                          EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId
                        + " and event with ID " + eventId + " not found."));

        Status status = eventRequestStatusUpdateRequest.getStatus();
        List<ParticipationRequest> participationRequests = participationRepository
                .findByIdIn(eventRequestStatusUpdateRequest.getRequestIds());

        if (event.getParticipantLimit() == 0 && !event.getRequestModeration()) {
            return buildResult(new ArrayList<>(), new ArrayList<>());
        }

        if (event.getConfirmedRequests() != null && event.getParticipantLimit() > 0
                && event.getConfirmedRequests().equals(Long.valueOf(event.getParticipantLimit()))) {
            throw new OverflowLimitException("Too many requests");
        }

        if (!event.getRequestModeration()) {
            return processRequestsWithoutModeration(event, status, participationRequests);
        }

        return processRequestsWithModeration(event, status, participationRequests);
    }


    @Transactional
    @Override
    public List<EventFullDto> getEventsAdmin(List<Long> users,
                                             List<String> states,
                                             List<Long> categories,
                                             LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd,
                                             Integer from, Integer size) {

        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);


        List<State> stateEnum = (states != null) ? states.stream().map(State::valueOf).collect(Collectors.toList()) : null;


        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new IllegalArgumentException("Start date " + rangeStart
                    + " cannot be later than end date " + rangeEnd);
        }


        Specification<Event> specification = buildSpecification(users, stateEnum, categories, rangeStart, rangeEnd);


        return eventRepository.findAll(specification, pageable).stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    private Specification<Event> buildSpecification(List<Long> users,
                                                    List<State> states,
                                                    List<Long> categories,
                                                    LocalDateTime start,
                                                    LocalDateTime end) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Добавляем условия фильтрации, если параметры заданы
            if (users != null) {
                predicates.add(root.get("initiator").get("id").in(users));
            }
            if (states != null) {
                predicates.add(root.get("state").in(states));
            }
            if (categories != null) {
                predicates.add(root.get("category").get("id").in(categories));
            }
            if (start != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), start));
            }
            if (end != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), end));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event oldEvent = eventRepository.getEventsById(eventId);

        validateUpdateEventAdmin(oldEvent, updateEventAdminRequest);

        if (updateEventAdminRequest.getLocation() != null) {
            Location location = locationRepository.save(updateEventAdminRequest.getLocation());
            updateEventAdminRequest.setLocation(location);
        }

        Category newCategory;
        if (updateEventAdminRequest.getCategory() != null) {
            newCategory = categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with ID " + updateEventAdminRequest.getCategory()
                            + " not found."));
        } else {
            newCategory = oldEvent.getCategory();
        }

        Event upEvent = oldEvent;
        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction().equals("PUBLISH_EVENT")) {
                upEvent = EventMapper.toEvent(updateEventAdminRequest, oldEvent, newCategory);
                upEvent.setPublishedOn(LocalDateTime.now());
                upEvent.setState(State.PUBLISHED);
            }
            if (updateEventAdminRequest.getStateAction().equals("REJECT_EVENT")) {
                upEvent.setState(State.CANCELED);

            }
        }
        upEvent.setId(eventId);

        return EventMapper.toEventFullDto(eventRepository.save(upEvent));
    }


    private void validateUpdateEventAdmin(Event oldEvent, UpdateEventAdminRequest updateEventAdminRequest) {
        if (oldEvent == null) {
            throw new NotFoundException("The required object was not found.");
        }

        LocalDateTime start = oldEvent.getEventDate();
        if (oldEvent.getPublishedOn() != null && start.isAfter(oldEvent.getPublishedOn().plusHours(1))) {
            throw new EventDateException("Time start" + start + "before eventDate + 1 Hours");
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            LocalDateTime newEventDate = LocalDateTime.parse(updateEventAdminRequest.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime currentTime = LocalDateTime.now();
            if (newEventDate.isBefore(currentTime) || newEventDate.isEqual(currentTime)) {
                throw new IllegalArgumentException("Time start" + start + "before or equals eventDate");
            }
        }

        if (oldEvent.getState() != null && !oldEvent.getState().equals(State.PENDING)
                && updateEventAdminRequest.getStateAction().equals("PUBLISH_EVENT")) {
            throw new StateArgumentException("Cannot publish the event because it's not in the right state:" +
                    " PUBLISHED OR CANCELED");
        }
        if (oldEvent.getState() != null && oldEvent.getState().equals(State.PUBLISHED)
                && updateEventAdminRequest.getStateAction().equals("REJECT_EVENT")) {
            throw new StateArgumentException("Cannot reject the event because it's not in the right state: PUBLISHED");
        }
    }

    @Transactional
    @Override
    public List<EventShortDto> getEventsAndStatsPublic(HttpServletRequest request,
                                                       String text,
                                                       List<Long> categories,
                                                       Boolean paid,
                                                       LocalDateTime rangeStart,
                                                       LocalDateTime rangeEnd,
                                                       Boolean onlyAvailable,
                                                       String sort, Integer from, Integer size) {
        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);
        LocalDateTime timeNow = LocalDateTime.now();

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new IllegalArgumentException("Time start " + rangeStart + " after end " + rangeEnd);
        }


        String textPattern = (text != null) ? "%" + text + "%" : null;

        List<Event> list;


        if (rangeStart == null && rangeEnd == null) {
            list = fetchEventsNoPeriod(State.PUBLISHED.toString(),
                    categories,
                    paid,
                    textPattern,
                    timeNow,
                    onlyAvailable,
                    sort,
                    pageable);
        } else {
            list = fetchEventsWithPeriod(State.PUBLISHED.toString(),
                    categories,
                    paid,
                    textPattern,
                    rangeStart,
                    rangeEnd,
                    onlyAvailable,
                    sort,
                    pageable);
        }

        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .id(null)
                .app("main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(timeNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();

        try {
            statsClient.addRequest(endpointHitDto);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(e.getLocalizedMessage());
        }

        if (list.isEmpty()) {
            return new ArrayList<>();
        }

        return list.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }


    private List<Event> fetchEventsNoPeriod(String state,
                                            List<Long> categories,
                                            Boolean paid,
                                            String text,
                                            LocalDateTime timeNow,
                                            Boolean onlyAvailable,
                                            String sort, Pageable pageable) {
        if ("EVENT_DATE".equals(sort)) {
            return onlyAvailable ? fetchSortedByEventDateAvailableNoPeriod(state, categories, paid, text, timeNow, pageable) : fetchSortedByEventDateNoPeriod(state, categories, paid, text, timeNow, pageable);
        } else if ("VIEWS".equals(sort)) {
            return onlyAvailable ? fetchSortedByViewsAvailableNoPeriod(state, categories, paid, text, timeNow, pageable) : fetchSortedByViewsNoPeriod(state, categories, paid, text, timeNow, pageable);
        } else {
            return onlyAvailable ? fetchAvailableNoPeriod(state, categories, paid, text, timeNow, pageable) : fetchDefaultNoPeriod(state, categories, paid, text, timeNow, pageable);
        }
    }


    private List<Event> fetchSortedByEventDateAvailableNoPeriod(String state,
                                                                List<Long> categories,
                                                                Boolean paid,
                                                                String text,
                                                                LocalDateTime timeNow,
                                                                Pageable pageable) {
        if (categories != null && text != null) {
            return eventRepository.getEventsNoPeriodSortEventDateAvailableCategoryText(state, categories, timeNow, text, pageable);
        } else if (text == null && categories != null) {
            return eventRepository.getEventsNoPeriodSortEventDateAvailableCategory(state, categories, timeNow, pageable);
        } else if (text != null) {
            return eventRepository.getEventsNoPeriodSortEventDateAvailableText(state, timeNow, text, pageable);
        } else {
            return eventRepository.getEventsNoPeriodSortEventDateAvailable(state, timeNow, pageable);
        }
    }

    private List<Event> fetchSortedByEventDateNoPeriod(String state,
                                                       List<Long> categories,
                                                       Boolean paid,
                                                       String text,
                                                       LocalDateTime timeNow,
                                                       Pageable pageable) {
        if (categories != null && text != null) {
            return eventRepository.getEventsNoPeriodSortEventDateCategoryText(state, categories, timeNow, text, pageable);
        } else if (text == null && categories != null) {
            return eventRepository.getEventsNoPeriodSortEventDateCategory(state, categories, timeNow, pageable);
        } else if (text != null) {
            return eventRepository.getEventsNoPeriodSortEventDateText(state, timeNow, text, pageable);
        } else {
            return eventRepository.getEventsNoPeriodSortEventDate(state, timeNow, pageable);
        }
    }

    private List<Event> fetchSortedByViewsAvailableNoPeriod(String state,
                                                            List<Long> categories,
                                                            Boolean paid,
                                                            String text,
                                                            LocalDateTime timeNow,
                                                            Pageable pageable) {
        if (categories != null && text != null) {
            return eventRepository.getEventsNoPeriodSortViewsAvailableCategoryText(state, categories, timeNow, text, pageable);
        } else if (categories != null) {
            return eventRepository.getEventsNoPeriodSortViewsAvailableCategory(state, categories, timeNow, pageable);
        } else if (text != null) {
            return eventRepository.getEventsNoPeriodSortViewsAvailableText(state, timeNow, text, pageable);
        } else {
            return eventRepository.getEventsNoPeriodSortViewsAvailable(state, timeNow, pageable);
        }
    }

    private List<Event> fetchSortedByViewsNoPeriod(String state,
                                                   List<Long> categories,
                                                   Boolean paid,
                                                   String text,
                                                   LocalDateTime timeNow,
                                                   Pageable pageable) {
        if (categories != null && text != null) {
            return eventRepository.getEventsNoPeriodSortViewsCategoryText(state, categories, timeNow, text, pageable);
        } else if (categories != null) {
            return eventRepository.getEventsNoPeriodSortViewsCategory(state, categories, timeNow, pageable);
        } else if (text != null) {
            return eventRepository.getEventsNoPeriodSortViewsText(state, timeNow, text, pageable);
        } else {
            return eventRepository.getEventsNoPeriodSortViews(state, timeNow, pageable);
        }
    }

    private List<Event> fetchAvailableNoPeriod(String state,
                                               List<Long> categories,
                                               Boolean paid,
                                               String text,
                                               LocalDateTime timeNow,
                                               Pageable pageable) {
        if (categories != null && text != null) {
            return eventRepository.getEventsNoPeriodAvailableCategoryText(state, categories, timeNow, text, pageable);
        } else if (categories != null) {
            return eventRepository.getEventsNoPeriodAvailableCategory(state, categories, timeNow, pageable);
        } else if (text != null) {
            return eventRepository.getEventsNoPeriodAvailableText(state, timeNow, text, pageable);
        } else {
            return eventRepository.getEventsNoPeriodAvailable(state, timeNow, pageable);
        }
    }

    private List<Event> fetchDefaultNoPeriod(String state,
                                             List<Long> categories,
                                             Boolean paid,
                                             String text,
                                             LocalDateTime timeNow,
                                             Pageable pageable) {
        if (categories != null && text != null) {
            return eventRepository.getEventsNoPeriodCategoryText(state, categories, timeNow, text, pageable);
        } else if (categories != null) {
            return eventRepository.getEventsNoPeriodCategory(state, categories, timeNow, pageable);
        } else if (text != null) {
            return eventRepository.getEventsNoPeriodText(state, timeNow, text, pageable);
        } else {
            return eventRepository.getEventsNoPeriod(state, timeNow, pageable);
        }
    }


    private List<Event> fetchEventsWithPeriod(String state,
                                              List<Long> categories,
                                              Boolean paid,
                                              String text,
                                              LocalDateTime rangeStart,
                                              LocalDateTime rangeEnd,
                                              Boolean onlyAvailable,
                                              String sort,
                                              Pageable pageable) {
        if ("EVENT_DATE".equals(sort)) {
            return fetchSortedByEventDateWithPeriod(state, categories, text, rangeStart, rangeEnd, onlyAvailable, pageable);
        } else if ("VIEWS".equals(sort)) {
            return fetchSortedByViewsWithPeriod(state, categories, text, rangeStart, rangeEnd, onlyAvailable, pageable);
        } else {
            return fetchDefaultWithPeriod(state, categories, text, rangeStart, rangeEnd, onlyAvailable, pageable);
        }
    }


    private List<Event> fetchSortedByEventDateWithPeriod(String state,
                                                         List<Long> categories,
                                                         String text,
                                                         LocalDateTime rangeStart,
                                                         LocalDateTime rangeEnd,
                                                         Boolean onlyAvailable,
                                                         Pageable pageable) {
        if (onlyAvailable) {
            if (categories != null && text != null) {
                return eventRepository.getEventsPeriodSortEventDateAvailableCategoryText(state, categories, rangeStart, rangeEnd, text, pageable);
            } else if (categories != null) {
                return eventRepository.getEventsPeriodSortEventDateAvailableCategory(state, categories, rangeStart, rangeEnd, pageable);
            } else if (text != null) {
                return eventRepository.getEventsPeriodSortEventDateAvailableText(state, rangeStart, rangeEnd, text, pageable);
            } else {
                return eventRepository.getEventsPeriodSortEventDateAvailable(state, rangeStart, rangeEnd, pageable);
            }
        } else {
            if (categories != null && text != null) {
                return eventRepository.getEventsPeriodSortEventDateCategoryText(state, categories, rangeStart, rangeEnd, text, pageable);
            } else if (categories != null) {
                return eventRepository.getEventsPeriodSortEventDateCategory(state, categories, rangeStart, rangeEnd, pageable);
            } else if (text != null) {
                return eventRepository.getEventsPeriodSortEventDateText(state, rangeStart, rangeEnd, text, pageable);
            } else {
                return eventRepository.getEventsPeriodSortEventDate(state, rangeStart, rangeEnd, pageable);
            }
        }
    }


    private List<Event> fetchSortedByViewsWithPeriod(String state,
                                                     List<Long> categories,
                                                     String text,
                                                     LocalDateTime rangeStart,
                                                     LocalDateTime rangeEnd,
                                                     Boolean onlyAvailable,
                                                     Pageable pageable) {
        if (onlyAvailable) {
            if (categories != null && text != null) {
                return eventRepository.getEventsPeriodSortViewsAvailableCategoryText(state, categories, rangeStart, rangeEnd, text, pageable);
            } else if (categories != null) {
                return eventRepository.getEventsPeriodSortViewsAvailableCategory(state, categories, rangeStart, rangeEnd, pageable);
            } else if (text != null) {
                return eventRepository.getEventsPeriodSortViewsAvailableText(state, rangeStart, rangeEnd, text, pageable);
            } else {
                return eventRepository.getEventsPeriodSortViewsAvailable(state, rangeStart, rangeEnd, pageable);
            }
        } else {
            if (categories != null && text != null) {
                return eventRepository.getEventsPeriodSortViewsCategoryText(state, categories, rangeStart, rangeEnd, text, pageable);
            } else if (categories != null) {
                return eventRepository.getEventsPeriodSortViewsCategory(state, categories, rangeStart, rangeEnd, pageable);
            } else if (text != null) {
                return eventRepository.getEventsPeriodSortViewsText(state, rangeStart, rangeEnd, text, pageable);
            } else {
                return eventRepository.getEventsPeriodSortViews(state, rangeStart, rangeEnd, pageable);
            }
        }
    }


    private List<Event> fetchDefaultWithPeriod(String state,
                                               List<Long> categories,
                                               String text,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               Boolean onlyAvailable,
                                               Pageable pageable) {
        if (onlyAvailable) {
            if (categories != null && text != null) {
                return eventRepository.getEventsPeriodAvailableCategoryText(state, categories, rangeStart, rangeEnd, text, pageable);
            } else if (categories != null) {
                return eventRepository.getEventsPeriodAvailableCategory(state, categories, rangeStart, rangeEnd, pageable);
            } else if (text != null) {
                return eventRepository.getEventsPeriodAvailableText(state, rangeStart, rangeEnd, text, pageable);
            } else {
                return eventRepository.getEventsPeriodAvailable(state, rangeStart, rangeEnd, pageable);
            }
        } else {
            if (categories != null && text != null) {
                return eventRepository.getEventsPeriodCategoryText(state, categories, rangeStart, rangeEnd, text, pageable);
            } else if (categories != null) {
                return eventRepository.getEventsPeriodCategory(state, categories, rangeStart, rangeEnd, pageable);
            } else if (text != null) {
                return eventRepository.getEventsPeriodText(state, rangeStart, rangeEnd, text, pageable);
            } else {
                return eventRepository.getEventsPeriod(state, rangeStart, rangeEnd, pageable);
            }
        }
    }

    @Transactional
    @Override
    public EventFullDto getEventByIdAndStatsPublic(HttpServletRequest request, Long eventId) {
        Event event = eventRepository.getEventByIdAndState(eventId, State.PUBLISHED);

        if (event == null) {
            throw new NotFoundException("Event with ID " + eventId + " not found");
        }
        LocalDateTime timeStart = event.getCreatedOn();
        LocalDateTime timeNow = LocalDateTime.now();
        List<String> uris = Collections.singletonList(request.getRequestURI());

        ResponseEntity<List<ViewStats>> response = statsClient.getStats(timeStart, timeNow, uris, true);
        List<ViewStats> resp = response.hasBody() ? response.getBody() : Collections.emptyList();

        if (resp.isEmpty()) {
            if (event.getViews() == null || event.getViews() == 0) {
                event.setViews(1L);
                eventRepository.save(event);
        } else {
                event.setViews((long) resp.size());
            }
        }
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .id(null)
                .app("main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(timeNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();

        statsClient.addRequest(endpointHitDto);

        return EventMapper.toEventFullDto(event);
    }


    private void confirmRequest(Event event, ParticipationRequest request) {
        request.setStatus(Status.CONFIRMED);
        event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        participationRepository.saveAndFlush(request);
    }


    private void rejectRequest(ParticipationRequest request) {
        request.setStatus(Status.REJECTED);
        participationRepository.saveAndFlush(request);
    }


    private List<ParticipationRequestDto> buildRejectedDtos(List<ParticipationRequest> allRequests,
                                                            List<ParticipationRequest> confirmedRequests) {
        List<ParticipationRequest> remainingRequests = new ArrayList<>(allRequests);
        remainingRequests.removeAll(confirmedRequests);
        return mapToDtos(remainingRequests);
    }


    private List<ParticipationRequestDto> mapToDtos(List<ParticipationRequest> requests) {
        return requests.stream().map(ParticipationMapper::toParticipationRequestDto).collect(Collectors.toList());
    }


    private EventRequestStatusUpdateResult buildResult(List<ParticipationRequestDto> confirmedRequests,
                                                       List<ParticipationRequestDto> rejectedRequests) {
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    private EventRequestStatusUpdateResult processRequestsWithoutModeration(Event event,
                                                                            Status status,
                                                                            List<ParticipationRequest> participationRequests) {
        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedDtos = new ArrayList<>();

        for (ParticipationRequest request : participationRequests) {
            if (!request.getStatus().equals(Status.PENDING)) {
                throw new StatusParticipationRequestException("Request status(" + request.getStatus()
                        + ") is not PENDING");
            }

            if (status.equals(Status.CONFIRMED)) {
                confirmRequest(event, request);
                confirmedRequests.add(request);

            } else {
                rejectRequest(request);
                rejectedDtos = buildRejectedDtos(participationRequests, new ArrayList<>());
                break;
            }
        }

        return buildResult(mapToDtos(confirmedRequests), rejectedDtos);
    }

    private EventRequestStatusUpdateResult processRequestsWithModeration(Event event,
                                                                         Status status,
                                                                         List<ParticipationRequest> participationRequests) {
        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedDtos = new ArrayList<>();

        for (ParticipationRequest request : participationRequests) {
            if (!request.getStatus().equals(Status.PENDING)) {
                throw new StatusParticipationRequestException("Request status(" + request.getStatus()
                        + ") is not PENDING");
            }

            if (status.equals(Status.CONFIRMED)) {
                confirmRequest(event, request);
                confirmedRequests.add(request);

            } else {
                rejectRequest(request);
                rejectedDtos = buildRejectedDtos(participationRequests, new ArrayList<>());
                break;
            }
        }
        return buildResult(mapToDtos(confirmedRequests), rejectedDtos);
    }

}
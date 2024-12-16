package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> getEventsByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByInitiatorIdAndId(Long userId, Long eventId);

    Event getEventsById(Long eventId);

    Set<Event> getEventsByIdIn(List<Long> events);

    List<Event> getEventsByInitiatorId(Long userId);

    Event getEventByIdAndState(Long eventId, State state);

    List<Event> getEventsByCategoryId(Long catId);

}
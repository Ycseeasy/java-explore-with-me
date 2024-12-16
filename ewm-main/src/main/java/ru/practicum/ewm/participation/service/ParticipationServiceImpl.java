package ru.practicum.ewm.participation.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.model.Status;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.OverflowLimitException;
import ru.practicum.ewm.exception.RepeatParticipationRequestException;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.participation.mapper.ParticipationMapper;
import ru.practicum.ewm.participation.model.ParticipationRequest;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipationServiceImpl implements ParticipationService {
    private final ParticipationRepository participationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public List<ParticipationRequestDto> getParticipationRequestPrivate(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User with ID " + userId + "not found");
        }
        List<Long> eventIds = eventRepository.getEventsByInitiatorId(userId).stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        List<ParticipationRequest> list;
        if (eventIds.isEmpty()) {
            list = participationRepository.getParticipationRequestsByRequester(userId);
        } else {
            list = participationRepository.getParticipationRequestsByRequesterAndEventNotIn(userId, eventIds);
        }
        return list.stream().map(ParticipationMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ParticipationRequestDto addParticipationRequestPrivate(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found"));


        List<ParticipationRequest> existingRequests = participationRepository
                .getParticipationRequestsByRequesterAndEvent(userId, eventId);
        validateAddParticipationRequestPrivate(event, existingRequests, userId);


        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(eventId)
                .requester(userId)
                .build();


        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            participationRequest.setStatus(Status.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else {
            participationRequest.setStatus(Status.PENDING);
        }


        eventRepository.save(event);
        ParticipationRequest newParticipationRequest = participationRepository.save(participationRequest);

        // Преобразование в DTO и возврат
        ParticipationRequestDto participationRequestDto = ParticipationMapper
                .toParticipationRequestDto(newParticipationRequest);
        participationRequestDto.setId(newParticipationRequest.getId());

        return participationRequestDto;
    }

    @Transactional
    @Override
    public ParticipationRequestDto updateRejectedParticipationRequestPrivate(Long userId, Long requestId) {

        ParticipationRequest participationRequest = participationRepository
                .getParticipationRequestByIdAndRequester(requestId, userId);
        if (participationRequest == null) {
            throw new NotFoundException("User with ID " + userId + "not found");
        }
        if (participationRequest.getStatus().equals(Status.PENDING)) {
            participationRequest.setStatus(Status.CANCELED);
        } else if (participationRequest.getStatus().equals(Status.CONFIRMED)) {
            Event event = eventRepository.getEventsById(participationRequest.getEvent());
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
            participationRequest.setStatus(Status.CANCELED);
        }

        return ParticipationMapper.toParticipationRequestDto(participationRepository.save(participationRequest));
    }

    private void validateAddParticipationRequestPrivate(@NotNull Event event,
                                                        List<ParticipationRequest> participationRequestList,
                                                        Long userId) {
        if (!participationRequestList.isEmpty()) {
            throw new RepeatParticipationRequestException("Request is empty");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new RepeatParticipationRequestException("Event already exist");
        } else if (event.getState() == null || !event.getState().equals(State.PUBLISHED)) {
            throw new RepeatParticipationRequestException("Event state is not PUBLISHED");
        } else if (event.getConfirmedRequests() != null && event.getParticipantLimit() > 0
                && event.getConfirmedRequests().equals(Long.valueOf(event.getParticipantLimit()))) {
            throw new OverflowLimitException("Too many requests");
        }
    }
}
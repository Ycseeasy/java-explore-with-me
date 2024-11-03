package ru.practicum.ewm.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.participation.model.ParticipationRequest;

import java.util.List;

public interface ParticipationRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> getParticipationRequestsByRequesterAndEvent(Long userId, Long eventId);

    List<ParticipationRequest> findByIdIn(List<Long> requestId);

    List<ParticipationRequest> getParticipationRequestsByRequesterAndEventNotIn(Long userId, List<Long> eventIdList);

    ParticipationRequest getParticipationRequestByIdAndRequester(Long requestId, Long userId);

    List<ParticipationRequest> getParticipationRequestsByEvent(Long eventId);

    List<ParticipationRequest> getParticipationRequestsByRequester(Long userId);
}
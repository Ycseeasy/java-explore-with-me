package ru.practicum.ewm.participation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.Status;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "participation_requests")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "event_id")
    private Long event;

    @Column(name = "requester_id")
    private Long requester;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;
}
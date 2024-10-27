package ru.practicum.ewm.participation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParticipationRequestDto {
    private Long id;
    private String created;
    private Long event;
    private Long requester;
    private String status;
}
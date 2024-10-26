package ru.practicum.ewm.dto.stats;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHitDto {

    private Integer id;

    private String app;

    private String uri;

    private String ip;

    private String timestamp;
}

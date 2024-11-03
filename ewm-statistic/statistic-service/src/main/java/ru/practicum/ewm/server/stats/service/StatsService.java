package ru.practicum.ewm.server.stats.service;

import ru.practicum.ewm.dto.stats.EndpointHitDto;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    EndpointHitDto addRequest(EndpointHitDto endpointHitDto);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, String[] uris, boolean unique);
}
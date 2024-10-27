package ru.practicum.ewm.server.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.stats.EndpointHitDto;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.server.stats.StatsMapper;
import ru.practicum.ewm.server.stats.exception.InvalidRequestException;
import ru.practicum.ewm.server.stats.model.EndpointHit;
import ru.practicum.ewm.server.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Transactional
    @Override
    public EndpointHitDto addRequest(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = StatsMapper.toEndpointHit(endpointHitDto);

        return StatsMapper.toEndpointHitDto(statsRepository.save(endpointHit));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, String[] uris, boolean unique) {

        if (start == null) {
            throw new InvalidRequestException("Start date must not be null");
        }
        if (end == null) {
            throw new InvalidRequestException("End date must not be null");
        }
        if (start.isAfter(end)) {
            throw new InvalidRequestException("Start date must be before or equal to end date");
        }

        List<ViewStats> list;

        if (!unique) {
            if (uris != null) {
                list = statsRepository.requestStats(start, end, uris);
            } else {
                list = statsRepository.requestAllStats(start, end);
            }
        } else {
            if (uris != null) {
                list = statsRepository.requestUniqueIpStats(start, end, uris);
            } else {
                list = statsRepository.requestUniqueIpAllStats(start, end);
            }
        }
        return list;
    }

}
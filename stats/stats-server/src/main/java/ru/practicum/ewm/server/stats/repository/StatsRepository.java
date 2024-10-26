package ru.practicum.ewm.server.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.server.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@EnableJpaRepositories
public interface StatsRepository extends JpaRepository<EndpointHit, Integer> {

    @Query(value = "select new ru.practicum.ewm.dto.stats.ViewStats(eh.app, eh.uri, cast(count(eh.ip) AS int) as hits) " +
            "from EndpointHit as eh " +
            "where eh.uri in ?3 and eh.timestamp >= ?1 and eh.timestamp <=?2 " +
            "group by eh.app, eh.uri order by hits desc")
    List<ViewStats> requestStats(LocalDateTime startTime, LocalDateTime endTime, String[] uris);

    @Query(value = "select new ru.practicum.ewm.dto.stats.ViewStats(eh.app, eh.uri, cast(count(eh.ip) AS int) as hits) " +
            "from EndpointHit as eh " +
            "where eh.timestamp >= ?1 and eh.timestamp <=?2 " +
            "group by eh.app, eh.uri order by hits desc")
    List<ViewStats> requestAllStats(LocalDateTime startTime, LocalDateTime endTime);

    @Query(value = "select new ru.practicum.ewm.dto.stats.ViewStats(eh.app, eh.uri, cast(count(DISTINCT eh.ip) AS int) as hits) " +
            "from EndpointHit as eh " +
            "where eh.uri in ?3 and eh.timestamp >= ?1 and eh.timestamp <=?2 " +
            "group by eh.app, eh.uri order by hits desc")
    List<ViewStats> requestUniqueIpStats(LocalDateTime startTime, LocalDateTime endTime, String[] uris);

    @Query(value = "select new ru.practicum.ewm.dto.stats.ViewStats(eh.app, eh.uri,cast(count(DISTINCT eh.ip) AS int) as hits) " +
            "from EndpointHit as eh " +
            "where eh.timestamp >= ?1 and eh.timestamp <=?2 " +
            "group by eh.app, eh.uri order by hits desc")
    List<ViewStats> requestUniqueIpAllStats(LocalDateTime startTime, LocalDateTime endTime);
}
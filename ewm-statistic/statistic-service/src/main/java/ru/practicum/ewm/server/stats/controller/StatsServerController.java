package ru.practicum.ewm.server.stats.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.stats.EndpointHitDto;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.server.stats.service.StatsService;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsServerController {
    private final StatsService statsService;

    @PostMapping("/hit")
    public EndpointHitDto addUser(@RequestBody EndpointHitDto endpointHitDto) {
        log.info("Creating request {}, app={}, ip={}", endpointHitDto.getApp(), endpointHitDto.getIp());
        return statsService.addRequest(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(
            @RequestParam(name = "start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss", iso = DateTimeFormat.ISO.DATE) LocalDateTime start,
            @RequestParam(name = "end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss", iso = DateTimeFormat.ISO.DATE) LocalDateTime end,
            @RequestParam(required = false, name = "uris") String[] uris,
            @RequestParam(name = "unique", defaultValue = "false") boolean unique) throws UnsupportedEncodingException {

        log.info("Get stats");
        return statsService.getStats(start, end, uris, unique);
    }

}
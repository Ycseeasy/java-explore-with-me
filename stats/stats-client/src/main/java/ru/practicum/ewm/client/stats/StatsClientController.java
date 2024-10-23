package ru.practicum.ewm.client.stats;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.dto.stats.EndpointHitDto;

import java.io.UnsupportedEncodingException;


@Slf4j
@Controller
@RequiredArgsConstructor
@Validated
public class StatsClientController {
    private final StatsClient statsClient;

    @PostMapping("/hit")
    public ResponseEntity<Object> addUser(HttpServletRequest request,
                                          @RequestBody EndpointHitDto endpointHitDto) {
        log.info("Creating request {}, app={}, ip={}", endpointHitDto.getApp(), endpointHitDto.getIp());
        return statsClient.addRequest(request.getRemoteAddr(), endpointHitDto);
    }

    @GetMapping("/stats")
    public ResponseEntity<Object> getAll(HttpServletRequest request,
                                         @RequestParam(name = "start") String start,
                                         @RequestParam(name = "end") String end,
                                         @RequestParam(required = false, name = "uris") String[] uris,
                                         @RequestParam(name = "unique", defaultValue = "false") boolean unique) throws UnsupportedEncodingException {
        log.info("Get stats");
        String ipResource = request.getRemoteAddr();
        return statsClient.getStats(ipResource, start, end, uris, unique);
    }
}
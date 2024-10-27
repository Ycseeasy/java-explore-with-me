package ru.practicum.ewm.client.stats;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.dto.stats.EndpointHitDto;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class StatsClient extends BaseClient {

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory()).build());
    }

    public ResponseEntity<Object> addRequest(EndpointHitDto endpointHitDto) {
        return post("/hit", endpointHitDto);
    }


    public ResponseEntity<List<ViewStats>> getStats(LocalDateTime start, LocalDateTime end,
                                                    @Nullable List<String> uris, boolean unique) {
        Map<String, Object> parameters = new HashMap<>(Map.of(
                "start", start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "end", end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "unique", unique));
        String uri;
        if (uris != null) {
            for (String s : uris) {
                parameters.put("uris", s);
            }
            uri = UriComponentsBuilder.fromPath("/stats")
                    .queryParam("start", start)
                    .queryParam("end", end)
                    .queryParam("uris", String.join(",", uris))
                    .queryParam("unique", unique)
                    .build()
                    .toUriString();
        } else {
            uri = UriComponentsBuilder.fromPath("/stats")
                    .queryParam("start", start)
                    .queryParam("end", end)
                    .queryParam("unique", unique)
                    .build()
                    .toUriString();
        }
        return getList(uri, parameters, new ParameterizedTypeReference<>() {
        });
    }
}
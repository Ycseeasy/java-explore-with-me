package ru.practicum.ewm.client.stats;

import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.dto.stats.EndpointHitDto;

import java.util.List;
import java.util.Map;


@Service
@AllArgsConstructor
public class StatsClient {
    protected final RestTemplate rest;

    public ResponseEntity<Object> addRequest(String ipResource, EndpointHitDto endpointHitDto) {
        return post("/hit", ipResource, null, endpointHitDto);
    }

    public ResponseEntity<Object> getStats(String ipResource, String start, String end, String[] uris, boolean unique) {

        Map<String, Object> parameters = null;
        if (uris != null) {
            parameters = Map.of(
                    "start", start,
                    "end", end,
                    "uris", uris,
                    "unique", unique
            );
            return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", ipResource, parameters);
        } else {
            parameters = Map.of(
                    "start", start,
                    "end", end,
                    "unique", unique
            );
            return get("/stats?start={start}&end={end}&unique={unique}", ipResource, parameters);
        }
    }

    protected <T> ResponseEntity<Object> post(String path, String ipResource, @Nullable Map<String, Object> parameters, T body) {
        return makeAndSendRequest(HttpMethod.POST, path, ipResource, parameters, body);
    }

    protected ResponseEntity<Object> get(String path, String ipResource, @Nullable Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, path, ipResource, parameters, null);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, String ipResource, @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders(ipResource));

        ResponseEntity<Object> statsServerResponse;
        try {
            if (parameters != null) {
                statsServerResponse = rest.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                statsServerResponse = rest.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareGatewayResponse(statsServerResponse);
    }

    private HttpHeaders defaultHeaders(String ipResource) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (ipResource != null) {
            headers.set("X-Stats-Resource-Ip", ipResource);
        }
        return headers;
    }

    private static ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}
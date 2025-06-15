package ru.practicum.shareit.client;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;

@Component
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    public UserClient(@Value("${shareit-server.url}") String serverUrl,
                      RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault()))
                .build());
    }

    public <T> ResponseEntity<Object> add(T body) {
        return post("", body);
    }

    public ResponseEntity<Object> get(Long id) {
        return get("/" + id);
    }

    public <T> ResponseEntity<Object> patch(Long id, T body) {
        return patch("/" + id, body);
    }

    public ResponseEntity<Object> delete(Long id) {
        return delete("/" + id);
    }

    public ResponseEntity<Object> getAll(int from, int size) {
        return get("?from={from}&size={size}", null,
                Map.of("from", from, "size", size));
    }
}

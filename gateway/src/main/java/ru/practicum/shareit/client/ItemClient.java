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
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl,
                      RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault()))
                .build());
    }

    public <T> ResponseEntity<Object> add(long userId, T body) {
        return post("", userId, body);
    }

    public <T> ResponseEntity<Object> patch(long userId, long itemId, T body) {
        return patch("/" + itemId, userId, body);
    }

    public ResponseEntity<Object> get(long userId, long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getOwnerItems(long userId, int from, int size) {
        return get("?from={from}&size={size}",
                userId,
                Map.of("from", from, "size", size));
    }

    public ResponseEntity<Object> search(String text, int from, int size) {
        return get("/search?text={text}&from={from}&size={size}",
                null,                                     // заголовок X-Sharer-User-Id не нужен
                Map.of("text", text, "from", from, "size", size));
    }

    public <T> ResponseEntity<Object> addComment(long userId, long itemId, T body) {
        return post("/" + itemId + "/comment", userId, body);
    }
}


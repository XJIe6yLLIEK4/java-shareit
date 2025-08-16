package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.RequestClient;


@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final RequestClient client;

    @PostMapping
    public ResponseEntity<Object> addRequest(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody @Valid ItemRequestDto requestDto) {
        return client.add(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> own(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.getOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> others(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) {
        return client.getOthers(userId, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> one(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @PathVariable("id") Long id) {
        return client.getOne(userId, id);
    }
}

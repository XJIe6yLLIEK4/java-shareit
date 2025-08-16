package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.comment.CommentDto;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemClient client;

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @Valid @RequestBody ItemDto dto) {
        return client.add(userId, dto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable("id") Long id,
                                         @Valid @RequestBody ItemDto dto) {
        return client.patch(userId, id, dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @PathVariable("id") Long id) {
        return client.get(userId, id);
    }

    @GetMapping
    public ResponseEntity<Object> ownerItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        return client.getOwnerItems(userId, from, size);  // исправлено
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) {
        return client.search(text, from, size);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Object> comment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable("id") Long id,
                                          @Valid @RequestBody CommentDto dto) {
        return client.addComment(userId, id, dto);
    }
}

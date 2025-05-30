package ru.practicum.shareit.request;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ItemRequestRepository {
    private final Map<Long, ItemRequest> storage = new HashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public ItemRequest save(ItemRequest req) {
        if (req.getId() == null) req.setId(idGen.getAndIncrement());
        storage.put(req.getId(), req);
        return req;
    }

    public Optional<ItemRequest> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<ItemRequest> findByRequester(Long userId) {
        return storage.values().stream()
                .filter(r -> r.getRequesterId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<ItemRequest> findAllExcept(Long userId) {
        return storage.values().stream()
                .filter(r -> !r.getRequesterId().equals(userId))
                .collect(Collectors.toList());
    }
}

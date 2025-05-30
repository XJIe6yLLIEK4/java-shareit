package ru.practicum.shareit.booking;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class BookingRepository {
    private final Map<Long, Booking> storage = new HashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public Booking save(Booking booking) {
        if (booking.getId() == null) booking.setId(idGen.getAndIncrement());
        storage.put(booking.getId(), booking);
        return booking;
    }

    public Optional<Booking> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Booking> findByBooker(Long userId) {
        return storage.values().stream()
                .filter(b -> b.getBookerId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<Booking> findByOwnerItems(Set<Long> ownerItemIds) {
        return storage.values().stream()
                .filter(b -> ownerItemIds.contains(b.getItemId()))
                .collect(Collectors.toList());
    }
}

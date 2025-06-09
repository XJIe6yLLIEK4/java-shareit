package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    Booking create(Long userId, BookingDto bookingDto);

    Booking approve(Long userId, Long bookingId, boolean approved);

    Booking getById(Long userId, Long bookingId);

    List<Booking> getAllByBooker(Long userId, String state);

    List<Booking> getAllByOwner(Long userId, String state);
}

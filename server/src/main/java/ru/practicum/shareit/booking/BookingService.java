package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(Long userId, BookingRequestDto bookingRequestDto);

    BookingResponseDto approve(Long userId, Long bookingId, boolean approved);

    BookingResponseDto getById(Long userId, Long bookingId);

    List<BookingResponseDto> getAllByBooker(Long userId, String state);

    List<BookingResponseDto> getAllByOwner(Long userId, String state);
}

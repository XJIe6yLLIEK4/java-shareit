package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class BookingMapper {

    public static BookingResponseDto toDto(Booking booking) {
        if (booking == null) return null;

        return BookingResponseDto.builder()
                .id(booking.getId())
                .item(booking.getItem())
                .booker(booking.getBooker())
                .status(booking.getStatus())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    public static Booking toModel(BookingRequestDto dto, Item item, User user, BookingStatus status) {
        if (dto == null) return null;

        return Booking.builder()
                .id(dto.getId())
                .item(item)
                .booker(user)
                .start(dto.getStart())
                .end(dto.getEnd())
                .status(status)
                .build();
    }
}


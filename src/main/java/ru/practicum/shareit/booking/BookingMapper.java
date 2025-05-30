package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;

public class BookingMapper {

    public static BookingDto toDto(Booking booking) {
        if (booking == null) return null;

        return BookingDto.builder()
                .id(booking.getId())
                .itemId(booking.getItemId())
                .bookerId(booking.getBookerId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus().name())
                .build();
    }

    public static Booking toModel(BookingDto dto) {
        if (dto == null) return null;

        return Booking.builder()
                .id(dto.getId())
                .itemId(dto.getItemId())
                .bookerId(dto.getBookerId())
                .start(dto.getStart())
                .end(dto.getEnd())
                .status(BookingStatus.valueOf(dto.getStatus()))
                .build();
    }
}


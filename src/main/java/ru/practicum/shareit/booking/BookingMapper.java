package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class BookingMapper {

    public static BookingDto toDto(Booking booking) {
        if (booking == null) return null;

        return BookingDto.builder()
                .id(booking.getId())
                .itemId(booking.getItem().getId())
                .itemName(booking.getItem().getName())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus().name())
                .build();
    }

    public static Booking toModel(BookingDto dto, Item item, User user, BookingStatus status) {
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


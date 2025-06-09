package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingServiceImpl;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

public class ItemMapper {
    private static CommentRepository commentRepository;
    private static BookingServiceImpl bookingService;
    private static UserRepository userRepository;

    public static void init(CommentRepository commentRepository, BookingServiceImpl bookingService, UserRepository userRepository) {
        ItemMapper.commentRepository = commentRepository;
        ItemMapper.bookingService = bookingService;
        ItemMapper.userRepository = userRepository;
    }

    public static ItemDto toDto(Item item) {
        if (item == null) return null;
        LocalDateTime now = LocalDateTime.now();

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(bookingService.getLastBooking(item.getId(), now).map(Booking::getEnd).orElse(null))
                .nextBooking(bookingService.getNextBooking(item.getId(), now).map(Booking::getStart).orElse(null))
                .comments(commentRepository.findByItemIdOrderByCreatedDesc(item.getId()).stream()
                        .map((c) -> CommentMapper.toDto(c, userRepository.findById(c.getAuthorId())
                                .map(User::getName).orElse("unknown")))
                        .toList())
                .build();
    }

    public static ItemDto toSimpleDto(Item item) {
        if (item == null) return null;
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(null)
                .nextBooking(null)
                .comments(commentRepository.findByItemIdOrderByCreatedDesc(item.getId()).stream()
                        .map((c) -> CommentMapper.toDto(c, userRepository.findById(c.getAuthorId())
                                .map(User::getName).orElse("unknown")))
                        .toList())
                .build();
    }

    public static Item toModel(ItemDto dto, User owner) {
        if (dto == null) return null;
        return Item.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .owner(owner)
                .build();
    }
}

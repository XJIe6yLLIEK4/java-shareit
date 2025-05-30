package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;

public class ItemRequestMapper {
    public static ItemRequestDto toDto(ItemRequest req) {
        if (req == null) return null;
        return ItemRequestDto.builder()
                .id(req.getId())
                .description(req.getDescription())
                .created(req.getCreated())
                .build();
    }

    public static ItemRequest toModel(ItemRequestDto dto, Long userId) {
        if (dto == null) return null;
        return ItemRequest.builder()
                .id(dto.getId())
                .description(dto.getDescription())
                .requesterId(userId)
                .created(LocalDateTime.now())
                .build();
    }
}

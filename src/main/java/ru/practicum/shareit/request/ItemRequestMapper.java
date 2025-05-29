package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;

public class ItemRequestMapper {
    public static ItemRequestDto toDto(ItemRequest req) {
        if (req == null) return null;
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(req.getId());
        dto.setDescription(req.getDescription());
        dto.setCreated(req.getCreated());
        return dto;
    }

    public static ItemRequest toModel(ItemRequestDto dto, Long userId) {
        if (dto == null) return null;
        ItemRequest req = new ItemRequest();
        req.setId(dto.getId());
        req.setDescription(dto.getDescription());
        req.setRequesterId(userId);
        req.setCreated(LocalDateTime.now());
        return req;
    }
}

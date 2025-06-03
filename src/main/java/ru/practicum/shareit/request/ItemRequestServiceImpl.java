package ru.practicum.shareit.request;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository repo;

    public ItemRequestServiceImpl(ItemRequestRepository repo) {
        this.repo = repo;
    }

    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto dto) {
        ItemRequest req = ItemRequestMapper.toModel(dto, userId);
        return ItemRequestMapper.toDto(repo.save(req));
    }

    @Override
    public List<ItemRequestDto> getByUser(Long userId) {
        return repo.findByRequester(userId).stream()
                .map(ItemRequestMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId) {
        return repo.findAllExcept(userId).stream()
                .map(ItemRequestMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        ItemRequest req = repo.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Request not found"));
        return ItemRequestMapper.toDto(req);
    }
}

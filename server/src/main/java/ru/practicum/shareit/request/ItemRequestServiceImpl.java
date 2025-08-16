package ru.practicum.shareit.request;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepo;
    private final ItemRepository itemRepo;
    private final UserRepository userRepo;
    private final ItemRequestMapper mapper;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto dto) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        ItemRequest saved = requestRepo.save(mapper.toModel(dto.getDescription(), user));
        return mapper.toDto(saved, List.of());
    }

    @Override
    public List<ItemRequestDto> getByUser(Long userId) {
        return findUserRequests(userId);
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId) {
        return findOthers(userId, 0, Integer.MAX_VALUE);
    }

    @Override
    public ItemRequestDto getById(Long userId, Long reqId) {
        ItemRequest r = requestRepo.findById(reqId)
                .orElseThrow(() -> new NoSuchElementException("request"));
        List<Item> answers = itemRepo.findByRequestId(reqId);
        return mapper.toDto(r, answers);
    }

    public List<ItemRequestDto> findUserRequests(Long userId) {
        List<ItemRequest> reqs = requestRepo.findAllByRequesterIdOrderByCreatedDesc(userId);
        return toDtosWithAnswers(reqs);
    }

    public List<ItemRequestDto> findOthers(Long userId, int from, int size) {
        Pageable page = PageRequest.of(from / size, size);
        List<ItemRequest> reqs = requestRepo.findOtherUsersRequests(userId, page);
        return toDtosWithAnswers(reqs);
    }

    private List<ItemRequestDto> toDtosWithAnswers(List<ItemRequest> reqs) {
        Map<Long, List<Item>> answers =
                itemRepo.findByRequestIdIn(reqs.stream().map(ItemRequest::getId).toList())
                        .stream()
                        .collect(Collectors.groupingBy(i -> i.getRequest().getId()));
        return reqs.stream()
                .map(r -> mapper.toDto(r, answers.getOrDefault(r.getId(), List.of())))
                .toList();
    }
}

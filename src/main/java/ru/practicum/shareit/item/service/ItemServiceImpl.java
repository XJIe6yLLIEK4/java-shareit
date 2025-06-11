package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingServiceImpl;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingServiceImpl bookingService;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           BookingServiceImpl bookingService, CommentRepository commentRepository,
                           ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingService = bookingService;
        this.commentRepository = commentRepository;
        this.itemMapper = itemMapper;
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Item item = ItemMapper.toModel(itemDto, owner);
        Item saved = itemRepository.save(item);
        return itemMapper.toDto(saved);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found"));
        if (!existing.getOwner().getId().equals(userId)) throw new SecurityException("Access denied");

        if (itemDto.getName() != null) existing.setName(itemDto.getName());
        if (itemDto.getDescription() != null) existing.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) existing.setAvailable(itemDto.getAvailable());
        Item updated = itemRepository.save(existing);
        return itemMapper.toDto(updated);
    }

    @Override
    public ItemDto getById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found"));
        if (userId.equals(item.getOwner().getId())) {
            return itemMapper.toDto(item);
        }
        return itemMapper.toSimpleDto(item);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        return itemRepository.findByOwnerId(userId).stream()
                .map(itemMapper::toSimpleDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        return itemRepository.search(text).stream()
                .map(itemMapper::toSimpleDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto dto) {
        boolean finished = bookingService.contains(
                userId, itemId, LocalDateTime.now(), BookingStatus.APPROVED);
        if (!finished) throw new ValidationException("No completed booking");
        Comment comment = commentRepository.save(Comment.builder()
                .text(dto.getText())
                .author(userRepository.findById(userId).orElseThrow())
                .item(itemRepository.getReferenceById(itemId))
                .created(LocalDateTime.now())
                .build());
        return CommentMapper.toDto(comment);
    }
}
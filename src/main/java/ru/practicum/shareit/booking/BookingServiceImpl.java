package ru.practicum.shareit.booking;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, ItemRepository itemRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public BookingResponseDto create(Long userId, BookingRequestDto dto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Item not found"));
        if (!item.getAvailable()) {
            throw new ValidationException("Товар недоступен");
        }
        Booking booking = BookingMapper.toModel(dto, item, booker, BookingStatus.WAITING);

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto approve(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));

        Long ownerId = booking.getItem().getOwner().getId();
        if (!ownerId.equals(userId)) {
            throw new SecurityException("Only owner can approve");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));

        Long ownerId = booking.getItem().getOwner().getId();
        if (!booking.getBooker().getId().equals(userId) && !ownerId.equals(userId)) {
            throw new SecurityException("Access denied");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByBooker(Long userId, String state) {
        return filterStateByBooker(userId, state)
                .stream().map(BookingMapper::toDto).toList();
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long ownerId, String state) {
        userRepository.findById(ownerId).orElseThrow(() -> new NoSuchElementException("Owner Not Found"));
        return filterStateByOwner(ownerId, state)
                .stream().map(BookingMapper::toDto).toList();
    }

    public Optional<Booking> getLastBooking(Long itemId, LocalDateTime localDateTime) {
        return bookingRepository.findTopByItemIdAndEndBeforeAndStatusOrderByStartDesc(itemId, localDateTime, BookingStatus.APPROVED);
    }

    public Optional<Booking> getNextBooking(Long itemId, LocalDateTime localDateTime) {
        return bookingRepository.findTopByItemIdAndStartAfterAndStatusOrderByStartAsc(itemId, localDateTime, BookingStatus.APPROVED);
    }

    public boolean contains(Long bookerId, Long itemId, LocalDateTime before, BookingStatus status) {
        return bookingRepository.existsByBooker_IdAndItemIdAndEndBeforeAndStatus(bookerId, itemId, before, status);
    }

    private List<Booking> filterStateByBooker(Long userId, String state) {
        LocalDateTime now = LocalDateTime.now();
        return switch (state.toUpperCase()) {
            case "CURRENT" -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case "PAST" -> bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case "FUTURE" -> bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case "WAITING" -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case "REJECTED" -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            case "ALL" -> bookingRepository.findByBookerIdOrderByStartDesc(userId);
            default -> throw new ValidationException("Unknown state");
        };
    }

    private List<Booking> filterStateByOwner(Long userId, String state) {
        LocalDateTime now = LocalDateTime.now();
        return switch (state.toUpperCase()) {
            case "CURRENT" -> bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case "PAST" -> bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);
            case "FUTURE" -> bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);
            case "WAITING" -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case "REJECTED" -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            case "ALL" -> bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
            default -> throw new ValidationException("Unknown state");
        };
    }
}

package ru.practicum.shareit.booking;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
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
    public Booking create(Long userId, BookingDto dto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Item not found"));
        if (item.getId() == 27) {
            System.out.println("Взят в аренду itemId = 27");
            System.out.printf("Путь userId = %d POST bookings с телом: %s%n",userId, dto);
            System.out.println("Время сейчас: " + LocalDateTime.now());
        }
        if (!item.getAvailable()) {
            throw new ValidationException("Товар недоступен");
        }
        Booking booking = BookingMapper.toModel(dto, item, booker, BookingStatus.WAITING);

        Booking b = bookingRepository.save(booking);
        System.out.println("Аренда из БД в момент создания " + b);
        return b;
    }

    @Override
    public Booking approve(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));

        Long ownerId = booking.getItem().getOwner().getId();
        if (!ownerId.equals(userId)) {
            throw new SecurityException("Only owner can approve");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));

        Long ownerId = booking.getItem().getOwner().getId();
        if (!booking.getBooker().getId().equals(userId) && !ownerId.equals(userId)) {
            throw new SecurityException("Access denied");
        }
        return booking;
    }

    @Override
    public List<Booking> getAllByBooker(Long userId, String state) {
        return filterState(bookingRepository.findByBooker_IdOrderByStartDesc(userId), state)
                .stream().toList();
    }

    @Override
    public List<Booking> getAllByOwner(Long ownerId, String state) {
        userRepository.findById(ownerId).orElseThrow(() -> new NoSuchElementException("Owner Not Found"));
        return filterState(bookingRepository.findByOwner(ownerId), state)
                .stream().toList();
    }

    public Optional<Booking> getLastBooking(Long itemId, LocalDateTime localDateTime) {
        System.out.println("Прошлая аренда " + bookingRepository.findTopByItemIdAndEndBeforeAndStatusOrderByStartDesc(itemId, localDateTime, BookingStatus.APPROVED));
        return bookingRepository.findTopByItemIdAndEndBeforeAndStatusOrderByStartDesc(itemId, localDateTime, BookingStatus.APPROVED);
    }

    public Optional<Booking> getNextBooking(Long itemId, LocalDateTime localDateTime) {
        return bookingRepository.findTopByItemIdAndStartAfterAndStatusOrderByStartAsc(itemId, localDateTime, BookingStatus.APPROVED);
    }

    public boolean contains(Long bookerId, Long itemId, LocalDateTime before, BookingStatus status) {
        return bookingRepository.existsByBooker_IdAndItemIdAndEndBeforeAndStatus(bookerId, itemId, before, status);
    }

    private List<Booking> filterState(List<Booking> src, String st) {
        LocalDateTime now = LocalDateTime.now();
        return switch (st.toUpperCase()) {
            case "CURRENT" -> src.stream().filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now)).toList();
            case "PAST" -> src.stream().filter(b -> b.getEnd().isBefore(now)).toList();
            case "FUTURE" -> src.stream().filter(b -> b.getStart().isAfter(now)).toList();
            case "WAITING" -> src.stream().filter(b -> b.getStatus() == BookingStatus.WAITING).toList();
            case "REJECTED" -> src.stream().filter(b -> b.getStatus() == BookingStatus.REJECTED).toList();
            case "ALL" -> src;
            default -> throw new ValidationException("Unknown state");
        };
    }
}

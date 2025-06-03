package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepo;
    private final ItemRepository itemRepo;

    public BookingServiceImpl(BookingRepository bookingRepo, ItemRepository itemRepo) {
        this.bookingRepo = bookingRepo;
        this.itemRepo = itemRepo;
    }

    @Override
    public BookingDto create(Long userId, BookingDto dto) {
        Booking booking = BookingMapper.toModel(dto);
        booking.setBookerId(userId);
        booking.setStatus(BookingStatus.WAITING);
        Booking saved = bookingRepo.save(booking);
        return BookingMapper.toDto(saved);
    }

    @Override
    public BookingDto approve(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));
        Long ownerId = itemRepo.findById(booking.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Item not found")).getOwnerId();
        if (!ownerId.equals(userId)) throw new SecurityException("Only owner can approve");
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookingRepo.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));
        Long ownerId = itemRepo.findById(booking.getItemId())
                .map(i -> i.getOwnerId()).orElse(null);
        if (!booking.getBookerId().equals(userId) && !ownerId.equals(userId)) {
            throw new SecurityException("Access denied");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getAllByBooker(Long userId) {
        return bookingRepo.findByBooker(userId).stream()
                .map(BookingMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllByOwner(Long userId) {
        var itemIds = itemRepo.findAllByOwner(userId).stream().map(i -> i.getId()).collect(Collectors.toSet());
        return bookingRepo.findByOwnerItems(itemIds).stream()
                .map(BookingMapper::toDto).collect(Collectors.toList());
    }
}
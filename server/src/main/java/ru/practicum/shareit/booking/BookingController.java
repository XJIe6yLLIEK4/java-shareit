package ru.practicum.shareit.booking;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;


@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponseDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @RequestBody BookingRequestDto dto) {
        return bookingService.create(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @PathVariable("bookingId") Long bookingId,
                                      @RequestParam boolean approved) {
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @PathVariable("bookingId") Long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByBooker(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllByBooker(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllByOwner(userId, state);
    }
}

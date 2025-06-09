package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.*;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker_IdOrderByStartDesc(Long id);

    @Query("SELECT b FROM Booking b WHERE b.booker.id=:id AND b.start > CURRENT_TIMESTAMP ORDER BY b.start DESC")
    List<Booking> findFutureByBooker(Long id);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id=:owner ORDER BY b.start DESC")
    List<Booking> findByOwner(Long owner);

    Optional<Booking> findTopByItemIdAndEndBeforeAndStatusOrderByStartDesc(Long itemId, LocalDateTime now, BookingStatus status);

    Optional<Booking> findTopByItemIdAndStartAfterAndStatusOrderByStartAsc(Long itemId, LocalDateTime now, BookingStatus status);

    boolean existsByBooker_IdAndItemIdAndEndBeforeAndStatus(
            Long bookerId, Long itemId, LocalDateTime before, BookingStatus status);
}

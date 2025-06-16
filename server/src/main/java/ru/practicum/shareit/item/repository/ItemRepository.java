package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerId(Long ownerId);

    List<Item> findByRequestId(Long requestId);

    List<Item> findByRequestIdIn(List<Long> requestIds);

    @Query("SELECT i FROM Item i WHERE i.available = TRUE AND " +
            "(lower(i.name) LIKE lower(concat('%', :txt, '%')) OR " +
            " lower(i.description) LIKE lower(concat('%', :txt, '%')))")
    List<Item> search(@Param("txt") String text);
}
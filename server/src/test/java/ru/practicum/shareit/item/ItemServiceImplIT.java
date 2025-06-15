package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class ItemServiceImplIT {

    @Container
    static PostgreSQLContainer<?> db =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("shareit")
                    .withUsername("user")
                    .withPassword("pass");

    @Autowired
    ItemService itemService;
    @Autowired
    UserRepository userRepo;
    @Autowired
    ItemRepository itemRepo;

    @Test
    void createAndFetchItem_withBookingsAndComments() {
        User owner = userRepo.save(User.builder()
                .name("Ann").email("a@mail.ru").build());

        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .build();

        ItemDto saved = itemService.create(owner.getId(), dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Drill");

        ItemDto fetched = itemService.getById(owner.getId(), saved.getId());
        assertThat(fetched.getComments()).isEmpty();
        assertThat(fetched.getLastBooking()).isNull();
    }
}
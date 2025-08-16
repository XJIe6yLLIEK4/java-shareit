package ru.practicum.shareit.serviceIT;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = ShareItServer.class)
@AutoConfigureTestDatabase
@Transactional
class ItemServiceImplIT {

    @Autowired
    ItemService itemService;
    @Autowired UserService userService;

    Long ownerId;
    Long otherId;
    Long itemId;

    @BeforeEach
    void initData() {
        ownerId = userService.create(new UserDto(null, "Owner", "o@mail")).getId();
        otherId = userService.create(new UserDto(null, "Other", "x@mail")).getId();

        itemId = itemService.create(ownerId,
                ItemDto.builder()
                        .name("Drill")
                        .description("Cordless power drill")
                        .available(true)
                        .build()).getId();
    }

    @Test
    void getUserItems_returnsOnlyOwners() {
        List<ItemDto> items = itemService.getAllByOwner(ownerId);
        assertThat(items).hasSize(1).first().extracting(ItemDto::getId).isEqualTo(itemId);
    }

    @Test
    void search_caseInsensitive_ok() {
        List<ItemDto> result = itemService.search("drILL");
        assertThat(result).extracting(ItemDto::getId).contains(itemId);
    }

    @Test
    void addComment_withoutFinishedBooking_fails() {
        assertThatThrownBy(() ->
                itemService.addComment(otherId, itemId,
                        ru.practicum.shareit.comment.CommentDto.builder()
                                .text("nice").build()))
                .isInstanceOf(ValidationException.class);
    }
}

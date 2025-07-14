package ru.practicum.shareit.dto;

import jakarta.validation.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.item.ItemDto;

import java.util.Set;

@JsonTest
@ContextConfiguration(classes = ru.practicum.shareit.ShareItGateway.class)
class ItemDtoJsonTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void blankName_shouldFail() {
        ItemDto bad = ItemDto.builder()
                .name("")
                .description("abc")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(bad);
        Assertions.assertThat(violations).hasSize(1);
        Assertions.assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
    }
}

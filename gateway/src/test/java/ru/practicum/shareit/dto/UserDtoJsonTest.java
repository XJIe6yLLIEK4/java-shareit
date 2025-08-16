package ru.practicum.shareit.dto;

import jakarta.validation.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.user.UserDto;

import java.util.Set;

@JsonTest
@ContextConfiguration(classes = ru.practicum.shareit.ShareItGateway.class)
class UserDtoJsonTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void badEmail_shouldFail() {
        UserDto bad = UserDto.builder()
                .name("John")
                .email("wrong")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(bad);
        Assertions.assertThat(violations).hasSize(1);
        Assertions.assertThat(violations.iterator().next().getMessage()).contains("формат email");
    }
}

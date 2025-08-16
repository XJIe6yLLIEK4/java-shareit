package ru.practicum.shareit.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import jakarta.validation.*;
import java.time.LocalDateTime;
import java.util.Set;

@JsonTest
@ContextConfiguration(classes = ru.practicum.shareit.ShareItGateway.class)
class BookItemRequestDtoJsonTest {

    @Autowired JacksonTester<BookItemRequestDto> json;

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void serialize_deserialize_ok() throws Exception {
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 10, 0);
        LocalDateTime end   = start.plusHours(2);

        BookItemRequestDto dto = new BookItemRequestDto(99L, start, end);

        String written = json.write(dto).getJson();
        Assertions.assertThat(written).contains("\"itemId\":99");
        Assertions.assertThat(written).contains(start.toString());

        BookItemRequestDto parsed = json.parseObject(written);
        Assertions.assertThat(parsed.getItemId()).isEqualTo(99L);
        Assertions.assertThat(parsed.getStart()).isEqualTo(start);
        Assertions.assertThat(parsed.getEnd()).isEqualTo(end);
    }

    @Test
    void validation_shouldFail_whenStartInPast() {
        BookItemRequestDto bad = new BookItemRequestDto(
                1L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1));

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(bad);
        Assertions.assertThat(violations).hasSize(1);
    }
}

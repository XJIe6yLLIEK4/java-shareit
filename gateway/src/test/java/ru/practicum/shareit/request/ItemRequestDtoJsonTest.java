package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    ObjectMapper mapper;

    @Test
    void serialization_deserialization_ok() throws Exception {
        LocalDateTime created = LocalDateTime.of(2025, 6, 12, 10, 30);

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(3L)
                .description("Need snowboard")
                .created(created)
                .items(List.of())
                .build();

        String json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"description\":\"Need snowboard\"");
        assertThat(json).contains("\"created\":\"2025-06-12T10:30:00\"");

        ItemRequestDto restored = mapper.readValue(json, ItemRequestDto.class);
        assertThat(restored).usingRecursiveComparison().isEqualTo(dto);
    }
}

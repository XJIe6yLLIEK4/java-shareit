package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto update(Long userId, UserDto userDto);

    UserDto getById(Long userId);

    List<UserDto> getAll();

    void delete(Long userId);
}

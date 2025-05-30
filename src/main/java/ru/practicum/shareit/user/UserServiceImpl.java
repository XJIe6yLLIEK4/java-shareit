package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Autowired
    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toModel(userDto);
        User saved = repository.create(user);
        return UserMapper.toDto(saved);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User existing = repository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (userDto.getName() != null) existing.setName(userDto.getName());
        if (userDto.getEmail() != null) existing.setEmail(userDto.getEmail());
        User updated = repository.update(existing);
        return UserMapper.toDto(updated);
    }

    @Override
    public UserDto getById(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return repository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId) {
        repository.delete(userId);
    }
}

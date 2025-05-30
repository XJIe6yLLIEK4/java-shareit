package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ExceptionSameEmail;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {
    private final Map<Long, User> storage = new HashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public User create(User user) {
        if (user.getId() == null) {
            user.setId(idGen.getAndIncrement());
        }
        for (User u : storage.values()) {
            if (user.getEmail().equals(u.getEmail())) {
                throw new ExceptionSameEmail();
            }
        }
        storage.put(user.getId(), user);
        return storage.get(user.getId());
    }

    public User update(User user) {
        Optional<User> userWithSameEmail = storage.values().stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .filter(u -> u.getEmail().equals(user.getEmail()))
                .findFirst();

        if (userWithSameEmail.isEmpty()) {
            storage.put(user.getId(), user);
        } else {
            throw new ExceptionSameEmail();
        }
        return storage.get(user.getId());
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void delete(Long id) {
        storage.remove(id);
    }
}

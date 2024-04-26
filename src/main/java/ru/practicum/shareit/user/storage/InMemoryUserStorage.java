package ru.practicum.shareit.user.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.DuplicateDataException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> dataUsers;
    private int id = 0;

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(dataUsers.values());
    }

    @Override
    public Optional<User> getUserById(int id) {
        return Optional.ofNullable(dataUsers.get(id));
    }

    @Override
    public User createUser(User user) {
        if (checkEmail(user.getEmail())) {
            throw new DuplicateDataException("Пользователь с email=" + user.getEmail() + " уже существует");
        }
        user.setId(generatedId());
        user.setId(id);
        dataUsers.put(id, user);
        return user;
    }

    @Override
    public User update(int id, User user) {
        User savedUser = dataUsers.get(id);
        dataUsers.remove(id);
        if (checkEmail(user.getEmail())) {
            dataUsers.put(id, savedUser);
            throw new DuplicateDataException("Пользователь с email=" + user.getEmail() + " уже существует");
        }
        dataUsers.put(id, user);
        return user;
    }

    @Override
    public void delete(int id) {
        if (!dataUsers.containsKey(id)) {
            throw new DataNotFoundException("Пользователь с id=" + id + " не найден");
        }
        dataUsers.remove(id);
    }

    private int generatedId() {
        return ++id;
    }

    private boolean checkEmail(String email) {
        return dataUsers.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }
}

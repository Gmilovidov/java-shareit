package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers();

    UserDto getUserById(int id);

    UserDto createUser(UserDto userDto);

    UserDto update(int id, UserDto userDto);

    void delete(int id);
}

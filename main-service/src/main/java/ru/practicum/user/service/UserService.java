package ru.practicum.user.service;

import ru.practicum.user.dto.UserDtoNew;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDtoNew userDtoNew);

    void deleteUserById(Long id);

    List<UserDto> findAllByIds(List<Long> ids, int from, int size);
}

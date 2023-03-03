package ru.practicum.user.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.user.dto.UserDtoNew;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static User toUser(UserDtoNew userDto) {
        return User.builder()
            .name(userDto.getName())
            .email(userDto.getEmail())
            .build();
    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .build();
    }

    public static List<UserDto> toUserDtoList(List<User> users) {
        List<UserDto> usersDtoFull = new ArrayList<>();
        for (User user : users) {
            usersDtoFull.add(toUserDto(user));
        }
        return usersDtoFull;
    }
}

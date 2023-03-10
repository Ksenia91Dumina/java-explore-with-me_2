package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserDtoNew;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    @Transactional
    public UserDto createUser(UserDtoNew userDto) {
        if (userDto.getName().isBlank()) {
            throw new BadRequestException("Имя должно быть заполнено");
        }
        repository.findByNameOrderByName()
            .stream()
            .filter(name -> name.equals(userDto.getName()))
            .forEachOrdered(name -> {
                throw new ConflictException(
                    String.format("Пользователь с именем %s - уже существует", name));
            });
        return UserMapper.toUserDto(repository.save(UserMapper.toUser(userDto)));
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        repository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id = %s не найден", id)));
        repository.deleteById(id);
    }

    @Override
    public List<UserDto> findAllByIds(List<Long> ids, int from, int size) {
        if (!ids.isEmpty()) {
            return UserMapper.toUserDtoList(repository.findAllById(ids));
        } else {
            return UserMapper.toUserDtoList(repository.findAll()
                .stream().skip(from).limit(size).collect(Collectors.toList()));
        }
    }
}

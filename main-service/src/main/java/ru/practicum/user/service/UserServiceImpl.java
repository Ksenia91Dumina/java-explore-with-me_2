package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.addition.MyPageRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserDtoNew;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    @Transactional
    public UserDto createUser(UserDtoNew userDto) {
        if(userDto.getName().isBlank()){
            throw new ConflictException("Имя должно быть заполнено");
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
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Пользователя с id ={} не существует", id));
        }
    }

    @Override
    public List<UserDto> findAllByIds(List<Long> ids, int from, int size) {
        MyPageRequest pageRequest = MyPageRequest.of(from, size);
        if (!ids.isEmpty()) {
            return UserMapper.toUserDtoList(
                repository.findAllByIdIn(ids, pageRequest).toList());
        } else {
            return UserMapper.toUserDtoList(repository.findAll(pageRequest));
        }
    }
}

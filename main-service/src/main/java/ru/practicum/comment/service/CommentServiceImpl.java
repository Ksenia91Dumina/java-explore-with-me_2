package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.addition.MyPageRequest;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoNew;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository repository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentDto createComment(Long eventId, Long userId, CommentDtoNew commentDto) {
        LocalDateTime created = LocalDateTime.now();
        if (commentDto.getText().isEmpty()) {
            throw new BadRequestException("Текст комментария не может быть пустым");
        }
        Event event = eventValidation(eventId);
        User user = userValidation(userId);
        if (event.getState().equals(EventState.PUBLISHED)) {
            Comment comment = CommentMapper.toComment(commentDto);
            comment.setAuthor(user);
            comment.setEvent(event);
            comment.setCreated(created);
            return CommentMapper.toCommentDto(repository.save(comment));
        } else {
            throw new ConflictException(
                String.format("Событие %s не опубликовано. Добавить комментарий невозможно.", eventId));
        }
    }

    @Override
    public List<CommentDto> getALlComments(Long eventId, int from, int size) {
        MyPageRequest pageRequest = MyPageRequest.of(from, size);
        eventValidation(eventId);
        return repository.findCommentOrderByEventId(eventId, pageRequest)
            .stream()
            .map(CommentMapper::toCommentDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getCommentsByUserId(Long userId, int from, int size) {
        MyPageRequest pageRequest = MyPageRequest.of(from, size);
        userValidation(userId);
        return repository.findCommentOrderByUserId(userId, pageRequest)
            .stream()
            .map(CommentMapper::toCommentDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCommentById(Long eventId, Long commentId, Long userId) {
        Comment comments = repository.findById(commentId)
            .orElseThrow(() -> new NotFoundException(String.format("Комментарий с id = {} не найден", commentId)));
        if (comments.getAuthor().getId().equals(userId)) {
            eventValidation(eventId);
            repository.deleteById(commentId);
        } else {
            throw new ConflictException(
                String.format("Пользователь %s не может удалить чужой комментарий.", userId));
        }
    }

    @Override
    @Transactional
    public void deleteCommentsByEventId(Long eventId) {
        eventValidation(eventId);
        repository.deleteCommentOrderByEventId(eventId);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long eventId, Long commentId, Long userId, CommentDto commentDto) {
        if (commentDto.getAuthor().equals(userId)) {
            eventValidation(eventId);
            if (commentDto.getText().isEmpty()) {
                throw new BadRequestException("Текст комментария не может быть пустым");
            }
            Comment comment = repository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Комментарий с id = {} не найден", commentId)));
            comment.setText(commentDto.getText());
            return CommentMapper.toCommentDto(repository.save(comment));
        } else {
            throw new ConflictException(
                String.format("Пользователь %s не может обновить чужой комментарий.", userId));
        }
    }

    private Event eventValidation(Long eventId) {
        return eventRepository.findById(eventId)
            .orElseThrow(() -> new NotFoundException(String.format("Событие с id = {} не найдено", eventId)));
    }

    private User userValidation(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id = {} не найден", userId)));
    }

}

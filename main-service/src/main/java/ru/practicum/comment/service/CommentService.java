package ru.practicum.comment.service;

import org.springframework.stereotype.Service;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoNew;

import java.util.List;

@Service
public interface CommentService {

    CommentDto createComment(Long eventId, Long userId, CommentDtoNew commentDto);

    List<CommentDto> getALlComments(Long eventId, int from, int size);

    List<CommentDto> getCommentsByUserId(Long userId, int from, int size);

    void deleteCommentById(Long eventId, Long commentId, Long userId);
    void deleteCommentsByEventId(Long eventId);

    CommentDto updateComment(Long eventId, Long commentId, Long userId, CommentDto commentDto);
}

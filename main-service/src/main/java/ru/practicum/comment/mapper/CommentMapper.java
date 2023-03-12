package ru.practicum.comment.mapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoNew;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.user.mapper.UserMapper;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentMapper {
    public static Comment toComment(CommentDtoNew commentDtoNew) {
        return Comment.builder()
            .text(commentDtoNew.getText())
            .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
            .id(comment.getId())
            .text(comment.getText())
            .event(EventMapper.toEventDtoShort(comment.getEvent()))
            .author(UserMapper.toUserDto(comment.getAuthor()))
            .createdOn(comment.getCreatedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .build();
    }

    public static List<CommentDto> toCommentDtoList(List<Comment> comments) {
        return comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
    }
}

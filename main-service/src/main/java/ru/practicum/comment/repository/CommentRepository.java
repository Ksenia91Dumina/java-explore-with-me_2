package ru.practicum.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.addition.MyPageRequest;
import ru.practicum.comment.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    void deleteCommentOrderByEventId(Long eventId);
    List<Comment> findCommentOrderByEventId(Long eventId, MyPageRequest pageRequest);

    List<Comment> findCommentOrderByUserId(Long userId, MyPageRequest pageRequest);
}

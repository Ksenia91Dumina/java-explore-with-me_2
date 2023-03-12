package ru.practicum.comment.dto;

import lombok.*;
import ru.practicum.event.dto.EventDtoShort;
import ru.practicum.user.dto.UserDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private String text;
    private EventDtoShort event;
    private UserDto author;
    private String createdOn;
}

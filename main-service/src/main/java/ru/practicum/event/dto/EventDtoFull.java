package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.category.dto.CategoryDtoFull;
import ru.practicum.event.model.EventState;
import ru.practicum.user.dto.UserDtoShort;

import java.time.LocalDateTime;

@Data
@Builder
public class EventDtoFull {

    private Long id;

    private String annotation;

    private CategoryDtoFull category;

    private Long confirmedRequests;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private UserDtoShort initiator;

    private Location location;

    private Boolean paid;

    private Long participantLimit;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedDate;

    private Boolean requestModeration;

    private EventState state;

    private String title;

    private Long views;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Location {

        private Float lat;

        private Float lon;
    }

}

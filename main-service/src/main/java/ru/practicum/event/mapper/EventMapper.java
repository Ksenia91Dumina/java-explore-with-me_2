package ru.practicum.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.event.dto.EventDtoFull;
import ru.practicum.event.dto.EventDtoNew;
import ru.practicum.event.dto.EventDtoShort;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.user.mapper.UserMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    public static Event toEvent(EventDtoNew eventDtoNew) {
        Event event = new Event();
        event.setAnnotation(eventDtoNew.getAnnotation());
        event.setCreatedOn(LocalDateTime.now());
        event.setDescription(eventDtoNew.getDescription());
        event.setEventDate(toLocalDateTime(eventDtoNew.getEventDate()));
        event.setLocation(eventDtoNew.getLocation());
        event.setPaid(eventDtoNew.getPaid());
        event.setParticipantLimit(eventDtoNew.getParticipantLimit());
        event.setPublishedOn(toLocalDateTime(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        event.setRequestModeration(eventDtoNew.getRequestModeration());
        event.setRequestModeration(eventDtoNew.getRequestModeration());
        event.setState(EventState.PENDING);
        event.setTitle(eventDtoNew.getTitle());
        return event;
    }

    public static EventDtoFull toEventDtoFull(Event event) {
        return new EventDtoFull(
            event.getId(),
            event.getAnnotation(),
            CategoryMapper.toCategoryDtoFull(event.getCategory()),
            event.getConfirmedRequests(),
            event.getCreatedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            event.getDescription(),
            event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            UserMapper.toUserDtoShort(event.getInitiator()),
            event.getLocation(),
            event.getPaid(),
            event.getParticipantLimit(),
            event.getPublishedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            event.getRequestModeration(),
            event.getState(),
            event.getTitle(),
            event.getViews());
    }

    public static List<EventDtoFull> toEventDtoFull(List<Event> events) {
        List<EventDtoFull> eventsDtoFull = new ArrayList<>();
        for (Event event : events) {
            eventsDtoFull.add(toEventDtoFull(event));
        }
        return eventsDtoFull;
    }

    public static EventDtoShort toEventDtoShort(Event event) {
        return new EventDtoShort(
            event.getId(),
            event.getAnnotation(),
            CategoryMapper.toCategoryDtoFull(event.getCategory()),
            event.getConfirmedRequests(),
            event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            UserMapper.toUserDtoShort(event.getInitiator()),
            event.getPaid(),
            event.getTitle(),
            event.getViews());
    }

    public static List<EventDtoShort> toEventDtoShort(List<Event> events) {
        List<EventDtoShort> eventDtoShorts = new ArrayList<>();
        for (Event event : events) {
            eventDtoShorts.add(toEventDtoShort(event));
        }
        return eventDtoShorts;
    }

    public static List<EventDtoShort> toEventShortDtoList(List<Event> events) {
        return events.stream().map(EventMapper::toEventDtoShort).collect(Collectors.toList());
    }


    public static LocalDateTime toLocalDateTime(String stringDateTime) {
        return LocalDateTime.parse(stringDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}

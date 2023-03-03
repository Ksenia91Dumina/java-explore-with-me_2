package ru.practicum.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.category.dto.CategoryDtoFull;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventDtoFull;
import ru.practicum.event.dto.EventDtoNew;
import ru.practicum.event.dto.EventDtoShort;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.user.dto.UserDtoShort;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    public static Event toNewEvent(EventDtoNew eventDtoNew, User initiator, Location location, Category category) {
        return Event.builder()
            .id(null)
            .annotation(eventDtoNew.getAnnotation())
            .category(category)
            .confirmedRequests(null)
            .description(eventDtoNew.getDescription())
            .createdDate(LocalDateTime.now())
            .eventDate(eventDtoNew.getEventDate())
            .initiator(initiator)
            .location(location)
            .paid(eventDtoNew.getPaid())
            .participantLimit(eventDtoNew.getParticipantLimit())
            .publishedDate(null)
            .requestModeration(eventDtoNew.getRequestModeration())
            .state(EventState.PENDING)
            .title(eventDtoNew.getTitle())
            .views(null)
            .build();
    }


    public static EventDtoFull toEventDtoFull(Event event) {
        return EventDtoFull.builder()
            .id(event.getId())
            .annotation(event.getAnnotation())
            .category(new CategoryDtoFull(
                event.getCategory().getId(),
                event.getCategory().getName()))
            .confirmedRequests(event.getConfirmedRequests())
            .createdDate(event.getCreatedDate())
            .description(event.getDescription())
            .eventDate(event.getEventDate())
            .initiator(new UserDtoShort(
                event.getInitiator().getId(),
                event.getInitiator().getName()))
            .location(new EventDtoFull.Location(
                event.getLocation().getLat(),
                event.getLocation().getLon()))
            .paid(event.getPaid())
            .participantLimit(event.getParticipantLimit())
            .publishedDate(event.getPublishedDate())
            .requestModeration(event.getRequestModeration())
            .state(event.getState())
            .title(event.getTitle())
            .views(event.getViews())
            .build();
    }

    public static List<EventDtoFull> toEventDtoFull(List<Event> events) {
        List<EventDtoFull> eventsDtoFull = new ArrayList<>();
        for (Event event : events) {
            eventsDtoFull.add(toEventDtoFull(event));
        }
        return eventsDtoFull;
    }

    public static EventDtoShort toEventDtoShort(Event event) {
        return EventDtoShort.builder()
            .id(event.getId())
            .annotation(event.getAnnotation())
            .category(new CategoryDtoFull(
                event.getCategory().getId(),
                event.getCategory().getName()))
            .confirmedRequests(event.getConfirmedRequests())
            .eventDate(event.getEventDate())
            .initiator(new UserDtoShort(
                event.getInitiator().getId(),
                event.getInitiator().getName()))
            .paid(event.getPaid())
            .title(event.getTitle())
            .views(event.getViews())
            .build();
    }

    public static List<EventDtoShort> toEventDtoShort(List<Event> events) {
        List<EventDtoShort> eventDtoShorts = new ArrayList<>();
        for (Event event : events) {
            eventDtoShorts.add(toEventDtoShort(event));
        }
        return eventDtoShorts;
    }
}

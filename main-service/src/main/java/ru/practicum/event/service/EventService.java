package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.model.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventDtoFull addEvent(EventDtoNew eventDtoNew, Long userId);

    EventDtoFull findEventByIdForInitiator(Long userId, Long eventId);

    List<EventDtoShort> findAllEventsForInitiator(Long userId, int from, int size);

    EventDtoFull updateEventByIdByInitiator(EventDtoUpdateByUser eventDtoUpdateByUser, Long userId, Long eventId);

    List<EventDtoFull> getAllEventsAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventDtoFull updateEventByIdByAdmin(EventDtoUpdateByAdmin eventDtoUpdateByAdmin, Long eventId);

    List<EventDtoShort> getAllEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                 LocalDateTime rangeEnd, Boolean onlyAvailable,
                 String sort, int from, int size, HttpServletRequest request);

    EventDtoFull getEventById(Long id, HttpServletRequest request);
}

package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.addition.PageRequestOverride;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.category.service.CategoryServiceImpl;
import ru.practicum.client.StatsClient;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.stat.StatsEndpointMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static ru.practicum.event.model.EventState.*;
import static ru.practicum.event.model.StateAction.*;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private final UserRepository userRepository;
    private final CategoryServiceImpl categoryService;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventDtoFull addEvent(EventDtoNew eventDtoNew, Long userId) {
        User user = initiatorValidation(userId);
        if (eventDtoNew.getEventDate() != null) {
            LocalDateTime dateTime = EventMapper.toLocalDateTime(eventDtoNew.getEventDate());
            if (dateTime.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Введена некорректная дата - должна быть будущая дата");
            }
        }
        if (eventDtoNew.getAnnotation() == null) {
            throw new BadRequestException("Поле annotation должно быть заполнено");
        }
        Event event = EventMapper.toEvent(eventDtoNew);
        Category category = categoryRepository.findById(eventDtoNew.getCategory())
            .orElseThrow(() -> new NotFoundException(String.format("Категория с id = {} не найдена",
                eventDtoNew.getCategory())));
        event.setInitiator(user);
        event.setCategory(category);
        event.setConfirmedRequests(0L);
        event.setViews(0L);
        return EventMapper.toEventDtoFull(eventRepository.save(event));
    }

    @Override
    public EventDtoFull findEventByIdForInitiator(Long userId, Long eventId) {
        initiatorValidation(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
            .orElseThrow(() -> new NotFoundException(String.format("Событие с id = {} не найдено", eventId)));
        return EventMapper.toEventDtoFull(event);
    }

    @Override
    public List<EventDtoShort> findAllEventsForInitiator(Long userId, int from, int size) {
        PageRequestOverride pageRequest = PageRequestOverride.of(from, size);
        initiatorValidation(userId);
        return EventMapper.toEventDtoShort(
            eventRepository.findAllByInitiatorId(userId, pageRequest).toList());
    }

    @Override
    @Transactional
    public EventDtoFull updateEventByIdByInitiator(EventDtoUpdateByUser eventDtoUpdateByUser, Long userId,
                                                   Long eventId) {
        initiatorValidation(userId);
        eventRepository.findById(eventId)
            .orElseThrow(() -> new NotFoundException(String.format("Событие с id = {} не найдено", eventId)));
        if (eventDtoUpdateByUser.getEventDate() != null) {
            LocalDateTime dateTime = EventMapper.toLocalDateTime(eventDtoUpdateByUser.getEventDate());
            if (dateTime.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Введена некорректная дата - должна быть будущая дата");
            }
        }
        Event event = getByIdAndInitiatorId(eventId, userId);
        if (PUBLISHED.equals(event.getState())) {
            throw new ConflictException("Событие не должно быть опубликовано");
        }
        if ((!PENDING.equals(event.getState())) && (!CANCELED.equals(event.getState()))) {
            throw new ConflictException("Могут быть изменены только события со статусом Pending или Canceled");
        }
        if (CANCEL_REVIEW.equals(eventDtoUpdateByUser.getStateAction())) {
            event.setState(CANCELED);
        } else if (SEND_TO_REVIEW.equals(eventDtoUpdateByUser.getStateAction())) {
            event.setState(PENDING);
        } else {
            throw new ConflictException("Поле stateAction должно содержать значение CANCEL_REVIEW или SEND_TO_REVIEW");
        }
        if (eventDtoUpdateByUser.getAnnotation() != null) {
            event.setAnnotation(eventDtoUpdateByUser.getAnnotation());
        }
        if (eventDtoUpdateByUser.getCategory() != null) {
            event.setCategory(categoryValidation(eventDtoUpdateByUser.getCategory()));
        }
        if (eventDtoUpdateByUser.getDescription() != null) {
            event.setAnnotation(eventDtoUpdateByUser.getDescription());
        }
        if (eventDtoUpdateByUser.getEventDate() != null) {
            event.setEventDate(EventMapper.toLocalDateTime(eventDtoUpdateByUser.getEventDate()));
        }
        if (eventDtoUpdateByUser.getLocation() != null) {
            event.setLocation(eventDtoUpdateByUser.getLocation());
        }
        if (eventDtoUpdateByUser.getPaid() != null) {
            event.setPaid(eventDtoUpdateByUser.getPaid());
        }
        if (eventDtoUpdateByUser.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDtoUpdateByUser.getParticipantLimit());
        }
        if (eventDtoUpdateByUser.getRequestModeration() != null) {
            event.setRequestModeration(eventDtoUpdateByUser.getRequestModeration());
        }
        if (eventDtoUpdateByUser.getTitle() != null) {
            event.setTitle(eventDtoUpdateByUser.getTitle());
        }
        return EventMapper.toEventDtoFull(eventRepository.save(event));
    }

    @Override
    public List<EventDtoFull> getAllEventsAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        PageRequestOverride pageRequest = PageRequestOverride.of(from, size);
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (rangeStart != null) {
            try {
                start = rangeStart;
            } catch (ConflictException e) {
                throw new ConflictException("Некорректное время начала диапазона " + rangeStart);
            }
        }
        if (rangeEnd != null) {
            try {
                end = rangeEnd;
            } catch (ConflictException e) {
                throw new ConflictException("Некорректное время окончания диапазона " + rangeEnd);
            }
        }

        start = (rangeStart != null) ? start : LocalDateTime.now();
        end = (rangeEnd != null) ? end : LocalDateTime.now().plusYears(300);

        if (start.isAfter(end)) {
            throw new ConflictException(
                "Время начала события не может быть позже, чем время окончания");
        }
        if (states == null) {
            states = new ArrayList<>();
            states.add(PENDING);
            states.add(CANCELED);
            states.add(PUBLISHED);
        }
        List<Event> events = new ArrayList<>();
        if (categories != null) {
            if (users != null) {
                events = eventRepository.findByInitiatorAndCategoriesAndState(users, categories, states, pageRequest);
            } else {
                events = eventRepository.findByCategoriesAndState(categories, states, pageRequest);
            }
        } else {
            if (users == null) {
                events = eventRepository.findByState(states, pageRequest);
            } else {
                events = eventRepository.findByInitiatorAndState(users, states, pageRequest);
            }
        }
        return events
            .stream()
            .map(EventMapper::toEventDtoFull)
            .collect(toList());
    }

    @Override
    @Transactional
    public EventDtoFull updateEventByIdByAdmin(EventDtoUpdateByAdmin eventDtoUpdateByAdmin, Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new NotFoundException(String.format("Событие с id = {} не найдено", eventId)));
        if (eventDtoUpdateByAdmin.getEventDate() != null) {
            LocalDateTime eventDate = EventMapper.toLocalDateTime(eventDtoUpdateByAdmin.getEventDate());
            LocalDateTime publishedOn = event.getPublishedOn();
            if (eventDate.isBefore(publishedOn.plusHours(1)) || eventDate.isBefore(LocalDateTime.now())) {
                throw new ConflictException("Дата события должна быть не ранее чем за час от даты публикации");
            }
        }
        if (PUBLISH_EVENT.equals(eventDtoUpdateByAdmin.getStateAction())) {
            if (!PENDING.equals(event.getState())) {
                throw new ConflictException(String.format("Невозможно опубликовать событие - неверно указан статус {}",
                    event.getState()));
            }
            event.setState(PUBLISHED);
        }
        if (REJECT_EVENT.equals(eventDtoUpdateByAdmin.getStateAction())) {
            if (PUBLISHED.equals(event.getState())) {
                throw new ConflictException(String.format("Невозможно опубликовать событие - неверно указан статус {}",
                    event.getState()));
            }
            event.setState(CANCELED);
        }
        if (eventDtoUpdateByAdmin.getAnnotation() != null) {
            event.setAnnotation(eventDtoUpdateByAdmin.getAnnotation());
        }
        if (eventDtoUpdateByAdmin.getCategory() != null) {
            event.setCategory(CategoryMapper.toCategory(
                categoryService.findById(eventDtoUpdateByAdmin.getCategory())));
        }
        if (eventDtoUpdateByAdmin.getDescription() != null) {
            event.setDescription(eventDtoUpdateByAdmin.getDescription());
        }
        if (eventDtoUpdateByAdmin.getEventDate() != null) {
            event.setEventDate(EventMapper.toLocalDateTime(eventDtoUpdateByAdmin.getEventDate()));
        }
        if (eventDtoUpdateByAdmin.getLocation() != null) {
            event.setLocation(eventDtoUpdateByAdmin.getLocation());
        }
        if (eventDtoUpdateByAdmin.getPaid() != null) {
            event.setPaid(eventDtoUpdateByAdmin.getPaid());
        }
        if (eventDtoUpdateByAdmin.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDtoUpdateByAdmin.getParticipantLimit());
        }
        if (eventDtoUpdateByAdmin.getRequestModeration() != null) {
            event.setRequestModeration(eventDtoUpdateByAdmin.getRequestModeration());
        }
        if (eventDtoUpdateByAdmin.getTitle() != null) {
            event.setTitle(eventDtoUpdateByAdmin.getTitle());
        }
        return EventMapper.toEventDtoFull(eventRepository.save(event));
    }

    @Override
    public List<EventDtoShort> getAllEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd, Boolean onlyAvailable, String sort,
                                            int from, int size, HttpServletRequest request) {
        statsClient.save(StatsEndpointMapper.toEndpointHit(request));
        rangeStart = (rangeStart != null) ? rangeStart : LocalDateTime.now();
        rangeEnd = (rangeEnd != null) ? rangeEnd : LocalDateTime.now().plusYears(300);
        if (rangeStart.isAfter(rangeEnd)) {
            throw new ConflictException("Время окончания события не может быть раньше времени начала события");
        }
        List<Event> events;
        if (categories != null) {
            events = eventRepository.findByCategoryIdsAndText(text, categories);
        } else {
            events = eventRepository.findByText(text);
        }
        return events
            .stream()
            .map(EventMapper::toEventDtoShort)
            .sorted(Comparator.comparing(EventDtoShort::getEventDate))
            .skip(from)
            .limit(size)
            .collect(toList());
    }

    @Override
    public EventDtoFull getEventById(Long id, HttpServletRequest request) {
        statsClient.save(StatsEndpointMapper.toEndpointHit(request));
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format("Событие с id = {} не найдено", id)));
        if (!PUBLISHED.equals(event.getState())) {
            throw new ConflictException(String.format("Событие с id = {} не опубликовано", id));
        }
        event.setViews(event.getViews() + 1);
        return EventMapper.toEventDtoFull(eventRepository.save(event));
    }

    private User initiatorValidation(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id ={} не найден", userId)));
    }

    private Category categoryValidation(Long catId) {
        return categoryRepository.findById(catId)
            .orElseThrow(() -> new NotFoundException(String.format("Категория с id ={} не найден", catId)));
    }

    private Event getByIdAndInitiatorId(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
            .orElseThrow(
                () -> new NotFoundException(String.format("Пользователь с id = {} или событие с id = {} не найдены",
                    userId, eventId)));
    }
}

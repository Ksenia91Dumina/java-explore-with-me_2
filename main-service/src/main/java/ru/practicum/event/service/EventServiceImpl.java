package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.addition.MyPageRequest;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.category.service.CategoryServiceImpl;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.stat.client.StatsClient;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static ru.practicum.event.model.EventState.*;
import static ru.practicum.event.model.StateAction.CANCEL_REVIEW;
import static ru.practicum.event.model.StateAction.SEND_TO_REVIEW;

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
        if (eventDtoNew.getAnnotation() == null) {
            throw new BadRequestException("Поле annotation должно быть заполнено");
        }
        Event event = EventMapper.toEvent(eventDtoNew);
        event.setInitiator(user);
        event.setCategory(categoryRepository.findById(eventDtoNew.getCategory())
            .orElseThrow(() -> new NotFoundException(String.format("Категория с id = {} не найдена",
                eventDtoNew.getCategory()))));
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
    public List<EventDtoFull> findAllEventsForInitiator(Long userId, int from, int size) {
        MyPageRequest pageRequest = MyPageRequest.of(from, size);
        initiatorValidation(userId);
        return EventMapper.toEventDtoFull(
            eventRepository.findAllByInitiatorId(userId, pageRequest).toList());
    }

    @Override
    @Transactional
    public EventDtoFull updateEventByIdByInitiator(EventDtoUpdateByUser eventDtoUpdateByUser, Long userId,
                                                   Long eventId) {
        initiatorValidation(userId);
        eventRepository.findById(eventId)
            .orElseThrow(() -> new NotFoundException(String.format("Событие с id = {} не найдено", eventId)));

        Event event = getByIdAndInitiatorId(eventId, userId);

        if (event.getState().equals(PUBLISHED)) {
            throw new ConflictException("Событие не должно быть опубликовано");
        }
        if (!event.getState().equals(PENDING) && !event.getState().equals(CANCELED)) {
            throw new ConflictException("Могут быть изменены только события со статусом Pending или Canceled");
        }
        if (eventDtoUpdateByUser.getStateAction().equals(CANCEL_REVIEW.toString())) {
            event.setState(CANCELED);
        } else if (eventDtoUpdateByUser.getStateAction().equals(SEND_TO_REVIEW.toString())) {
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
        MyPageRequest pageRequest = MyPageRequest.of(from, size);
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (rangeStart != null) {
            try {
                start = rangeStart;
            } catch (NotFoundException e) {
                throw new NotFoundException("Некорректное время начала диапазона " + rangeStart);
            }
        }
        if (rangeEnd != null) {
            try {
                end = rangeEnd;
            } catch (NotFoundException e) {
                throw new NotFoundException("Некорректное время окончания диапазона " + rangeEnd);
            }
        }

        start = (rangeStart != null) ? start : LocalDateTime.now();
        end = (rangeEnd != null) ? end : LocalDateTime.now().plusYears(300);

        if (start.isAfter(end)) {
            throw new NotFoundException(
                "Время начала события не может быть позже, чем время окончания");
        }
        if (states == null) {
            states = new ArrayList<>();
            states.add(EventState.PENDING);
            states.add(EventState.CANCELED);
            states.add(EventState.PUBLISHED);
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
        if (eventDtoUpdateByAdmin.getStateAction().equals(StateAction.PUBLISH_EVENT.toString())) {
            if (!event.getState().equals(PENDING)) {
                throw new ConflictException(String.format("Невозможно опубликовать событие - неверно указан статус {}",
                    event.getState()));
            }
            event.setState(PUBLISHED);
        }
        if (eventDtoUpdateByAdmin.getStateAction().equals(StateAction.REJECT_EVENT.toString())) {
            if (event.getState().equals(PUBLISHED)) {
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
        saveEndpointHit(request);
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
    public EventDtoFull getEventById(Long id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format("Событие с id = {} не найдено", id)));
        if (!event.getState().equals(PUBLISHED)) {
            throw new ConflictException(String.format("Событие с id = {} не опубликовано", id));
        }
        event.setViews(event.getViews() + 1);
        return EventMapper.toEventDtoFull(eventRepository.save(event));
    }

    private User initiatorValidation(Long userId) {
        User initiator = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id ={} не найден", userId)));
        return initiator;
    }

    private Category categoryValidation(Long catId) {
        Category category = categoryRepository.findById(catId)
            .orElseThrow(() -> new NotFoundException(String.format("Категория с id ={} не найден", catId)));
        return category;
    }

    private void saveEndpointHit(HttpServletRequest request) {
        EndpointHitDto endpointHit = new EndpointHitDto(
            request.getServerName(),
            request.getRequestURI(),
            request.getRemoteAddr(),
            LocalDateTime.now());
        statsClient.createStat(endpointHit);
    }

    private Event getByIdAndInitiatorId(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
            .orElseThrow(
                () -> new NotFoundException(String.format("Пользователь с id = {} или событие с id = {} не найдены",
                    userId, eventId)));
    }
}

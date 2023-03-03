package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.addition.MyPageRequest;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
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
import static ru.practicum.event.model.EventState.PUBLISHED;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private final UserRepository userRepository;

    private final LocationRepository locationRepository;

    private final CategoryRepository categoryRepository;

    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventDtoFull addEvent(EventDtoNew eventDtoNew, Long userId) {
        initiatorValidation(userId);
        Category category = categoryRepository.findById(eventDtoNew.getCategory())
            .orElseThrow(() -> new NotFoundException("Категории не существует"));
        if (eventDtoNew.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException(
                "Время начала события не может быть раньше, чем через два часа от текущего времени");
        }
        Location location = locationRepository.save(LocationMapper.toLocation(eventDtoNew.getLocation()));
        Event event = eventRepository.save(EventMapper.toNewEvent(eventDtoNew, userRepository.findById(userId).get(),
            location, category));
        return EventMapper.toEventDtoFull(event);
    }

    @Override
    public EventDtoFull findEventByIdForInitiator(Long userId, Long eventId) {
        initiatorValidation(userId);
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new NotFoundException(String.format("Событие с id = {} не найдено", eventId)));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException(String.format("Пользователь с id = {} является инициатором событияс id = {}",
                userId, eventId));
        }
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
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new NotFoundException(String.format("Событие с id = {} не найдено", eventId)));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException(String.format("Пользователь с id = {} является инициатором событияс id = {}",
                userId, eventId));
        }
        if (event.getState().equals(PUBLISHED)) {
            throw new ConflictException("Невозможно обновить событие");
        }
        if (eventDtoUpdateByUser.getAnnotation() != null) {
            event.setAnnotation(eventDtoUpdateByUser.getAnnotation());
        }
        if (eventDtoUpdateByUser.getCategory() != null) {
            Category category = categoryRepository.findById(eventDtoUpdateByUser.getCategory())
                .orElseThrow(() -> new NotFoundException(String.format("Категория с id = {} не найдена",
                    eventDtoUpdateByUser.getCategory())));
            event.setCategory(category);
        }
        if (eventDtoUpdateByUser.getDescription() != null) {
            event.setDescription(eventDtoUpdateByUser.getDescription());
        }
        if (eventDtoUpdateByUser.getEventDate() != null) {
            eventDateValidation(eventDtoUpdateByUser.getEventDate());
            event.setEventDate(eventDtoUpdateByUser.getEventDate());
        }
        if (eventDtoUpdateByUser.getLocation() != null) {
            Location location = locationRepository.findById(event.getLocation().getId()).get();
            location.setLon(eventDtoUpdateByUser.getLocation().getLon());
            location.setLat(eventDtoUpdateByUser.getLocation().getLat());
            locationRepository.save(location);
            event.setLocation(location);
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
        if (eventDtoUpdateByUser.getStateAction() != null) {
            if (event.getState() == EventState.CANCELED
                && eventDtoUpdateByUser.getStateAction() == StateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (event.getState() == EventState.PENDING
                && eventDtoUpdateByUser.getStateAction() == StateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            } else {
                throw new ConflictException("Невозможно обновить статус для события");
            }
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
        if ((categories != null) && (users != null)) {
            events = eventRepository.findByInitiatorAndCategoriesAndState(users, categories, states, pageRequest);
        }
        if (categories == null) {
            if (users == null) {
                events = eventRepository.findByState(states, pageRequest);
            } else {
                events = eventRepository.findByInitiatorAndState(users, states, pageRequest);
            }
        } else {
            if (users == null) {
                events = eventRepository.findByCategoriesAndState(categories, states, pageRequest);
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
        if (eventDtoUpdateByAdmin.getAnnotation() != null) {
            event.setAnnotation(eventDtoUpdateByAdmin.getAnnotation());
        }
        if (eventDtoUpdateByAdmin.getCategory() != null) {
            Category category = categoryRepository.findById(eventDtoUpdateByAdmin.getCategory())
                .orElseThrow(() -> new NotFoundException(String.format("Категория с id = {} не найдена",
                    eventDtoUpdateByAdmin.getCategory())));
            event.setCategory(category);
        }
        if (eventDtoUpdateByAdmin.getDescription() != null) {
            event.setDescription(eventDtoUpdateByAdmin.getDescription());
        }
        if (eventDtoUpdateByAdmin.getEventDate() != null && event.getCreatedDate() != null) {
            eventDateValidation(eventDtoUpdateByAdmin.getEventDate());
            event.setEventDate(eventDtoUpdateByAdmin.getEventDate());
        }
        if (eventDtoUpdateByAdmin.getLocation() != null) {
            Location location = locationRepository.findById(event.getLocation().getId()).get();
            location.setLon(eventDtoUpdateByAdmin.getLocation().getLon());
            location.setLat(eventDtoUpdateByAdmin.getLocation().getLat());
            locationRepository.save(location);
            event.setLocation(location);
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
        if (eventDtoUpdateByAdmin.getStateAction() != null) {
            if (event.getState() == EventState.PENDING
                && eventDtoUpdateByAdmin.getStateAction() == StateAction.PUBLISH_EVENT) {
                event.setState(PUBLISHED);
            } else if (event.getState() == EventState.PENDING
                && eventDtoUpdateByAdmin.getStateAction() == StateAction.REJECT_EVENT) {
                event.setState(EventState.CANCELED);
            } else {
                throw new ConflictException("Невозможно обновить статус для события");
            }
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

        event.setViews(event.getViews() + 1);
        return EventMapper.toEventDtoFull(eventRepository.save(event));
    }

    private void eventDateValidation(LocalDateTime date) {
        if (date.isBefore(LocalDateTime.now().plusHours(2L))) {
            throw new ConflictException("Некорректная дата");
        }
    }

    private void initiatorValidation(Long userId) {
        User initiator = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id ={} не найден", userId)));
    }

    private void saveEndpointHit(HttpServletRequest request) {
        EndpointHitDto endpointHit = new EndpointHitDto(
            request.getServerName(),
            request.getRequestURI(),
            request.getRemoteAddr(),
            LocalDateTime.now());
        statsClient.createStat(endpointHit);
    }
}

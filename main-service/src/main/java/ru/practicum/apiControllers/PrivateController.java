package ru.practicum.apiControllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventDtoFull;
import ru.practicum.event.dto.EventDtoNew;
import ru.practicum.event.dto.EventDtoUpdateByUser;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestDtoUpdateStatus;
import ru.practicum.request.dto.RequestDtoUpdated;
import ru.practicum.request.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateController {

    private final EventService eventService;

    private final RequestService requestService;

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    protected EventDtoFull createEvent(@PathVariable Long userId, @RequestBody @Valid EventDtoNew eventDtoNew) {
        log.info("Получен запрос на создание события {} от пользователя с id {}", eventDtoNew, userId);
        return eventService.addEvent(eventDtoNew, userId);
    }

    @GetMapping("/events/{eventId}")
    protected EventDtoFull findEventByIdForInitiator(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Получен запрос на получение события с id {} от пользователя с id {}", eventId, userId);
        return eventService.findEventByIdForInitiator(userId, eventId);
    }

    @GetMapping("/events")
    protected List<EventDtoFull> findAllEventsForInitiator(@PathVariable Long userId,
                                                           @RequestParam(required = false, defaultValue = "0")
                                                           @PositiveOrZero int from,
                                                           @RequestParam(required = false, defaultValue = "10")
                                                           @PositiveOrZero int size) {
        log.info("Получен запрос на получение списка всех событий для пользователя с id {}", userId);
        return eventService.findAllEventsForInitiator(userId, from, size);
    }

    @PatchMapping("/events/{eventId}")
    protected EventDtoFull updateEventByIdByInitiator(@PathVariable Long userId, @PathVariable Long eventId,
                                                      @RequestBody @Valid EventDtoUpdateByUser eventDtoUpdateByUser) {
        log.info("Получен запрос на обновление события с id {} от пользователя с id {}", eventId, userId);
        return eventService.updateEventByIdByInitiator(eventDtoUpdateByUser, userId, eventId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    protected RequestDto createRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("Получен запрос на участие в событии с id {} от пользователя с id {}", eventId, userId);
        return requestService.createRequest(userId, eventId);
    }

    @GetMapping("/requests")
    protected List<RequestDto> findRequestByUserId(@PathVariable Long userId) {
        log.info("Получен запрос на получение информации о заявках пользователя с id {}", userId);
        return requestService.findRequestByRequesterId(userId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    protected RequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("Получен запрос на отмену заявки на участие в событии с id {} от пользователя с id {}", requestId,
            userId);
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    protected List<RequestDto> findRequestsForEventInitiator(
        @PathVariable Long userId,
        @PathVariable Long eventId) {
        log.info("Получен запрос на получение заявок на участие в событии с id {}", eventId);
        return requestService.findRequestsForEventInitiator(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    protected RequestDtoUpdated updateRequestStatusByEventInitiator(
        @PathVariable Long userId,
        @PathVariable Long eventId,
        @RequestBody RequestDtoUpdateStatus participationRequestDtoStatusUpdate) {
        log.info("Получен запрос на изменение статуса заявок на участие в событии с id {}", eventId);
        return requestService.updateRequestStatusByEventInitiator(
            userId, eventId, participationRequestDtoStatusUpdate);
    }
}

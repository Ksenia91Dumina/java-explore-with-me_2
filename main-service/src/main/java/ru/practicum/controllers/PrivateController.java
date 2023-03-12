package ru.practicum.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoNew;
import ru.practicum.comment.service.CommentService;
import ru.practicum.event.dto.EventDtoFull;
import ru.practicum.event.dto.EventDtoNew;
import ru.practicum.event.dto.EventDtoShort;
import ru.practicum.event.dto.EventDtoUpdateByUser;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestDtoUpdateStatus;
import ru.practicum.request.dto.RequestDtoUpdated;
import ru.practicum.request.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
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

    private final CommentService commentService;

    @PostMapping("/events")
    public ResponseEntity<EventDtoFull> createEvent(@PathVariable Long userId,
                                                    @RequestBody @Valid EventDtoNew eventDtoNew) {
        log.info("Получен запрос на создание события {} от пользователя с id {}", eventDtoNew, userId);
        return new ResponseEntity<>(eventService.addEvent(eventDtoNew, userId), HttpStatus.CREATED);
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventDtoFull> findEventByIdForInitiator(@PathVariable Long userId,
                                                                  @PathVariable Long eventId) {
        log.info("Получен запрос на получение события с id {} от пользователя с id {}", eventId, userId);
        return new ResponseEntity<>(eventService.findEventByIdForInitiator(userId, eventId), HttpStatus.OK);
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventDtoShort>> findAllEventsForInitiator(@PathVariable Long userId,
                                                                         @RequestParam(required = false, defaultValue = "0")
                                                                         @PositiveOrZero int from,
                                                                         @RequestParam(required = false, defaultValue = "10")
                                                                         @Positive int size) {
        log.info("Получен запрос на получение списка всех событий для пользователя с id {}", userId);
        return ResponseEntity.ok(eventService.findAllEventsForInitiator(userId, from, size));
    }

    @PatchMapping("/events/{eventId}")
    public ResponseEntity<EventDtoFull> updateEventByIdByInitiator(@PathVariable Long userId,
                                                                   @PathVariable Long eventId,
                                                                   @RequestBody
                                                                   EventDtoUpdateByUser eventDtoUpdateByUser) {
        log.info("Получен запрос на обновление события с id {} от пользователя с id {}", eventId, userId);
        return ResponseEntity.ok(eventService.updateEventByIdByInitiator(eventDtoUpdateByUser, userId, eventId));
    }

    @PostMapping("/requests")
    public ResponseEntity<RequestDto> createRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("Получен запрос на участие в событии с id {} от пользователя с id {}", eventId, userId);
        return new ResponseEntity<>(requestService.createRequest(userId, eventId), HttpStatus.CREATED);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<RequestDto>> findRequestByUserId(@PathVariable Long userId) {
        log.info("Получен запрос на получение информации о заявках пользователя с id {}", userId);
        return ResponseEntity.ok(requestService.findRequestByRequesterId(userId));
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ResponseEntity<RequestDto> cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("Получен запрос на отмену заявки на участие в событии с id {} от пользователя с id {}", requestId,
            userId);
        return ResponseEntity.ok(requestService.cancelRequest(userId, requestId));
    }

    @GetMapping("/events/{eventId}/requests")
    public ResponseEntity<List<RequestDto>> findRequestsForEventInitiator(
        @PathVariable Long userId,
        @PathVariable Long eventId) {
        log.info("Получен запрос на получение заявок на участие в событии с id {}", eventId);
        return new ResponseEntity<>(requestService.findRequestsForEventInitiator(userId, eventId), HttpStatus.OK);
    }

    @PatchMapping("/events/{eventId}/requests")
    public ResponseEntity<RequestDtoUpdated> updateRequestStatusByEventInitiator(
        @PathVariable Long userId,
        @PathVariable Long eventId,
        @RequestBody RequestDtoUpdateStatus participationRequestDtoStatusUpdate) {
        log.info("Получен запрос на изменение статуса заявок на участие в событии с id {}", eventId);
        return ResponseEntity.ok(requestService.updateRequestStatusByEventInitiator(
            userId, eventId, participationRequestDtoStatusUpdate));
    }

    @PostMapping("/comments/{eventId}")
    public ResponseEntity<CommentDto> createComment(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @RequestBody @Valid CommentDtoNew commentDtoNew) {
        log.info("Получен запрос на создание комментария от пользователя с id {} к событию с id = {}", userId, eventId);
        return new ResponseEntity<>(commentService.createComment(eventId, userId, commentDtoNew), HttpStatus.CREATED);
    }

    @GetMapping("/comments")
    public ResponseEntity<List<CommentDto>> getAllByAuthorId(@PathVariable Long userId,
                                                             @PositiveOrZero
                                                             @RequestParam(name = "from", defaultValue = "0") int from,
                                                             @Positive @RequestParam(name = "size", defaultValue = "10")
                                                             int size) {
        log.info("Запрос на получение всех комментариев от пользователя с id {}", userId);
        return ResponseEntity.ok(commentService.getCommentsByAuthorId(userId, from, size));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Long userId,
                                                    @PathVariable Long commentId,
                                                    @RequestBody CommentDto commentDto) {
        log.info("Получен запрос на изменение комментария с id {} пользователем с id = {}", commentId, userId);
        return ResponseEntity.ok(commentService.updateComment(commentId, userId, commentDto));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteCommentByIdByAuthor(@PathVariable Long userId,
                                                          @PathVariable Long commentId) {
        log.info("Получен запрос на удаление комментария с id {} пользователем с id = {}", commentId, userId);
        commentService.deleteCommentByIdByAuthor(commentId, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

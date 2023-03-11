package ru.practicum.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDtoFull;
import ru.practicum.category.dto.CategoryDtoNew;
import ru.practicum.category.service.CategoryService;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;
import ru.practicum.compilation.dto.CompilationDtoFull;
import ru.practicum.compilation.dto.CompilationDtoNew;
import ru.practicum.compilation.dto.CompilationDtoUpdated;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.event.dto.EventDtoFull;
import ru.practicum.event.dto.EventDtoUpdateByAdmin;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserDtoNew;
import ru.practicum.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminController {

    private final UserService userService;

    private final CategoryService categoryService;

    private final CompilationService compilationService;

    private final EventService eventService;

    private final CommentService commentService;


    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid UserDtoNew userDtoNew) {
        log.info("Получен запрос на добавление пользователя {}", userDtoNew);
        return new ResponseEntity<>(userService.createUser(userDtoNew), HttpStatus.CREATED);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        log.info("Получен запрос на удаление пользователя с id {}", userId);
        userService.deleteUserById(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers(@RequestParam(name = "ids", required = false) List<Long> ids,
                                                     @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                                     int from,
                                                     @Positive @RequestParam(name = "size", defaultValue = "10")
                                                     int size) {
        log.info("Получен запрос на поиск пользователей с id: {}. from = {}, size = {}", ids, from, size);
        return new ResponseEntity<>(userService.findAllByIds(ids, from, size), HttpStatus.OK);
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDtoFull> createCategory(@RequestBody @Valid CategoryDtoNew categoryDtoNew) {
        log.info("Получен запрос на создание категории");
        return new ResponseEntity<>(categoryService.create(categoryDtoNew), HttpStatus.CREATED);
    }

    @DeleteMapping("/categories/{catId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long catId) {
        log.info("Получен запрос на удаление категории с id {}", catId);
        categoryService.deleteById(catId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/categories/{catId}")
    public ResponseEntity<CategoryDtoFull> updateCategory(@RequestBody @Valid CategoryDtoNew categoryDtoNew,
                                                          @PathVariable Long catId) {
        log.info("Получен запрос на обновление категории с id {}.", catId);
        return ResponseEntity.ok(categoryService.updateById(categoryDtoNew, catId));
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventDtoFull>> getEventsFullInfo(
        @RequestParam(name = "users", required = false) List<Long> users,
        @RequestParam(name = "states", required = false)
        List<EventState> states,
        @RequestParam(name = "categories", required = false)
        List<Long> categories,
        @RequestParam(name = "rangeStart", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime rangeStart,
        @RequestParam(name = "rangeEnd", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
        @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
        @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Получен запрос на поиск событий");
        return ResponseEntity.ok(eventService.getAllEventsAdmin(
            users, states, categories, rangeStart, rangeEnd, from, size));
    }

    @PatchMapping("/events/{eventId}")
    public ResponseEntity<EventDtoFull> updateEvent(@RequestBody EventDtoUpdateByAdmin eventDtoUpdateByAdmin,
                                                    @PathVariable Long eventId) {
        log.info("Получен запрос от администратора на обновление события с id {}", eventId);
        return ResponseEntity.ok(eventService.updateEventByIdByAdmin(eventDtoUpdateByAdmin, eventId));
    }

    @PostMapping("/compilations")
    public ResponseEntity<CompilationDtoFull> createCompilation(
        @RequestBody @Valid CompilationDtoNew compilationDtoNew) {
        log.info("Получен запрос на создание подборки событий");
        return new ResponseEntity<>(compilationService.createCompilation(compilationDtoNew), HttpStatus.CREATED);
    }

    @DeleteMapping("/compilations/{compId}")
    public ResponseEntity<Void> deleteCompilationById(@PathVariable Long compId) {
        log.info("Получен запрос на удаление подборки событий с id {}", compId);
        compilationService.deleteCompilationById(compId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/compilations/{compId}")
    public ResponseEntity<CompilationDtoFull> updateCompilationById(
        @RequestBody CompilationDtoUpdated compilationDtoUpdate,
        @PathVariable Long compId) {
        log.info("Получен запрос на обновление подборки событий с id {}", compId);
        return ResponseEntity.ok(compilationService.updateCompilationById(compilationDtoUpdate, compId));
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<CommentDto> getCommentById(@PathVariable Long commentId) {
        log.info("Запрос на получение информации о комментарии с id = {}", commentId);
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteCommentById(@PathVariable Long commentId) {
        log.info("Получен запрос на удаление комментария с id = {}", commentId);
        commentService.deleteCommentById(commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/comments/{eventId}")
    public ResponseEntity<Void> deleteCommentsByEventId(@PathVariable Long eventId) {
        log.info("Получен запрос на удаление комментариев для события с id = {}", eventId);
        commentService.deleteCommentsByEventId(eventId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

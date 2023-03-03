package ru.practicum.apiControllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDtoFull;
import ru.practicum.category.dto.CategoryDtoNew;
import ru.practicum.category.service.CategoryService;
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


    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid UserDtoNew userDtoNew) {
        log.info("Получен запрос на добавление пользователя {}", userDtoNew);
        return userService.createUser(userDtoNew);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Получен запрос на удаление пользователя с id {}", userId);
        userService.deleteUserById(userId);
    }

    @GetMapping("/users")
    public List<UserDto> getAllUsers(@RequestParam(name = "ids", required = false) List<Long> ids,
                                     @RequestParam(name = "from", defaultValue = "0") int from,
                                     @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Получен запрос на поиск пользователей с id: {}. from = {}, size = {}", ids, from, size);
        return userService.findAllByIds(ids, from, size);
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDtoFull createCategory(@RequestBody @Valid CategoryDtoNew categoryDtoNew) {
        log.info("Получен запрос на создание категории");
        return categoryService.create(categoryDtoNew);
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("Получен запрос на удаление категории с id {}", catId);
        categoryService.deleteById(catId);
    }

    @PatchMapping("/categories/{catId}")
    public CategoryDtoFull updateCategory(@RequestBody @Valid CategoryDtoNew categoryDtoNew,
                                          @PathVariable Long catId) {
        log.info("Получен запрос на обновление категории с id {}.", catId);
        return categoryService.updateById(categoryDtoNew, catId);
    }

    @GetMapping("/events")
    public List<EventDtoFull> getEventsFullInfo(@RequestParam(name = "users", required = false) List<Long> users,
                                                @RequestParam(name = "states", required = false)
                                                List<EventState> states,
                                                @RequestParam(name = "categories", required = false)
                                                List<Long> categories,
                                                @RequestParam(name = "rangeStart", required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                LocalDateTime rangeStart,
                                                @RequestParam(name = "rangeEnd", required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                @RequestParam(name = "from", defaultValue = "0") int from,
                                                @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Получен запрос на поиск событий");
        Pageable pageable = PageRequest.of(from / size, size);
        return eventService.getAllEventsAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/events/{eventId}")
    public EventDtoFull updateEvent(@RequestBody EventDtoUpdateByAdmin eventDtoUpdateByAdmin,
                                    @PathVariable Long eventId) {
        log.info("Получен запрос от администратора на обновление события с id {}", eventId);
        return eventService.updateEventByIdByAdmin(eventDtoUpdateByAdmin, eventId);
    }

    @PostMapping("/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDtoFull createCompilation(@RequestBody @Valid CompilationDtoNew compilationDtoNew) {
        log.info("Получен запрос на создание подборки событий");
        return compilationService.createCompilation(compilationDtoNew);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilationById(@PathVariable Long compId) {
        log.info("Получен запрос на удаление подборки событий с id {}", compId);
        compilationService.deleteCompilationById(compId);
    }

    @PatchMapping("/compilations/{compId}")
    public CompilationDtoFull updateCompilationById(@RequestBody CompilationDtoUpdated compilationDtoUpdate,
                                                    @PathVariable Long compId) {
        log.info("Получен запрос на обновление подборки событий с id {}", compId);
        return compilationService.updateCompilationById(compilationDtoUpdate, compId);
    }
}

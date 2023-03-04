package ru.practicum.apiControllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.dto.CategoryDtoFull;
import ru.practicum.category.service.CategoryService;
import ru.practicum.compilation.dto.CompilationDtoFull;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.event.dto.EventDtoFull;
import ru.practicum.event.dto.EventDtoShort;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicController {

    private final CategoryService categoryService;

    private final EventService eventService;


    private final CompilationService compilationService;

    private static final String APP = "ewm-service";

    @GetMapping("/categories")
    protected List<CategoryDtoFull> getAllCategories(@RequestParam(name = "from", defaultValue = "0") int from,
                                                     @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Получен запрос на получение всех категорий");
        return categoryService.getAll(from, size);
    }

    @GetMapping("/categories/{catId}")
    protected CategoryDtoFull findCategoryById(@PathVariable Long catId) {
        log.info("Получен запрос на получение категории с id {}", catId);
        return categoryService.findById(catId);
    }

    @GetMapping("/events")
    protected List<EventDtoShort> findAllPublicEvents(@RequestParam(name = "text", required = false) String text,
                                                      @RequestParam(name = "categories", required = false)
                                                      List<Long> categories,
                                                      @RequestParam(name = "paid", required = false) Boolean paid,
                                                      @RequestParam(name = "rangeStart", defaultValue = "1980-01-01 13:30:38")
                                                      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                      LocalDateTime rangeStart,
                                                      @RequestParam(name = "rangeEnd", defaultValue = "2050-01-01 00:00:00")
                                                      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                      LocalDateTime rangeEnd,
                                                      @RequestParam(required = false) Boolean onlyAvailable,
                                                      @RequestParam(required = false) String sort,
                                                      @RequestParam(name = "from", defaultValue = "0") int from,
                                                      @RequestParam(name = "size", defaultValue = "10") int size,
                                                      HttpServletRequest request) {
        log.info("Получен запрос на получение списка событий");
        return eventService.getAllEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort,
            from, size, request);
    }

    @GetMapping("/events/{id}")
    protected EventDtoFull findPublicEventById(@PathVariable Long id,
                                               HttpServletRequest httpServletRequest) {
        log.info("Получен запрос на получение события с id {}", id);
        return eventService.getEventById(id);
    }

    @GetMapping("/compilations")
    protected List<CompilationDtoFull> getCompilations(@RequestParam(name = "pinned", required = false) Boolean pinned,
                                                       @RequestParam(name = "from", defaultValue = "0") int from,
                                                       @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Получен запрос на получение подборки событий");
        return compilationService.getAllCompilations(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    protected CompilationDtoFull getPublicCompilationById(@PathVariable Long compId) {
        log.info("Получен запрос на получение подборки событий с id {}", compId);
        return compilationService.getCompilationById(compId);
    }

    private Sort getSortType(EventSort sort) {
        if (sort != null) {
            switch (sort) {
                case VIEWS:
                    return Sort.by("views").ascending();
                case EVENT_DATE:
                    return Sort.by("eventDate").ascending();
            }
        }
        return Sort.unsorted();
    }
}

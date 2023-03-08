package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.addition.PageRequestOverride;
import ru.practicum.compilation.dto.CompilationDtoFull;
import ru.practicum.compilation.dto.CompilationDtoNew;
import ru.practicum.compilation.dto.CompilationDtoUpdated;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository repository;

    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDtoFull createCompilation(CompilationDtoNew compilationDtoNew) {
        List<Event> events = new ArrayList<>();
        if (!compilationDtoNew.getEvents().isEmpty()) {
            events = eventRepository.findAllByIdIn(compilationDtoNew.getEvents());
        }
        if (compilationDtoNew.getPinned() == null) {
            compilationDtoNew.setPinned(false);
        }
        return CompilationMapper.toCompilationDtoFull(
            repository.save(CompilationMapper.toCompilation(compilationDtoNew, events)));
    }

    @Override
    @Transactional
    public void deleteCompilationById(Long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Подборки событий с id = {} не существует", id));
        }
    }

    @Override
    @Transactional
    public CompilationDtoFull updateCompilationById(CompilationDtoUpdated compilationDtoUpdate, Long id) {
        Compilation compilation = repository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format("Подборки событий с id = {} не существует", id)));
        if (compilationDtoUpdate.getTitle() != null) {
            compilation.setTitle(compilationDtoUpdate.getTitle());
        }
        if (compilationDtoUpdate.getPinned() != null) {
            compilation.setPinned(compilationDtoUpdate.getPinned());
        }
        if (compilationDtoUpdate.getEvents() != null) {
            compilation.setEvents(eventRepository.findAllByIdIn(compilationDtoUpdate.getEvents()));
        }
        return CompilationMapper.toCompilationDtoFull(repository.save(compilation));
    }

    @Override
    public List<CompilationDtoFull> getAllCompilations(Boolean pinned, int from, int size) {
        PageRequestOverride pageRequest = PageRequestOverride.of(from, size);
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = repository.findAllByPinned(pinned, pageRequest);
        } else {
            compilations = repository.findAll();
        }
        return CompilationMapper.toCompilationDtoFullList(compilations);
    }

    @Override
    public CompilationDtoFull getCompilationById(Long id) {
        return CompilationMapper.toCompilationDtoFull(repository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format("Подборки событий с id = {} не существует", id))));
    }
}

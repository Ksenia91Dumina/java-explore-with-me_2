package ru.practicum.compilation.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.compilation.dto.CompilationDtoFull;
import ru.practicum.compilation.dto.CompilationDtoNew;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompilationMapper {

    public static Compilation toCompilation(CompilationDtoNew compilationDtoNew, List<Event> events) {
        return Compilation.builder()
            .title(compilationDtoNew.getTitle())
            .pinned(compilationDtoNew.getPinned())
            .events(events)
            .build();
    }

    public static CompilationDtoFull toCompilationDtoFull(Compilation compilation) {
        return CompilationDtoFull.builder()
            .id(compilation.getId())
            .title(compilation.getTitle())
            .pinned(compilation.getPinned())
            .events(EventMapper.toEventDtoShort(compilation.getEvents()))
            .build();
    }

    public static List<CompilationDtoFull> toCompilationDtoFullList(List<Compilation> compilations) {
        List<CompilationDtoFull> compilationsDtoFull = new ArrayList<>();
        for (Compilation compilation : compilations) {
            compilationsDtoFull.add(toCompilationDtoFull(compilation));
        }
        return compilationsDtoFull;
    }
}

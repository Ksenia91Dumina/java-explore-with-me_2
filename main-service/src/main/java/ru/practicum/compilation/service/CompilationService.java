package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDtoFull;
import ru.practicum.compilation.dto.CompilationDtoNew;
import ru.practicum.compilation.dto.CompilationDtoUpdated;

import java.util.List;

public interface CompilationService {

    CompilationDtoFull createCompilation(CompilationDtoNew compilationDtoNew);

    CompilationDtoFull getCompilationById(Long id);

    void deleteCompilationById(Long id);

    CompilationDtoFull updateCompilationById(CompilationDtoUpdated compilationDtoUpdate, Long id);

    List<CompilationDtoFull> getAllCompilations(Boolean pinned, int from, int size);

}

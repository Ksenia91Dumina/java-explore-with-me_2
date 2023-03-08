package ru.practicum.compilation.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.event.dto.EventDtoShort;

import java.util.List;

@Data
@Builder
public class CompilationDtoFull {

    private Long id;

    private String title;

    private Boolean pinned;

    private List<EventDtoShort> events;




}
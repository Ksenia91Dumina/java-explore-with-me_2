package ru.practicum.compilation.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompilationDtoUpdated {

    private String title;

    private Boolean pinned;
    private List<Long> events;

}

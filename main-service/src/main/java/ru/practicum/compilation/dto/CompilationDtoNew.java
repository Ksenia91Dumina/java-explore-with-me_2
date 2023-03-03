package ru.practicum.compilation.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class CompilationDtoNew {

    @NotBlank
    private String title;

    private Boolean pinned;
    private List<Long> events;


}
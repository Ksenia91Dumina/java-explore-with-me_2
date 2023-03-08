package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.category.dto.CategoryDtoFull;
import ru.practicum.user.dto.UserDtoShort;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDtoShort {
    Long id;
    @NotBlank
    String annotation;
    CategoryDtoFull category;
    Long confirmedRequests;
    @NotBlank
    String eventDate;
    @NotNull
    UserDtoShort initiator;
    @NotNull
    Boolean paid;
    @NotBlank
    String title;
    Long views;

}

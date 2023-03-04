package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.category.dto.CategoryDtoFull;
import ru.practicum.event.model.EventState;
import ru.practicum.user.dto.UserDtoShort;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventDtoFull {

    Long id;
    @NotBlank
    String annotation;
    CategoryDtoFull category;
    Long confirmedRequests;
    String createdOn;
    @NotBlank
    String description;
    @NotBlank
    String eventDate;
    @NotNull
    UserDtoShort initiator;
    @NotNull
    LocationDto location;
    @NotNull
    Boolean paid;
    Long participantLimit;
    String publishedOn;
    Boolean requestModeration;
    EventState state;
    @NotBlank
    String title;
    Long views;

}

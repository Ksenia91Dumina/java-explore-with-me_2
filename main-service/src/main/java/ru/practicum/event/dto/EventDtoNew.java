package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDtoNew {
    @Length(min = 20, max = 2000)
    String annotation;
    @NotNull
    Long category;
    @Length(min = 20, max = 7000)
    String description;
    @NotBlank
    String eventDate;
    @NotNull
    LocationDto location;
    Boolean paid;
    Long participantLimit;
    Boolean requestModeration;
    @Length(min = 3, max = 120)
    String title;

}

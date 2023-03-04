package ru.practicum.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.request.model.RequestState;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDtoUpdateStatus {
    List<Long> requestIds;

    RequestState status;
}

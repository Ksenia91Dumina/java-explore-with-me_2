package ru.practicum.request.service;

import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestDtoUpdateStatus;
import ru.practicum.request.dto.RequestDtoUpdated;

import java.util.List;

public interface RequestService {
    RequestDto createRequest(Long userId, Long eventId);

    List<RequestDto> findRequestByRequesterId(Long userId);

    RequestDto cancelRequest(Long userId, Long requestId);

    List<RequestDto> findRequestsForEventInitiator(Long initiatorId, Long eventId);

    RequestDtoUpdated updateRequestStatusByEventInitiator(
        Long userId, Long eventId, RequestDtoUpdateStatus participationRequestDtoStatusUpdate);
}

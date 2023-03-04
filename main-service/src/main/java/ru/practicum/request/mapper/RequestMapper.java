package ru.practicum.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.request.model.RequestState.CONFIRMED;
import static ru.practicum.request.model.RequestState.PENDING;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMapper {

    public static Request toRequest(Event event, User requester) {
        Request request = new Request();
        request.setEvent(event);
        request.setRequester(requester);
        request.setCreatedOn(LocalDateTime.now());
        request.setStatus(event.getRequestModeration() ? PENDING : CONFIRMED);
        return request;
    }

    public static RequestDto toRequestDto(Request request) {
        return RequestDto.builder()
            .id(request.getId())
            .event(request.getEvent().getId())
            .requester(request.getRequester().getId())
            .status(request.getStatus())
            .createdOn(request.getCreatedOn())
            .build();
    }

    public static List<RequestDto> toRequestDtoList(List<Request> participationRequests) {
        List<RequestDto> requestDtoList = new ArrayList<>();
        for (Request participationRequest : participationRequests) {
            requestDtoList.add(toRequestDto(participationRequest));
        }
        return requestDtoList;
    }
}

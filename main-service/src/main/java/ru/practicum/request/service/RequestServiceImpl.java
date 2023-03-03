package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestDtoUpdateStatus;
import ru.practicum.request.dto.RequestDtoUpdated;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestState;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository repository;

    private final UserRepository userRepository;

    private final EventRepository eventRepository;

    @Override
    @Transactional
    public RequestDto createRequest(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(String.format("Пользователя с id = {} не существует", userId)));
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new NotFoundException(String.format("События с id = {} не существует", eventId)));
        if (!event.getRequestModeration()) {
            if (event.getInitiator().getId().equals(userId)) {
                throw new NotFoundException("Организатор не может быть участником события");
            }
            if (!event.getState().equals(EventState.PUBLISHED)) {
                throw new NotFoundException("Событие еще не опубликовано");
            }
            if ((event.getParticipantLimit() - event.getConfirmedRequests()) <= 0) {
                throw new NotFoundException("Свободных мест нет");
            }
        }

        RequestDto requestDto = RequestDto.builder()
            .createdDate(LocalDateTime.now())
            .event(eventId)
            .requester(userId)
            .status(RequestState.PENDING)
            .build();

        return RequestMapper.toRequestDto(repository.save(RequestMapper.toRequest(requestDto)));
    }

    @Override
    public List<RequestDto> findRequestByRequesterId(Long userId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(String.format("Пользователя с id = {} не существует", userId)));
        return RequestMapper.toRequestDtoList(repository.findAllByRequesterId(userId));
    }

    @Override
    public List<RequestDto> findRequestsForEventInitiator(Long initiatorId, Long eventId) {
        return RequestMapper.toRequestDtoList(repository.findAllByEventId(eventId));
    }

    @Override
    @Transactional
    public RequestDto cancelRequest(Long userId, Long requestId) {
        Request request = repository
            .findRequestByIdAndRequesterId(requestId, userId)
            .orElseThrow(() -> new NotFoundException(String.format("Запроса с id = {} не существует", requestId)));
        if (!userId.equals(request.getRequester())) {
            throw new ConflictException("Отменить запрос может пользователь, создавший его");
        }
        if (request.getStatus().equals(RequestState.CANCELED)) {
            throw new ConflictException("Данный запрос уже отменен");
        }
        request.setStatus(RequestState.CANCELED);
        return RequestMapper.toRequestDto(repository.save(request));
    }

    @Override
    @Transactional
    public RequestDtoUpdated updateRequestStatusByEventInitiator(
        Long userId, Long eventId, RequestDtoUpdateStatus participationRequestDtoStatusUpdate) {

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
            String.format("События с id = {} не существует", eventId)));
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException("Не удалось выполнить запрос");
        }
        List<Request> participationRequests = repository.findAllByIdIn(
            participationRequestDtoStatusUpdate.getRequestIds());
        RequestDtoUpdated participationRequestDtoUpdated = new RequestDtoUpdated(
            new ArrayList<>(), new ArrayList<>());
        if (participationRequestDtoStatusUpdate.getStatus().equals(RequestState.CONFIRMED)) {
            if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
                throw new ConflictException("Невозможно превысить лимит возможных участников");
            }
            for (Request participationRequest : participationRequests) {
                if (!participationRequest.getStatus().equals(RequestState.PENDING)) {
                    throw new ConflictException("Невозможно изменить статус");
                }
                if (event.getConfirmedRequests() < event.getParticipantLimit()) {
                    participationRequest.setStatus(RequestState.CONFIRMED);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                    participationRequestDtoUpdated.getConfirmedRequests()
                        .add(RequestMapper.toRequestDto(participationRequest));
                } else {
                    participationRequest.setStatus(RequestState.REJECTED);
                    participationRequestDtoUpdated.getRejectedRequests()
                        .add(RequestMapper.toRequestDto(participationRequest));
                }
            }
            repository.saveAll(participationRequests);
            eventRepository.save(event);
            return participationRequestDtoUpdated;
        } else if (participationRequestDtoStatusUpdate.getStatus().equals(RequestState.REJECTED)) {
            for (Request participationRequest : participationRequests) {
                if (!participationRequest.getStatus().equals(RequestState.PENDING)) {
                    throw new ConflictException("Невозможно изменить статус");
                }
                participationRequest.setStatus(RequestState.REJECTED);
                participationRequestDtoUpdated.getRejectedRequests()
                    .add(RequestMapper.toRequestDto(participationRequest));
            }
            repository.saveAll(participationRequests);
        }
        return participationRequestDtoUpdated;
    }
}
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

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.request.model.RequestState.CONFIRMED;
import static ru.practicum.request.model.RequestState.REJECTED;


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
        if (repository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new ConflictException(String.format("Запрос на событие с id = {} и " +
                "пользователем с id = {} уже существует", eventId, userId));
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Организатор не может быть участником события");
        }
        if (!(EventState.PUBLISHED).equals(event.getState())) {
            throw new ConflictException("Событие еще не опубликовано");
        }
        if (event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            throw new ConflictException("Свободных мест нет");
        }
        if (!event.getRequestModeration()) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        return RequestMapper.toRequestDto(repository.save(RequestMapper.toRequest(event, requester)));
    }

    @Override
    public List<RequestDto> findRequestByRequesterId(Long userId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(String.format("Пользователя с id = {} не существует", userId)));
        return RequestMapper.toRequestDtoList(repository.findAllByRequesterId(userId));
    }

    @Override
    public List<RequestDto> findRequestsForEventInitiator(Long initiatorId, Long eventId) {
        eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
            String.format("События с id = {} не существует", eventId)));
        userRepository.findById(initiatorId).orElseThrow(() -> new NotFoundException(
            String.format("Пользователя с id = {} не существует", initiatorId)));
        return RequestMapper.toRequestDtoList(repository.findAllByEventId(eventId));
    }

    @Override
    @Transactional
    public RequestDto cancelRequest(Long userId, Long requestId) {
        Request request = repository.findByIdAndRequesterId(requestId, userId)
            .orElseThrow(() -> new NotFoundException(String.format("Запроса с id = {} не существует", requestId)));
        if ((RequestState.CANCELED).equals(request.getStatus())) {
            throw new ConflictException("Данный запрос уже отменен");
        }
        request.setStatus(RequestState.CANCELED);
        return RequestMapper.toRequestDto(repository.save(request));
    }

    @Override
    @Transactional
    public RequestDtoUpdated updateRequestStatusByEventInitiator(
        Long userId, Long eventId, RequestDtoUpdateStatus requestDtoStatusUpdate) {

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
            String.format("События с id = {} не существует", eventId)));
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
            String.format("Пользователя с id = {} не существует", userId)));

        List<RequestDto> confirmedRequests = List.of();
        List<RequestDto> rejectedRequests = List.of();

        List<Long> requestIds = requestDtoStatusUpdate.getRequestIds();
        List<Request> requests = requestIds.stream()
            .map(this::getRequestById)
            .collect(Collectors.toList());

        String status = requestDtoStatusUpdate.getStatus();

        if ((REJECTED.toString()).equals(status)) {
            rejectedRequests = requests.stream()
                .peek(request -> request.setStatus(REJECTED))
                .map(repository::save)
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
            return new RequestDtoUpdated(confirmedRequests, rejectedRequests);
        }

        Long participantLimit = event.getParticipantLimit();
        Long approvedRequests = event.getConfirmedRequests();
        Long availableParticipants = participantLimit - approvedRequests;
        Long potentialParticipants = (long) requestIds.size();

        if (participantLimit > 0 && participantLimit.equals(approvedRequests)) {
            throw new ConflictException(String.format("Количество участников превышено", eventId));
        }

        if ((CONFIRMED.toString()).equals(status)) {
            if (participantLimit.equals(0L) ||
                (potentialParticipants <= availableParticipants && !event.getRequestModeration())) {
                confirmedRequests = requests.stream()
                    .peek(request -> request.setStatus(CONFIRMED))
                    .map(repository::save)
                    .map(RequestMapper::toRequestDto)
                    .collect(Collectors.toList());
                event.setConfirmedRequests(approvedRequests + potentialParticipants);
            } else {
                confirmedRequests = requests.stream()
                    .limit(availableParticipants)
                    .peek(request -> request.setStatus(CONFIRMED))
                    .map(repository::save)
                    .map(RequestMapper::toRequestDto)
                    .collect(Collectors.toList());
                rejectedRequests = requests.stream()
                    .skip(availableParticipants)
                    .peek(request -> request.setStatus(REJECTED))
                    .map(repository::save)
                    .map(RequestMapper::toRequestDto)
                    .collect(Collectors.toList());
                event.setConfirmedRequests(participantLimit);
            }
        }
        eventRepository.save(event);
        return new RequestDtoUpdated(confirmedRequests, rejectedRequests);
    }

    private Request getRequestById(Long requestId) {
        Request request = repository.findById(requestId)
            .orElseThrow(() -> new NotFoundException(String.format("Запрос с id = {} не найден", requestId)));
        if (!(RequestState.PENDING).equals(request.getStatus())) {
            throw new ConflictException("Статус запроса должен быть PENDING");
        }
        return request;
    }
}


package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequesterId(Long id);

    Optional<Request> findByIdAndRequesterId(Long id, Long requesterId);
    Optional<Request> findByRequesterIdAndEventId(Long userId, Long eventId);


    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByIdIn(List<Long> eventIds);
}

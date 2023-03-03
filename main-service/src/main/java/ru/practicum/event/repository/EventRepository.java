package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.addition.MyPageRequest;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findAllByInitiatorId(Long initiatorId, MyPageRequest pageRequest);

    List<Event> findAllByIdIn(List<Long> ids);

    @Query("SELECT e FROM Event AS e " +
        "WHERE (e.category.id IN :categories) AND (e.initiator.id IN :users) AND (e.state IN :states)")
    List<Event> findByInitiatorAndCategoriesAndState(List<Long> users, List<Long> categories,
                                                     List<EventState> states, MyPageRequest pageRequest);

    @Query("SELECT e FROM Event AS e WHERE (e.state IN :states)")
    List<Event> findByState(List<EventState> states, MyPageRequest pageRequest);

    @Query("SELECT e FROM Event AS e " +
        "WHERE (e.category.id IN :categories) AND (e.state IN :states)")
    List<Event> findByCategoriesAndState(List<Long> categories, List<EventState> states, MyPageRequest pageRequest);

    @Query("SELECT e FROM Event AS e " +
        "WHERE (e.initiator.id IN :users) AND (e.state IN :states)")
    List<Event> findByInitiatorAndState(List<Long> users, List<EventState> states, MyPageRequest pageRequest);

    @Query("SELECT e FROM Event AS e " +
        "WHERE (LOWER(e.annotation) LIKE CONCAT('%',LOWER(:text),'%') OR " +
        "LOWER(e.description) LIKE CONCAT('%',LOWER(:text),'%'))")
    List<Event> findByText(String text);

    @Query("SELECT e FROM Event AS e WHERE (e.category.id IN :categories) AND " +
        "(LOWER(e.annotation) LIKE CONCAT('%',LOWER(:text),'%') OR " +
        "LOWER(e.description) LIKE CONCAT('%',LOWER(:text),'%'))")
    List<Event> findByCategoryIdsAndText(String text, List<Long> categories);
}

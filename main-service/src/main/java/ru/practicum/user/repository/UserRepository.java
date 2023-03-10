package ru.practicum.user.repository;

import org.springframework.data.jpa.repository.Query;
import ru.practicum.addition.PageRequestOverride;
import ru.practicum.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface  UserRepository extends JpaRepository<User, Long> {
    Page<User> findAllByIdIn(List<Long> ids, PageRequestOverride pageRequest);

    List<User> findAll();

    @Query("select u.name from User u")
    List<String> findByNameOrderByName();
}

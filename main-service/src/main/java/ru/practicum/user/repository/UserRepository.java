package ru.practicum.user.repository;

import org.springframework.data.jpa.repository.Query;
import ru.practicum.addition.MyPageRequest;
import ru.practicum.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface  UserRepository extends JpaRepository<User, Long> {
    Page<User> findAllByIdIn(List<Long> ids, MyPageRequest pageRequest);

    List<User> findAllByIdIn(List<Long> ids);

    @Query("select u.name from User u")
    List<String> findByNameOrderByName();
}

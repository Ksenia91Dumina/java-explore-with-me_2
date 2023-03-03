package ru.practicum.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.category.model.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("select c.name from Category c")
    List<String> findByNameOrderByName();

    List<Category> findAllByIdIn(List<Long> ids);
}
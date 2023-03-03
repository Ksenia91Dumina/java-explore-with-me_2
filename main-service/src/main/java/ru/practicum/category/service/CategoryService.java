package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDtoFull;
import ru.practicum.category.dto.CategoryDtoNew;

import java.util.List;

public interface CategoryService {

    CategoryDtoFull create(CategoryDtoNew categoryDtoNew);

    void deleteById(Long id);

    CategoryDtoFull updateById(CategoryDtoNew categoryDtoNew, Long id);

    List<CategoryDtoFull> getAll(int from, int size);

    CategoryDtoFull findById(Long id);
}

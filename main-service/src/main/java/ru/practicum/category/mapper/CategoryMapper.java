package ru.practicum.category.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.category.dto.CategoryDtoFull;
import ru.practicum.category.dto.CategoryDtoNew;
import ru.practicum.category.model.Category;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryMapper {

    public static Category toCategory(CategoryDtoNew categoryDtoNew) {
        return Category.builder()
            .name(categoryDtoNew.getName())
            .build();
    }

    public static Category toCategory(CategoryDtoFull categoryDtoFull) {
        return Category.builder()
            .id(categoryDtoFull.getId())
            .name(categoryDtoFull.getName())
            .build();
    }

    public static CategoryDtoFull toCategoryDtoFull(Category category) {
        return CategoryDtoFull.builder()
            .id(category.getId())
            .name(category.getName())
            .build();
    }

    public static List<CategoryDtoFull> toCategoryDtoFull(List<Category> categories) {
        List<CategoryDtoFull> categoriesDtoFull = new ArrayList<>();
        for (Category category : categories) {
            categoriesDtoFull.add(toCategoryDtoFull(category));
        }
        return categoriesDtoFull;
    }
}

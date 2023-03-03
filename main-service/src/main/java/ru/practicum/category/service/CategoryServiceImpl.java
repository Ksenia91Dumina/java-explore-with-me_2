package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.addition.MyPageRequest;
import ru.practicum.category.dto.CategoryDtoFull;
import ru.practicum.category.dto.CategoryDtoNew;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    @Override
    @Transactional
    public CategoryDtoFull create(CategoryDtoNew categoryDtoNew) {
        repository.findByNameOrderByName()
            .stream()
            .filter(name -> name.equals(categoryDtoNew.getName())).forEachOrdered(name -> {
                throw new ConflictException(
                    String.format("Категория с названием %s уже существует", name));
            });
        Category categories = CategoryMapper.toCategory(categoryDtoNew);
        Category categoriesSave = repository.save(categories);
        return CategoryMapper.toCategoryDtoFull(categoriesSave);
    }

    @Override
    public CategoryDtoFull findById(Long id) {
        return CategoryMapper.toCategoryDtoFull(repository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format("Не найдена категория с id = {}", id))));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Не найдена категория с id = {}, удалить нельзя", id));
        }
    }

    @Override
    @Transactional
    public CategoryDtoFull updateById(CategoryDtoNew categoryDtoNew, Long id) {
        Category category = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Невозможно обновить - категория не найдена"));
        repository.findByNameOrderByName()
            .stream()
            .filter(name -> name.equals(categoryDtoNew.getName())).forEachOrdered(name -> {
                throw new ConflictException(
                    String.format("Категория с названием %s уже существует", name));
            });
        category.setName(categoryDtoNew.getName());
        return CategoryMapper.toCategoryDtoFull(repository.save(category));
    }

    @Override
    public List<CategoryDtoFull> getAll(int from, int size) {
        MyPageRequest pageRequest = MyPageRequest.of(from, size);
        return repository.findAll(pageRequest)
            .stream()
            .map(CategoryMapper::toCategoryDtoFull)
            .collect(Collectors.toList());
    }

}


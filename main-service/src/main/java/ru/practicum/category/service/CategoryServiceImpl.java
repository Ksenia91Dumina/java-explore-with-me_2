package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.addition.PageRequestOverride;
import ru.practicum.category.dto.CategoryDtoFull;
import ru.practicum.category.dto.CategoryDtoNew;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDtoFull create(CategoryDtoNew categoryDtoNew) {
        repository.findByNameOrderByName()
            .stream()
            .filter(name -> name.equals(categoryDtoNew.getName())).forEachOrdered(name -> {
                throw new ConflictException(
                    String.format("Категория с названием %s уже существует", name));
            });
        return CategoryMapper.toCategoryDtoFull(repository.save(CategoryMapper.toCategory(categoryDtoNew)));
    }

    @Override
    public CategoryDtoFull findById(Long id) {
        return CategoryMapper.toCategoryDtoFull(repository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format("Не найдена категория с id = {}", id))));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.findById(id)
            .orElseThrow(
                () -> new NotFoundException(String.format("Не найдена категория с id = {}, удалить нельзя", id)));
        if (eventRepository.findAllByCategoryId(id).size() == 0) {
            repository.deleteById(id);
        } else {
            throw new ConflictException(String.format("Категория с id = {} не пустая", id));
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
        if (categoryDtoNew.getName().isBlank()) {
            throw new BadRequestException("Название не может быть пустым");
        }
        category.setName(categoryDtoNew.getName());
        return CategoryMapper.toCategoryDtoFull(repository.save(category));
    }

    @Override
    public List<CategoryDtoFull> getAll(int from, int size) {
        PageRequestOverride pageRequest = PageRequestOverride.of(from, size);
        return repository.findAll(pageRequest)
            .stream()
            .map(CategoryMapper::toCategoryDtoFull)
            .collect(Collectors.toList());
    }

}


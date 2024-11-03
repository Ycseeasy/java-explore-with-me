package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.DuplicateNameException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationExceptionFindCategory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CategoryDto addCategoryAdmin(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new DuplicateNameException("Category name " + newCategoryDto.getName()
                    + "already exists in the system.");
        }
        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(newCategoryDto)));
    }

    @Transactional
    @Override
    public CategoryDto updateCategoryAdmin(Long catId, NewCategoryDto newCategoryDto) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with ID " + catId + " not found");
        }
        if (categoryRepository.existsByName(newCategoryDto.getName())
                && !categoryRepository.getByName(newCategoryDto.getName()).getId().equals(catId)) {
            throw new DuplicateNameException("Category name " + newCategoryDto.getName()
                    + "already exists in the system.");
        }
        Category newCategory = CategoryMapper.toCategory(newCategoryDto);
        newCategory.setId(catId);
        return CategoryMapper.toCategoryDto(categoryRepository.save(newCategory));
    }

    @Transactional
    @Override
    public void deleteCategoryAdmin(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with ID " + catId + " not found");
        }
        if (eventRepository.findFirstByCategoryId(catId) != null) {
            throw new ValidationExceptionFindCategory("Category with ID " + catId + " not empty");
        }
        categoryRepository.deleteById(catId);
    }


    @Transactional
    @Override
    public List<CategoryDto> getCategoryPublic(Integer from, Integer size) {

        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);

        return categoryRepository.findAll(pageable)
                .stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public CategoryDto getCategoryByIdPublic(Long catId) {
        Optional<Category> result = categoryRepository.findById(catId);
        if (result.isPresent()) {
            return CategoryMapper.toCategoryDto(result.get());
        } else {
            throw new NotFoundException("Category with ID " + catId + " not found");
        }
    }
}
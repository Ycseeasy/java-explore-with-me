package ru.practicum.ewm.category.controllers;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping(path = "/categories")
@Validated
@RequiredArgsConstructor
public class CategoryPublicController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getCategoryPublic(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from, @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return categoryService.getCategoryPublic(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategoryByIdPublic(@Positive @PathVariable Long catId) {
        return categoryService.getCategoryByIdPublic(catId);
    }
}
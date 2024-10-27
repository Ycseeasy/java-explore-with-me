package ru.practicum.ewm.controllers.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

@Slf4j
@RestController
@RequestMapping(path = "/admin/categories")
@Validated
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryService categoryService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CategoryDto addCategoryAdmin(HttpServletRequest request, @Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.addCategoryAdmin(newCategoryDto);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategoryAdmin(HttpServletRequest request, @Positive @PathVariable Long catId, @Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.updateCategoryAdmin(catId, newCategoryDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{catId}")
    public void deleteCategoryAdmin(HttpServletRequest request, @Positive @PathVariable("catId") Long catId) {
        categoryService.deleteCategoryAdmin(catId);
    }
}
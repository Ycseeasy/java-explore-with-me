package ru.practicum.ewm.controllers.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.service.CompilationService;

@Slf4j
@RestController
@RequestMapping(path = "/admin/compilations")
@Validated
@RequiredArgsConstructor
public class CompilationAdminController {

    private final CompilationService compilationService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CompilationDto addCompilationAdmin(HttpServletRequest request, @Valid @RequestBody NewCompilationDto newCompilationDto) {
        return compilationService.addCompilationAdmin(newCompilationDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{compId}")
    public void deleteCompilationByIdAdmin(HttpServletRequest request, @Positive @PathVariable("compId") Long compId) {
        compilationService.deleteCompilationByIdAdmin(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilationByIdAdmin(HttpServletRequest request, @Positive @PathVariable Long compId, @Valid @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        return compilationService.updateCompilationByIdAdmin(compId, updateCompilationRequest);
    }

}
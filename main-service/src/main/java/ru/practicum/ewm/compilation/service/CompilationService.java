package ru.practicum.ewm.compilation.service;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> getCompilationsPublic(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationByIdPublic(Long compId);

    CompilationDto addCompilationAdmin(NewCompilationDto newCompilationDto);

    void deleteCompilationByIdAdmin(Long compId);

    CompilationDto updateCompilationByIdAdmin(Long compId, UpdateCompilationRequest updateCompilationRequest);

}
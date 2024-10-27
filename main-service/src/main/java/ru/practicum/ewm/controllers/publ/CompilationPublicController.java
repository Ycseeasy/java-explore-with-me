package ru.practicum.ewm.controllers.publ;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping(path = "/compilations")
@Validated
@RequiredArgsConstructor
public class CompilationPublicController {

    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilationsPublic(HttpServletRequest request,
                                                      @RequestParam(required = false, name = "pinned") Boolean pinned,
                                                      @PositiveOrZero
                                                          @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                      @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return compilationService.getCompilationsPublic(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationByIdPublic(HttpServletRequest request, @Positive @PathVariable Long compId) {
        return compilationService.getCompilationByIdPublic(compId);
    }

}
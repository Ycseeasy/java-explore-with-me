package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UpdateCompilationRequest {

    private List<Long> events;
    private boolean pinned;
    @Size(min = 1, max = 50)
    private String title;
}

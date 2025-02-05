package ru.practicum.ewm.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class CommentDto {

    private Long id;
    @NotBlank
    private String text;
    @NonNull
    private Long event;
    @NonNull
    private Long author;
    private String created;
}
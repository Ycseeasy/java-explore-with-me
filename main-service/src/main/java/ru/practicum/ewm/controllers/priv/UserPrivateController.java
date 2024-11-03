package ru.practicum.ewm.controllers.priv;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.participation.service.ParticipationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@Validated
@RequiredArgsConstructor
public class UserPrivateController {

    private final ParticipationService participationService;

    @GetMapping
    public List<ParticipationRequestDto> getParticipationRequestPrivate(@NotNull @Positive @PathVariable Long userId) {
        return participationService.getParticipationRequestPrivate(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequestPrivate(@Positive @PathVariable(required = false) Long userId, @Positive @RequestParam(required = false) Long eventId) {

        return participationService.addParticipationRequestPrivate(userId, eventId);
    }


    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto updateRejectedParticipationRequestPrivate(@NotNull @Positive @PathVariable Long userId, @NotNull @Positive @PathVariable(required = true, name = "requestId") Long requestId) {
        return participationService.updateRejectedParticipationRequestPrivate(userId, requestId);
    }
}

package com.example.football.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateMatchRequest(
        @NotNull LocalDate matchDate,
        @NotNull Long homeTeamId,
        @NotNull Long awayTeamId,
        @Min(0) int homeScore,
        @Min(0) int awayScore,
        String venue
) {
}
